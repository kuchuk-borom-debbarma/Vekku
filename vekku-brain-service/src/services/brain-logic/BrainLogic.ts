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
     * 
     * This method:
     * 1. Connects to Qdrant Vector Database.
     * 2. Ensures the 'vekku' collection exists with correct vector size (384).
     * 3. Loads the ONNX embedding model (BGE-Small-EN-V1.5) into memory.
     */
    public async initialize() {
        await this.qdrantService.initialize();
        await this.embeddingService.initialize();
    }

    /**
     * 🧠 LEARN: Semantic Tagging (One Vector per Synonym)
     * 
     * Concept:
     * A "Tag" in the Brain is not just a single point, but a cloud of points representing
     * its Synonyms. This allows for precise matching.
     * 
     * Workflow:
     * 1. **Clean Slate**: Deletes ALL existing points for this `tagId` to handle updates/removals.
     * 2. **Embed**: Generates a 384-dimensional vector for *each unique synonym*.
     * 3. **Store**: Saves each synonym as a distinct Point in Qdrant with payload metadata.
     * 
     * @param tagId - Stable UUID from the Database (Tag Entity ID)
     * @param alias - Display Name (e.g. "Artificial Intelligence")
     * @param synonyms - List of triggers (e.g. ["AI", "Machine Learning", "LLM"])
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
     * 
     * Uses Vector Similarity to find concepts related to the input content.
     * 
     * Logic:
     * 1. **Embed**: Vectors the first 2000 chars of content (summary) (Skipped).
     * 2. **Search**: Finds nearest neighbor points (synonyms) in Qdrant.
     * 3. **Aggregation (Max-Score)**:
     *    - If multiple synonyms for the SAME tag appear (e.g., "JS" and "JavaScript"),
     *      we take the *highest score* to represent the tag.
     *    - We group by `alias` (or `original_name` fallback).
     * 
     * @param content - Text content to tag
     * @param threshold - Minimum similarity score (0.0 - 1.0)
     * @param topK - Max number of unique tags to return
     */
    public async getRawTagsByEmbedding(content: string, threshold: number = 0.3, topK: number = 50): Promise<{ name: string, score: number }[]> {
        console.log(`🤔 Getting raw tags...`);


        // 2. Embed
        const globalVector = await this.embeddingService.getVector(content);

        // 3. Search
        const globalSearchResult = await this.qdrantService.search(
            globalVector,
            topK * 2,
            threshold,
            { must: [{ key: "type", match: { value: "TAG" } }] }
        );

        // Group by ALIAS, keeping highest score
        const uniqueTags = new Map<string, number>();
        for (const hit of globalSearchResult) {
            const alias = hit.payload?.alias as string;

            if (alias) {
                if (!uniqueTags.has(alias) || hit.score > uniqueTags.get(alias)!) {
                    uniqueTags.set(alias, hit.score);
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
     * 
     * Ideal for long documents. This method breaks the text into semantically coherent blocks
     * and tags each block individually.
     * 
     * Workflow:
     * 1. **Semantic Split**: Uses `TextSplitter` to divide text based on embedding similarity gaps (topics).
     * 2. **Local Search**: vectors and searches for tags for *each chunk*.
     * 3. **Region Mapping**: Maps the tags back to the specific character range in the original text.
     * 
     * @returns List of Regions, each with its own set of TagScores.
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
     * 
     * Unlike `getRawTagsByEmbedding` which *discovers* tags, this method *validates* them.
     * It explicitly checks: "How relevant is Tag X to this Content?"
     * 
     * Used for:
     * - Checking if a user-applied tag is actually relevant.
     * - Filtering a fixed list of categories.
     * 
     * @param tags - List of arbitrary strings (Tag Names) to test.
     * @param content - The text to score against.
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
    public async deleteTag(tagId: string): Promise<void> {
        console.log(`🗑️ Deleting tag by ID: ${tagId}`);
        await this.qdrantService.deleteByFilter({
            must: [{ key: "tag_id", match: { value: tagId } }]
        });
    }
}