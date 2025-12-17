
import { BrainLogic } from './src/services/brain-logic/BrainLogic';
import { env } from '@huggingface/transformers';

// Force local
env.allowLocalModels = false;
env.useBrowserCache = false;

async function verifyOverallTags() {
    console.log("🚀 Starting Verification for Overall Tags...");

    // Simulate content about two distinct topics: Space and Cooking
    const text = `
    The NASA Mars rover has detected new rock formations. Space exploration is crucial for understanding our universe. 
    Rocket propulsion systems are becoming more efficient. Astronauts are training for long-duration missions.
    
    On a different note, making the perfect pasta requires boiling water with plenty of salt. 
    Italian cuisine relies on fresh ingredients like tomatoes and basil. Cooking is an art form that brings people together.
    `;

    const brain = BrainLogic.getInstance();

    // Pre-teach some tags to ensure we have matches
    await brain.learnTag("Space");
    await brain.learnTag("Mars");
    await brain.learnTag("Cooking");
    await brain.learnTag("Pasta");
    await brain.learnTag("Java"); // Control tag, shouldn't appear

    console.log("🔎 Asking for suggestions...");
    const result = await brain.suggestTags(text, 0.4, 10);

    console.log("\n--- REGIONS ---");
    result.regions.forEach((r, i) => {
        console.log(`[Region ${i}] "${r.regionContent.substring(0, 30)}..." -> Tags: ${r.tagScores.map(t => t.name).join(", ")}`);
    });

    console.log("\n--- OVERALL TAGS (Global) ---");
    result.overallTags.forEach(t => {
        console.log(`Tag: ${t.name}, Score: ${t.score.toFixed(4)}`);
    });

    // Simple assertions
    const hasSpace = result.overallTags.some(t => t.name === "Space");
    const hasCooking = result.overallTags.some(t => t.name === "Cooking");

    if (hasSpace && hasCooking) {
        console.log("\n✅ SUCCESS: Both topics detected in overall tags.");
    } else {
        console.error("\n❌ FAILURE: Missing expected topics.");
    }
}

verifyOverallTags();
