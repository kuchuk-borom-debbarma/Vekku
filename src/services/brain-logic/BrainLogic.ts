import { pipeline, env } from '@xenova/transformers';
import { QdrantClient } from '@qdrant/js-client-rest';
import { v4 as uuidv4 } from 'uuid';
import { ContentRegionTags } from './model';

// Configuration: Force local execution (no remote API calls to HuggingFace)
env.allowLocalModels = false;
env.useBrowserCache = false;

export class BrainLogic {
    private static instance: BrainLogic;
    private qdrant: QdrantClient;
    private embedder: any = null;

    // Constants
    private readonly COLLECTION_NAME = "vekku_brain";
    private readonly MODEL_NAME = "Xenova/bge-small-en-v1.5";

    private constructor() {
        // Connect to Qdrant (ensure it's running on localhost:6333)
        this.qdrant = new QdrantClient({ url: 'http://localhost:6333' });
    }

    public static getInstance(): BrainLogic {
        if (!BrainLogic.instance) {
            BrainLogic.instance = new BrainLogic();
        }
        return BrainLogic.instance;
    }

    /**
     * 🚀 INITIALIZE: Ensures DB exists and Model is loaded
     */
    public async initialize() {
        console.log("🔌 Connecting to Qdrant...");

        // 1. Create Collection if missing
        const result = await this.qdrant.getCollections();
        const exists = result.collections.some(c => c.name === this.COLLECTION_NAME);

        if (!exists) {
            console.log(`📦 Creating collection: ${this.COLLECTION_NAME}`);
            await this.qdrant.createCollection(this.COLLECTION_NAME, {
                vectors: { size: 384, distance: 'Cosine' } // BGE-Small is 384 dim
            });
        }

        // 2. Load AI Model (Singleton)
        if (!this.embedder) {
            console.log(`🧠 Loading AI Model: ${this.MODEL_NAME}...`);
            this.embedder = await pipeline('feature-extraction', this.MODEL_NAME);
            console.log("✅ Model Loaded!");
        }
    }

    /**
     * 🧠 LEARN: The Logic from your Javadoc
     * Embeds the tag and saves it with "type=TAG"
     */
    public async learnTag(tagName: string): Promise<void> {
        if (!this.embedder) await this.initialize();

        console.log(`🎓 Learning concept: "${tagName}"`);

        // 1. Convert Text -> Vector
        // The model returns a Tensor, we need a plain array
        const output = await this.embedder(tagName, { pooling: 'mean', normalize: true });
        const vector = Array.from(output.data) as number[];

        // 2. Save to Qdrant
        await this.qdrant.upsert(this.COLLECTION_NAME, {
            wait: true,
            points: [
                {
                    id: uuidv4(), // Generate unique ID
                    vector: vector,
                    payload: {
                        original_name: tagName,
                        type: "TAG" // <--- Crucial Metadata
                    }
                }
            ]
        });

        console.log(`✅ Learned: ${tagName}`);
    }

    /**
     * 🔎 SUGGEST: Finds tags conceptually related to content
     * Splits content into semantic regions and finds tags for each region.
     */
    public async suggestTags(content: string): Promise<ContentRegionTags[]> {
        // Dynamic import to avoid issues if basic loading fails or for lazy loading
        // @ts-ignore
        const { chunkit } = await import('semantic-chunking');

        console.log(`🤔 Thinking about tags for content length: ${content.length}`);

        // 1. Chunk the content using semantic-chunking
        // We use the same model name as defined in the class constant for consistency
        const chunks = await chunkit(
            [{ document_name: 'input', document_text: content }],
            {
                onnxEmbeddingModel: this.MODEL_NAME, // 'Xenova/bge-small-en-v1.5'
                returnEmbedding: true, // Get vectors directly from chunker
                similarityThreshold: 0.5, // Adjustable threshold
                logging: false
            }
        );

        const regions: ContentRegionTags[] = [];
        let searchCursor = 0;

        // 2. Process each chunk
        for (const chunk of chunks) {
            const chunkText = chunk.text;
            if (!chunkText || chunkText.trim().length === 0) continue;

            // Robustly find the chunk in the original text, ignoring whitespace differences
            const match = this.findFuzzyMatch(content, chunkText, searchCursor);

            if (match) {
                // Advance cursor only if we found a match (maintaining order)
                searchCursor = match.end;

                // 3. Search Qdrant for this chunk
                // chunk.embedding is returned because returnEmbedding: true
                const vector = Array.from(chunk.embedding as number[]);

                const result = await this.qdrant.search(this.COLLECTION_NAME, {
                    vector: vector,
                    limit: 3,
                    score_threshold: 0.4,
                    filter: {
                        must: [
                            {
                                key: "type",
                                match: { value: "TAG" }
                            }
                        ]
                    }
                });

                const tagScores = result.map(hit => ({
                    name: hit.payload?.original_name as string,
                    score: hit.score
                })).filter(t => t.name);

                if (tagScores.length > 0) {
                    regions.push({
                        regionContent: content.substring(match.start, match.end), // Use original text content
                        regionStartIndex: match.start,
                        regionEndIndex: match.end,
                        tagScores: tagScores
                    });
                }
            } else {
                console.warn(`Could not align chunk to original text: "${chunkText.substring(0, 20)}..."`);
            }
        }

        return regions;
    }

    /**
     * Helper to find where 'search' appears in 'text' starting from 'fromIndex',
     * ignoring differences in whitespace sequences.
     */
    private findFuzzyMatch(text: string, search: string, fromIndex: number): { start: number, end: number } | null {
        let tIdx = fromIndex;
        let sIdx = 0;
        let matchStart = -1;

        while (tIdx < text.length && sIdx < search.length) {
            const tChar = text[tIdx];
            const sChar = search[sIdx];

            // If characters match, proceed
            if (tChar === sChar) {
                if (matchStart === -1) matchStart = tIdx;
                tIdx++;
                sIdx++;
                continue;
            }

            // If mismatch, check if it's due to whitespace
            const tIsSpace = /\s/.test(tChar);
            const sIsSpace = /\s/.test(sChar);

            if (tIsSpace && sIsSpace) {
                // Both are whitespace, consume both until non-whitespace
                // Actually, just skip whitespace in both until they match or one runs out
                // Simple approach: skip whitespace in Text
                while (tIdx < text.length && /\s/.test(text[tIdx])) tIdx++;
                // Skip whitespace in Search
                while (sIdx < search.length && /\s/.test(search[sIdx])) sIdx++;
                // Loop will continue and compare next non-space chars
                continue;
            } else if (tIsSpace) {
                // Text has extra whitespace, skip it
                tIdx++;
                continue;
            } else if (sIsSpace) {
                // Search has extra whitespace? (Less likely if search is normalized, but possible)
                // Or we missed the start? 
                // If we are getting here inside a match, it implies mismatch.
                // If search has space but text doesn't, strict mismatch unless we allow ignoring space in search.
                sIdx++;
                continue;
            }

            // Real mismatch
            // Reset search (naive backtracking - if we were matching)
            if (matchStart !== -1) {
                // Reset to character after matchStart
                tIdx = matchStart + 1;
                sIdx = 0;
                matchStart = -1;
            } else {
                tIdx++;
            }
        }

        if (sIdx >= search.length) {
            // Found complete match
            return { start: matchStart, end: tIdx };
        }

        return null;
    }
}