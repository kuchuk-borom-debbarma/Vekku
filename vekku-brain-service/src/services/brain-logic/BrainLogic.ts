import { v5 as uuidv5 } from 'uuid';
import { ContentRegionTags } from './model';
import { findFuzzyMatch } from '../../utils/textUtils';
import { config } from '../../config';
import { EmbeddingService } from '../core/EmbeddingService';
import { QdrantService } from '../core/QdrantService';
import { TextSplitter } from '../core/TextSplitter';
import { cosineSimilarity } from '../../utils/mathUtils';

// Namespace for Tag UUID generation (Randomly generated constant to ensure uniqueness of our namespace)
const TAG_NAMESPACE = '6ba7b810-9dad-11d1-80b4-00c04fd430c8';

export class BrainLogic {
    private static instance: BrainLogic;
    private embeddingService: EmbeddingService;
    private qdrantService: QdrantService;
    private textSplitter: TextSplitter;

    private constructor() {
        this.embeddingService = EmbeddingService.getInstance();
        this.qdrantService = QdrantService.getInstance();
        this.textSplitter = new TextSplitter();
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
        await this.qdrantService.initialize();
        await this.embeddingService.initialize();
    }

    /**
     * 🧠 LEARN: The Logic from your Javadoc
     * Embeds the tag and saves it with "type=TAG"
     */
    public async learnTag(inputTagName: string): Promise<void> {
        // Normalize to lowercase to ensure "Java" == "java"
        const tagName = inputTagName.toLowerCase().trim();

        console.log(`🎓 Learning concept: "${tagName}" (Input: "${inputTagName}")`);

        // 1. Convert Text -> Vector
        const vector = await this.embeddingService.getVector(tagName);

        // 2. Save to Qdrant
        const deterministicId = uuidv5(tagName, TAG_NAMESPACE);

        await this.qdrantService.upsert([
            {
                id: deterministicId,
                vector: vector,
                payload: {
                    original_name: tagName, // Storing normalized name
                    type: "TAG" // <--- Crucial Metadata
                }
            }
        ]);

        console.log(`✅ Learned: ${tagName} (ID: ${deterministicId})`);
    }

    /**
     * 🔎 GET RAW TAGS: Similarity Search
     */
    public async getRawTagsByEmbedding(content: string, threshold: number = 0.3, topK: number = 50): Promise<{ name: string, score: number }[]> {
        console.log(`🤔 Getting raw tags...`);

        // 1. Summary: First 2000 chars
        const summaryText = content.slice(0, 2000);

        // 2. Embed
        const globalVector = await this.embeddingService.getVector(summaryText);

        // 3. Search
        const globalSearchResult = await this.qdrantService.search(
            globalVector,
            topK * 2,
            threshold,
            { must: [{ key: "type", match: { value: "TAG" } }] }
        );

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
     */
    public async getRegionTags(content: string, threshold: number = 0.3, topK: number = 5): Promise<ContentRegionTags[]> {
        console.log(`🧩 Getting region tags (Semantic Chunking)...`);

        // 1. Smart Semantic Split
        const semanticChunks = await this.textSplitter.split(content, 0.45);

        const regions: ContentRegionTags[] = [];

        for (const chunkText of semanticChunks) {

            // Embed chunk
            const chunkVector = await this.embeddingService.getVector(chunkText);

            // Search specifically for this chunk
            const chunkResults = await this.qdrantService.search(
                chunkVector,
                10,
                threshold,
                { must: [{ key: "type", match: { value: "TAG" } }] }
            );

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
                .slice(0, topK);

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
     * 🧠 SUGGEST TAGS: Combined overall and region tags
     */




    /**
     * ⚖️ SCORE: Evaluates relevance of specific tags against content.
     */
    public async scoreTags(tags: string[], content: string): Promise<{ name: string, score: number }[]> {
        console.log(`⚖️ Scoring ${tags.length} tags against content ("${content.substring(0, 20)}...")`);

        // 1. Embed Content
        const contentVector = await this.embeddingService.getVector(content);

        const results: { name: string, score: number }[] = [];

        // 2. Embed & Score each Tag
        for (const tag of tags) {
            const tagVector = await this.embeddingService.getVector(tag);
            const score = cosineSimilarity(contentVector, tagVector);
            results.push({ name: tag, score });
        }

        return results;
    }
}