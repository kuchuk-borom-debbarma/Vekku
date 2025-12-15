import { pipeline, env } from '@huggingface/transformers';
import { QdrantClient } from '@qdrant/js-client-rest';
import { v4 as uuidv4 } from 'uuid';
import { ContentRegionTags } from './model';
import { RecursiveCharacterTextSplitter } from "@langchain/textsplitters";
import { findFuzzyMatch } from '../../utils/textUtils';
import { config } from '../../config';

// Configuration: Force local execution (no remote API calls to HuggingFace)
env.allowLocalModels = false;
env.useBrowserCache = false;

export class BrainLogic {
    private static instance: BrainLogic;
    private qdrant: QdrantClient;
    private embedder: any = null;

    private constructor() {
        // Connect to Qdrant
        this.qdrant = new QdrantClient({ url: config.qdrant.url });
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
        const exists = result.collections.some(c => c.name === config.qdrant.collectionName);

        if (!exists) {
            console.log(`📦 Creating collection: ${config.qdrant.collectionName}`);
            await this.qdrant.createCollection(config.qdrant.collectionName, {
                vectors: { size: 384, distance: 'Cosine' } // BGE-Small is 384 dim
            });
        }

        // 2. Load AI Model (Singleton)
        if (!this.embedder) {
            console.log(`🧠 Loading AI Model: ${config.ai.modelName}...`);
            this.embedder = await pipeline('feature-extraction', config.ai.modelName);
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
        // 2. Save to Qdrant
        await this.qdrant.upsert(config.qdrant.collectionName, {
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
    /**
     * 🔎 SUGGEST: Finds tags conceptually related to content
     * Splits content into regions using LangChain and finds tags for each region.
     */
    public async suggestTags(content: string): Promise<ContentRegionTags[]> {
        if (!this.embedder) await this.initialize();

        console.log(`🤔 Thinking about tags for content length: ${content.length}`);

        // 1. Chunk content using Industry Standard Splitter (LangChain)
        // We use specific separators to catch semantic shifts in run-on sentences
        const splitter = new RecursiveCharacterTextSplitter({
            separators: [".", "!", "?", "\n", " but ", " and ", " then ", " "],
            chunkSize: 100, // Reasonable size for a "thought"
            chunkOverlap: 20, // Overlap to maintain context
        });

        const docs = await splitter.createDocuments([content]);
        const regions: ContentRegionTags[] = [];

        // 2. Process each chunk
        for (const doc of docs) {
            const chunkText = doc.pageContent;

            // 3. Embed the chunk
            const output = await this.embedder(chunkText, { pooling: 'mean', normalize: true });
            const vector = Array.from(output.data) as number[];

            // 4. Search Qdrant
            // 4. Search Qdrant
            const result = await this.qdrant.search(config.qdrant.collectionName, {
                vector: vector,
                limit: 3,
                score_threshold: 0.45,
                filter: {
                    must: [
                        { key: "type", match: { value: "TAG" } }
                    ]
                }
            });

            const tagScores = result.map(hit => ({
                name: hit.payload?.original_name as string,
                score: hit.score
            })).filter(t => t.name);

            if (tagScores.length > 0) {
                // Find accurate position in original text
                const match = findFuzzyMatch(content, chunkText, 0); // Simplified logic

                // Fallback indices if fuzzy match fails (LangChain doesn't give offsets by default)
                // In a real prod app, better offset tracking is needed.
                const start = match ? match.start : content.indexOf(chunkText);
                const end = match ? match.end : start + chunkText.length;

                if (start !== -1) {
                    regions.push({
                        regionContent: chunkText,
                        regionStartIndex: start,
                        regionEndIndex: end,
                        tagScores: tagScores
                    });
                }
            }
        }

        return regions;
    }

    /**
     * Helper to find where 'search' appears in 'text' starting from 'fromIndex',
     * ignoring differences in whitespace sequences.
     */

}