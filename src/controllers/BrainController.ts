import { BrainLogic } from '../services/BrainLogic';

// This matches the gRPC function signature
export const BrainController = {

    Learn: async (call: any, callback: any) => {
        const tagName = call.request.tag_name;

        try {
            // Call the Logic
            const brain = BrainLogic.getInstance();
            await brain.learnTag(tagName);

            // Respond success (Empty object as per proto)
            callback(null, {});
        } catch (error: any) {
            console.error("❌ Error in Learn:", error);
            callback({
                code: 13, // gRPC Internal Error
                details: error.message
            });
        }
    },

    Analyze: (call: any, callback: any) => {
        // ... We will implement this next
        callback(null, { regions: [] });
    }
};