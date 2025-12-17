import { Request, Response } from 'express';
import { BrainLogic } from '../services/brain-logic/BrainLogic';

// Express Controller
export const BrainController = {

    /**
     * POST /learn
     * Body: { tag_name: string }
     */
    Learn: async (req: Request, res: Response) => {
        const tagName = req.body.tag_name;

        if (!tagName) {
            return res.status(400).json({ error: "tag_name is required" });
        }

        try {
            // Call the Logic
            const brain = BrainLogic.getInstance();
            await brain.learnTag(tagName);

            // Respond success
            return res.json({});
        } catch (error: unknown) {
            console.error("❌ Error in Learn:", error);
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            return res.status(500).json({ error: errorMessage });
        }
    },

    /**
     * POST /raw-tags
     * Body: { content: string, threshold?: number, topK?: number }
     * Returns: { name: string, score: number }[]
     */
    GetRawTags: async (req: Request, res: Response) => {
        const { content, threshold, topK } = req.body;

        if (!content) {
            return res.status(400).json({ error: "content is required" });
        }

        try {
            const brain = BrainLogic.getInstance();
            // Get raw tags from embedding
            const result = await brain.getRawTagsByEmbedding(content, threshold, topK);

            return res.json({ tags: result });
        } catch (error: unknown) {
            console.error("❌ Error in GetRawTags:", error);
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            return res.status(500).json({ error: errorMessage });
        }
    },

    /**
     * POST /region-tags
     * Body: { content: string, threshold?: number }
     * Returns: ContentRegionTags[]
     */
    GetRegionTags: async (req: Request, res: Response) => {
        const { content, threshold, topK } = req.body;

        if (!content) {
            return res.status(400).json({ error: "content is required" });
        }

        try {
            const brain = BrainLogic.getInstance();
            const result = await brain.getRegionTags(content, threshold, topK);

            return res.json({ regions: result });
        } catch (error: unknown) {
            console.error("❌ Error in GetRegionTags:", error);
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            return res.status(500).json({ error: errorMessage });
        }
    },

    /**
     * POST /score-tags
     * Body: { tags: string[], content: string }
     * Returns: { scores: [{ name: string, score: number }] }
     */
    ScoreTags: async (req: Request, res: Response) => {
        const { tags, content } = req.body;

        if (!tags || !content) {
            return res.status(400).json({ error: "tags and content are required" });
        }

        try {
            const brain = BrainLogic.getInstance();
            const scores = await brain.scoreTags(tags, content);
            return res.json({ scores });
        } catch (error: unknown) {
            console.error("❌ Error in ScoreTags:", error);
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            return res.status(500).json({ error: errorMessage });
        }
    },
    /**
     * POST /suggest-combined
     * Body: { content: string, threshold?: number, topK?: number }
     * Returns: { tags: [{ name: string, score: number }] }
     */
    SuggestCombinedTags: async (req: Request, res: Response) => {
        const { content, threshold, topK } = req.body;

        if (!content) {
            return res.status(400).json({ error: "content is required" });
        }

        try {
            const brain = BrainLogic.getInstance();
            const result = await brain.suggestOverallRegionCombinedTags(content, threshold, topK);
            // Return wrapped in 'tags' for consistency with GetRawTags
            return res.json({ tags: result });
        } catch (error: unknown) {
            console.error("❌ Error in SuggestCombinedTags:", error);
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            return res.status(500).json({ error: errorMessage });
        }
    }
};