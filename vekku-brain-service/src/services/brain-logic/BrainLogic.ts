import { pipeline, env, FeatureExtractionPipeline } from '@huggingface/transformers';
import { QdrantClient } from '@qdrant/js-client-rest';
import { v5 as uuidv5 } from 'uuid';
import { ContentRegionTags } from './model';
import { RecursiveCharacterTextSplitter } from "@langchain/textsplitters";
import { findFuzzyMatch } from '../../utils/textUtils';
import { cosineSimilarity } from '../../utils/mathUtils';
import { config } from '../../config';

// Configuration: Force local execution (no remote API calls to HuggingFace)
env.allowLocalModels = false;
env.useBrowserCache = false;

// Namespace for Tag UUID generation (Randomly generated constant to ensure uniqueness of our namespace)
const TAG_NAMESPACE = '6ba7b810-9dad-11d1-80b4-00c04fd430c8'; // Standard DNS namespace as a placeholder, or custom

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
    public async learnTag(inputTagName: string): Promise<void> {
        if (!this.embedder) await this.initialize();

        // Normalize to lowercase to ensure "Java" == "java"
        const tagName = inputTagName.toLowerCase().trim();

        console.log(`🎓 Learning concept: "${tagName}" (Input: "${inputTagName}")`);

        // 1. Convert Text -> Vector
        // The model returns a Tensor, we need a plain array
        const output = await this.embedder!(tagName, { pooling: 'mean', normalize: true });
        const vector = Array.from(output.data) as number[];

        // 2. Save to Qdrant
        // We use a Deterministic UUID based on the tagName.
        // This ensures if we "learn" the same tag again, we ID-match and basicallly upsert/overwrite
        // instead of creating a duplicate ghost entry.
        const deterministicId = uuidv5(tagName, TAG_NAMESPACE);

        await this.qdrant.upsert(config.qdrant.collectionName, {
            wait: true,
            points: [
                {
                    id: deterministicId,
                    vector: vector,
                    payload: {
                        original_name: tagName, // Storing normalized name
                        type: "TAG" // <--- Crucial Metadata
                    }
                }
            ]
        });

        console.log(`✅ Learned: ${tagName} (ID: ${deterministicId})`);
    }

    /**
    /**
     * 🔎 GET RAW TAGS: Purely embedding-based tag retrieval
     * Embeds the content and finds the closest tags in the vector space.
     */
    public async getRawTagsByEmbedding(content: string, threshold: number = 0.3, topK: number = 50): Promise<{ name: string, score: number }[]> {
        if (!this.embedder) await this.initialize();

        console.log(`🤔 Getting raw tags...`);

        // Note: Models have token limits. We take the first ~2000 chars 
        // to capture the intro/core definition.
        const summaryText = content.slice(0, 2000);

        const globalOutput = await this.embedder!(summaryText, { pooling: 'mean', normalize: true });
        const globalVector = Array.from(globalOutput.data) as number[];

        const globalSearchResult = await this.qdrant.search(config.qdrant.collectionName, {
            vector: globalVector,
            limit: topK * 2, // Fetch more to account for duplicates
            score_threshold: threshold,
            filter: { must: [{ key: "type", match: { value: "TAG" } }] }
        });

        // Deduplicate by name, keeping highest score
        const uniqueTags = new Map<string, number>();
        for (const hit of globalSearchResult) {
            const name = hit.payload?.original_name as string;
            if (name) {
                if (!uniqueTags.has(name) || hit.score > uniqueTags.get(name)!) {
                    uniqueTags.set(name, hit.score);
                }
            }
        }

        return Array.from(uniqueTags.entries())
            .map(([name, score]) => ({ name, score }))
            .sort((a, b) => b.score - a.score)
            .slice(0, topK);
    }

    /**
     * 🧩 GET REGION TAGS: Chunk-based tag retrieval
     * Splits content into chunks and finds tags for each chunk.
     */
    /**
     * 🧩 GET REGION TAGS: Chunk-based tag retrieval
     * Splits content into chunks and finds tags for each chunk.
     */
    public async getRegionTags(content: string, threshold: number = 0.3): Promise<ContentRegionTags[]> {
        if (!this.embedder) await this.initialize();

        console.log(`🧩 Getting region tags (Semantic Chunking)...`);

        // 1. Smart Semantic Split
        // We use a similarity threshold to decide when to break a chunk.
        // Higher = More sensitive to shifts (smaller chunks). Lower = larger chunks.
        const semanticChunks = await this.semanticTextSplit(content, 0.45);

        const regions: ContentRegionTags[] = [];

        for (const chunkText of semanticChunks) {

            // Embed chunk
            const chunkVector = await this.getVector(chunkText);

            // Search specifically for this chunk
            const chunkResults = await this.qdrant.search(config.qdrant.collectionName, {
                vector: chunkVector,
                limit: 10, // Fetch slightly more to account for duplicates
                score_threshold: threshold,
                filter: { must: [{ key: "type", match: { value: "TAG" } }] }
            });

            // Deduplicate tags for this chunk
            const uniqueChunkTags = new Map<string, number>();
            for (const hit of chunkResults) {
                const name = hit.payload?.original_name as string;
                if (name) {
                    if (!uniqueChunkTags.has(name) || hit.score > uniqueChunkTags.get(name)!) {
                        uniqueChunkTags.set(name, hit.score);
                    }
                }
            }

            const tagScores = Array.from(uniqueChunkTags.entries())
                .map(([name, score]) => ({ name, score }))
                .sort((a, b) => b.score - a.score)
                .slice(0, 5); // Start with top 5 distinct

            if (tagScores.length > 0) {
                // Find fuzzy position
                const match = findFuzzyMatch(content, chunkText, 0);
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
     * Splits text into chunks based on semantic similarity between sentences.
     */
    private async semanticTextSplit(content: string, similarityThreshold: number): Promise<string[]> {
        // 1. Split into sentences (Naive regex, keeping punctuation)
        // Matches non-punctuation followed by punctuation and optional space
        const sentences = content.match(/[^.!?]+[.!?]+(\s+|$)/g) || [content];

        if (sentences.length === 0) return [];

        const chunks: string[] = [];
        let currentChunk: string[] = [sentences[0]];

        let lastSentenceVector = await this.getVector(sentences[0]);

        for (let i = 1; i < sentences.length; i++) {
            const sentence = sentences[i];
            const currentVector = await this.getVector(sentence);

            const sim = cosineSimilarity(lastSentenceVector, currentVector);

            // Log for debugging flow
            // console.log(`Sim: ${sim.toFixed(2)} | "${sentences[i-1].slice(0,10)}..." vs "${sentence.slice(0,10)}..."`);

            if (sim >= similarityThreshold) {
                // Similar topic, grow chunk
                currentChunk.push(sentence);
            } else {
                // Topic shifted! Save old chunk and start new.
                chunks.push(currentChunk.join(""));
                currentChunk = [sentence];
            }

            // We compare against the immediate previous sentence to detect "Shifts"
            lastSentenceVector = currentVector;
        }

        // Add the last chunk
        if (currentChunk.length > 0) {
            chunks.push(currentChunk.join(""));
        }

        return chunks;
    }

    /**
     * Helper to embed a single string.
     */
    private async getVector(text: string): Promise<number[]> {
        if (!this.embedder) await this.initialize();
        const output = await this.embedder!(text, { pooling: 'mean', normalize: true });
        return Array.from(output.data) as number[];
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