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
        } catch (error: any) {
            console.error("❌ Error in Learn:", error);
            return res.status(500).json({ error: error.message });
        }
    },

    /**
     * POST /analyze
     * Body: { text: string }
     */
    Analyze: (req: Request, res: Response) => {
        // ... We will implement this next
        return res.json({ regions: [] });
    },

    /**
     * POST /suggest-tags
     * Body: { content: string }
     */
    SuggestTags: async (req: Request, res: Response) => {
        const content = req.body.content;

        if (!content) {
            return res.status(400).json({ error: "content is required" });
        }

        try {
            const brain = BrainLogic.getInstance();
            const tags = await brain.suggestTags(content);
            return res.json({ tags });
        } catch (error: any) {
            console.error("❌ Error in SuggestTags:", error);
            return res.status(500).json({ error: error.message });
        }
    }
};