
// @ts-ignore
import { chunkit } from 'semantic-chunking';
import { env } from '@huggingface/transformers';

// Force local
env.allowLocalModels = false;
env.useBrowserCache = false;

async function testChunking() {
    let text = "I spent the morning debugging my compiler error which was very frustrating but then I took a break to water my roses and plant some new tulips in the backyard";

    // Heuristic: Inject periods before conjunctions if missing
    // This forces sentence-parse to split them
    text = text.replace(/(\s+)(but|and|then|however|while)(\s+)/gi, "$1$2.$3");
    // Also ensuring normal punctuation splits are respected if present

    console.log("Testing chunking on (processed):", text);

    const chunks = await chunkit(
        [{ document_name: 'input', document_text: text }],
        {
            onnxEmbeddingModel: 'Xenova/bge-small-en-v1.5',
            returnEmbedding: false,
            similarityThreshold: 0.6,
            dynamicThresholdLowerBound: 0.6,
            dynamicThresholdUpperBound: 0.6,
            sentenceSplitter: {
                splitter: "regex",
                splitterPattern: /[\.!\?;]+|\s+(?:but|and|then|however|while)\s+/gi
            },
            logging: true
        }
    );

    console.log("Chunks found:", chunks.length);
    chunks.forEach((c: any, i: any) => {
        console.log(`[${i}] ${c.text}`);
    });
}

testChunking();
