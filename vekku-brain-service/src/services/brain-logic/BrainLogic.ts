import { pipeline, env, FeatureExtractionPipeline } from '@huggingface/transformers';
import { QdrantClient } from '@qdrant/js-client-rest';
import { v4 as uuidv4 } from 'uuid';
import { ContentRegionTags } from './model';
import { RecursiveCharacterTextSplitter } from "@langchain/textsplitters";
import { findFuzzyMatch } from '../../utils/textUtils';
import { cosineSimilarity } from '../../utils/mathUtils';
import { config } from '../../config';

// Configuration: Force local execution (no remote API calls to HuggingFace)
env.allowLocalModels = false;
env.useBrowserCache = false;

export class BrainLogic {
    private static instance: BrainLogic;
    private qdrant: QdrantClient;
    private embedder: FeatureExtractionPipeline | null = null;

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
            this.embedder = (await pipeline('feature-extraction', config.ai.modelName) as unknown) as FeatureExtractionPipeline;
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
        const output = await this.embedder!(tagName, { pooling: 'mean', normalize: true });
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
     * Also calculates OVERALL tags by aggregating results from all regions.
     */
    public async suggestTags(content: string, threshold: number = 0.3, topK: number = 50): Promise<{ regions: ContentRegionTags[], overallTags: { name: string, score: number }[] }> {
        if (!this.embedder) await this.initialize();

        console.log(`🤔 Thinking about tags for content length: ${content.length} (Threshold: ${threshold}, TopK: ${topK})`);

        // 1. Chunk content using Industry Standard Splitter (LangChain)
        // We use specific separators to catch semantic shifts in run-on sentences
        const splitter = new RecursiveCharacterTextSplitter({
            separators: [".", "!", "?", "\n", " but ", " and ", " then ", " "],
            chunkSize: 100, // Reasonable size for a "thought"
            chunkOverlap: 20, // Overlap to maintain context
        });

        const docs = await splitter.createDocuments([content]);
        const regions: ContentRegionTags[] = [];

        // Map to store aggregated scores for overall calculation
        // Key: Tag Name, Value: Sum of scores
        const globalTagScores = new Map<string, number>();

        // 2. Process each chunk
        for (const doc of docs) {
            const chunkText = doc.pageContent;

            // 3. Embed the chunk
            const output = await this.embedder!(chunkText, { pooling: 'mean', normalize: true });
            const vector = Array.from(output.data) as number[];

            // 4. Search Qdrant
            const result = await this.qdrant.search(config.qdrant.collectionName, {
                vector: vector,
                limit: topK,
                score_threshold: threshold,
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

                    // Aggregate scores for Global Tags
                    tagScores.forEach(t => {
                        const currentSum = globalTagScores.get(t.name) || 0;
                        globalTagScores.set(t.name, currentSum + t.score);
                    });
                }
            }
        }

        // 5. Calculate Overall Tags (Weighted Consensus)
        // Formula: Sum(Scores) * (1 / log(TotalChunks + 1))
        // We add +1 to log to avoid division by zero or negative results for few chunks
        const totalChunks = regions.length;
        const decayFactor = totalChunks > 1 ? (1.0 / Math.log(totalChunks + Math.E)) : 1.0;

        const overallTags = Array.from(globalTagScores.entries())
            .map(([name, totalScore]) => ({
                name,
                score: totalScore * decayFactor
            }))
            .sort((a, b) => b.score - a.score)
            .slice(0, topK); // Return top K global tags

        return { regions, overallTags };
    }

    /**
     * ⚖️ SCORE: Evaluates relevance of specific tags against content.
     * Embeds the content and each tag, then calculates Cosine Similarity.
     */
    public async scoreTags(tags: string[], content: string): Promise<{ name: string, score: number }[]> {
        if (!this.embedder) await this.initialize();

        console.log(`⚖️ Scoring ${tags.length} tags against content ("${content.substring(0, 20)}...")`);

        // 1. Embed Content
        const contentOutput = await this.embedder!(content, { pooling: 'mean', normalize: true });
        const contentVector = Array.from(contentOutput.data) as number[];

        const results: { name: string, score: number }[] = [];

        // 2. Embed & Score each Tag
        // Optimization: In prod, we should fetch tag vectors from Qdrant if they exist.
        for (const tag of tags) {
            const tagOutput = await this.embedder!(tag, { pooling: 'mean', normalize: true });
            const tagVector = Array.from(tagOutput.data) as number[];

            const score = cosineSimilarity(contentVector, tagVector);
            results.push({ name: tag, score });
        }

        return results;
    }
}