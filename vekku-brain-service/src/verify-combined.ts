import { BrainLogic } from './services/brain-logic/BrainLogic';

async function main() {
    try {
        console.log("⏳ Initializing Brain for Combined Tags Verification...");
        const brain = BrainLogic.getInstance();
        await brain.initialize();

        const content = `
        Java is a high-level, class-based, object-oriented programming language that is designed to have
        as few implementation dependencies as possible. It is a general-purpose programming language intended
        to let application developers write once, run anywhere (WORA), meaning that compiled Java code can run
        on all platforms that support Java without the need for recompilation.

        Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".
        We take an opinionated view of the Spring platform and third-party libraries so you can get started with minimum fuss.
        Most Spring Boot applications need little Spring configuration.
        `;

        console.log("🧠 Testing suggestOverallRegionCombinedTags...");
        const results = await brain.suggestOverallRegionCombinedTags(content);

        console.log("\n✅ Combined Tags Results:");
        results.forEach(tag => {
            console.log(`- ${tag.name} (Score: ${tag.score.toFixed(4)})`);
        });

        if (results.length > 0) {
            console.log("\n✅ SUCCESS: Returned combined tags.");
        } else {
            console.error("\n❌ FAILURE: No tags returned.");
            process.exit(1);
        }

    } catch (error) {
        console.error("❌ Error:", error);
        process.exit(1);
    }
}

main();
