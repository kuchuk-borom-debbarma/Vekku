import { Request, Response } from 'express';
import { BrainLogic } from '../services/brain-logic/BrainLogic';

// Express Controller
export const BrainController = {

    /**
     * POST /learn
     * Body: { id: string, alias: string, synonyms: string[] }
     */
    Learn: async (req: Request, res: Response) => {
        const { id, alias, synonyms } = req.body;

        if (!id || !alias || !Array.isArray(synonyms)) {
            return res.status(400).json({ error: "id, alias, and synonyms (array) are required" });
        }

        try {
            // Call the Logic
            const brain = BrainLogic.getInstance();
            await brain.learnTag(id, alias, synonyms);

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
     * GET /tags?limit=20&offset=x
     */
    GetAllTags: async (req: Request, res: Response) => {
        const limit = parseInt(req.query.limit as string) || 20;
        const offset = req.query.offset as string | undefined;

        try {
            const brain = BrainLogic.getInstance();
            const result = await brain.getAllTags(limit, offset);
            return res.json(result);
        } catch (error: unknown) {
            console.error("❌ Error in GetAllTags:", error);
            return res.status(500).json({ error: "Failed to fetch tags" });
        }
    },

    /**
     * DELETE /tags/:name
     */
    DeleteTag: async (req: Request, res: Response) => {
        const tagName = req.params.name;

        if (!tagName) {
            return res.status(400).json({ error: "Tag name required" });
        }

        try {
            const brain = BrainLogic.getInstance();
            await brain.deleteTag(tagName);
            return res.json({ success: true });
        } catch (error: unknown) {
            console.error("❌ Error in DeleteTag:", error);
            return res.status(500).json({ error: "Failed to delete tag" });
        }
    }

};