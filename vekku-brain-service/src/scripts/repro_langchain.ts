
import { RecursiveCharacterTextSplitter } from "@langchain/textsplitters";

async function testLangChainSplit() {
    const text = "I spent the morning debugging my compiler error which was very frustrating but then I took a break to water my roses and plant some new tulips in the backyard";

    console.log("Testing LangChain Recursive Splitter...");

    const splitter = new RecursiveCharacterTextSplitter({
        separators: [".", "!", "?", " but ", " and ", " then ", " "], // Custom separators for semantic pauses
        chunkSize: 50, // Force small chunks for demo
        chunkOverlap: 10,
    });

    const output = await splitter.createDocuments([text]);

    console.log(`Created ${output.length} chunks:`);
    output.forEach((doc, i) => {
        console.log(`[${i}] ${doc.pageContent}`);
    });
}

testLangChainSplit();
