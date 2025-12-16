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
     * POST /suggest-tags
     * Body: { content: string }
     * Returns: ContentRegionTags[] - Detailed semantic regions with tags
     */
    SuggestTags: async (req: Request, res: Response) => {
        const content = req.body.content;

        if (!content) {
            return res.status(400).json({ error: "content is required" });
        }

        try {
            const brain = BrainLogic.getInstance();
            // Get detailed regions with semantic chunking
            const regions = await brain.suggestTags(content);

            // Return the full structure as requested
            return res.json({ regions });
        } catch (error: unknown) {
            console.error("❌ Error in SuggestTags:", error);
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
    }
};