import { EmbeddingService } from './EmbeddingService';
import { cosineSimilarity } from '../../utils/mathUtils';

export class TextSplitter {
    private embeddingService: EmbeddingService;

    constructor() {
        this.embeddingService = EmbeddingService.getInstance();
    }

    /**
     * 🧠 SEMANTIC TEXT SPLITTER
     * Splits text into chunks by checking semantic similarity between sentences.
     */
    public async split(content: string, similarityThreshold: number): Promise<string[]> {
        // 1. Split into "sentences" preserving formatting
        const rawSen = content.split(/(?<=[.!?])\s+|\n+/);

        // Filter out empty lines
        const sentences = rawSen.map(s => s.trim()).filter(s => s.length > 0);

        if (sentences.length === 0) return [];

        const chunks: string[] = [];
        let currentChunk: string[] = [sentences[0]];

        let lastSentenceVector = await this.embeddingService.getVector(sentences[0]);

        for (let i = 1; i < sentences.length; i++) {
            const sentence = sentences[i];
            const currentVector = await this.embeddingService.getVector(sentence);

            const sim = cosineSimilarity(lastSentenceVector, currentVector);

            if (sim >= similarityThreshold) {
                // Similar topic, grow chunk.
                currentChunk.push(sentence);
            } else {
                // Topic shifted!
                chunks.push(currentChunk.join(" "));
                currentChunk = [sentence];
            }

            lastSentenceVector = currentVector;
        }

        if (currentChunk.length > 0) {
            chunks.push(currentChunk.join(" "));
        }

        return chunks;
    }
}
