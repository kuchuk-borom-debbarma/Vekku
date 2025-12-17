import { pipeline, env, FeatureExtractionPipeline } from '@huggingface/transformers';
import { config } from '../../config';

// Configuration: Force local execution (no remote API calls to HuggingFace)
env.allowLocalModels = false;
env.useBrowserCache = false;

export class EmbeddingService {
    private static instance: EmbeddingService;
    private embedder: FeatureExtractionPipeline | null = null;

    private constructor() { }

    public static getInstance(): EmbeddingService {
        if (!EmbeddingService.instance) {
            EmbeddingService.instance = new EmbeddingService();
        }
        return EmbeddingService.instance;
    }

    public async initialize() {
        if (!this.embedder) {
            console.log(`🧠 Loading AI Model: ${config.ai.modelName}...`);
            this.embedder = (await pipeline('feature-extraction', config.ai.modelName) as unknown) as FeatureExtractionPipeline;
            console.log("✅ Model Loaded!");
        }
    }

    public async getVector(text: string): Promise<number[]> {
        if (!this.embedder) await this.initialize();
        const output = await this.embedder!(text, { pooling: 'mean', normalize: true });
        return Array.from(output.data) as number[];
    }
}
