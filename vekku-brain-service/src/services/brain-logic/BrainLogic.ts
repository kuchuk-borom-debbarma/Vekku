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
     * 🧠 LEARN: Semantic Tagging (One Vector per Synonym)
     */
    public async learnTag(tagId: string, alias: string, synonyms: string[]): Promise<void> {
        console.log(`🎓 Learning concept: "${alias}" (${synonyms.length} synonyms)`);

        // 1. Clean up old vectors for this Tag ID
        // (This ensures that if we removed a synonym from the list, it's gone from Qdrant)
        await this.qdrantService.deleteByFilter({
            must: [{ key: "tag_id", match: { value: tagId } }]
        });

        // 2. Iterate and Learn
        const points: any[] = [];
        const uniqueSynonyms = new Set(synonyms.map(s => s.toLowerCase().trim()));

        for (const synonym of uniqueSynonyms) {
            // Embed
            const vector = await this.embeddingService.getVector(synonym);
            // Deterministic ID for this point (TagID + Synonym)
            const pointId = uuidv5(tagId + synonym, TAG_NAMESPACE);

            points.push({
                id: pointId,
                vector: vector,
                payload: {
                    tag_id: tagId,
                    alias: alias,
                    original_name: synonym, // for debugging/exact match
                    type: "TAG"
                }
            });
        }

        if (points.length > 0) {
            await this.qdrantService.upsert(points);
        }

        console.log(`✅ Learned "${alias}" with ${points.length} vectors.`);
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

        // Deduplicate by ALIAS, keeping highest score
        const uniqueTags = new Map<string, number>();
        for (const hit of globalSearchResult) {
            const alias = hit.payload?.alias as string;
            // Fallback to original_name if alias missing (backward compat or legacy data)
            const name = alias || (hit.payload?.original_name as string);

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

            // Deduplicate by ALIAS
            const uniqueChunkTags = new Map<string, number>();
            for (const hit of chunkResults) {
                const alias = hit.payload?.alias as string;
                const name = alias || (hit.payload?.original_name as string);

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

    /**
     * 📜 GET ALL TAGS: Paginated list
     */
    public async getAllTags(limit: number = 20, offset?: string | number): Promise<{ tags: any[], nextOffset?: string | number }> {
        const result = await this.qdrantService.scroll(limit, offset, {
            must: [{ key: "type", match: { value: "TAG" } }]
        });

        const tags = result.points.map(point => ({
            id: point.id,
            name: point.payload?.original_name
        }));

        return {
            tags: tags,
            nextOffset: (result.next_page_offset as any) || undefined
        };
    }

    /**
     * 🗑️ DELETE TAG
     */
    public async deleteTag(tagName: string): Promise<void> {
        console.log(`🗑️ Deleting tag by alias: ${tagName}`);
        // We delete by ALIAS filter because we might not have the UUID here in this legacy endpoint
        await this.qdrantService.deleteByFilter({
            must: [{ key: "alias", match: { value: tagName } }]
        });
    }
}