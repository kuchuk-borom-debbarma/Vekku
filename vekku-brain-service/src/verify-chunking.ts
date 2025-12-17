import { BrainLogic } from './services/brain-logic/BrainLogic';

async function run() {
    const logic = BrainLogic.getInstance();

    console.log("⏳ Initializing Brain...");
    // We need to initialize to load the model
    await logic.initialize();

    // A text with 3 distinct topics: Fruit, Finance, Coding
    const text = "Apples are delicious fruits. They grow on trees and are often red or green. I love eating apple pie. " +
        "The stock market crashed today. Prices plummeted across the board. Investors are panicking about the economy. " +
        "Python is a versatile programming language. It is great for data science and AI. Coding in Python is fun.";

    console.log("\n📝 Input text:", text);
    console.log("-----------------------------------");

    // Access private method using 'any' casting to bypass TS compiler check for verification
    // We use a threshold of 0.5 for clear separation
    const chunks = await (logic as any).semanticTextSplit(text, 0.45);

    console.log(`\n✅ Generated ${chunks.length} chunks:`);
    chunks.forEach((chunk: string, i: number) => {
        console.log(`\n[Chunk ${i + 1}]`);
        console.log(`"${chunk.trim()}"`);
    });
}

run().catch(e => {
    console.error("❌ Error:", e);
    process.exit(1);
});
