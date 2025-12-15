import { pipeline, env } from '@xenova/transformers';
import { QdrantClient } from '@qdrant/js-client-rest';
import { v4 as uuidv4 } from 'uuid';

// Configuration: Force local execution (no remote API calls to HuggingFace)
env.allowLocalModels = false;
env.useBrowserCache = false;

export class BrainLogic {
    private static instance: BrainLogic;
    private qdrant: QdrantClient;
    private embedder: any = null;

    // Constants
    private readonly COLLECTION_NAME = "vekku_brain";
    private readonly MODEL_NAME = "Xenova/bge-small-en-v1.5";

    private constructor() {
        // Connect to Qdrant (ensure it's running on localhost:6333)
        this.qdrant = new QdrantClient({ url: 'http://localhost:6333' });
    }

    public static getInstance(): BrainLogic {
        if (!BrainLogic.instance) {
            BrainLogic.instance = new BrainLogic();
        }
        return BrainLogic.instance;
    }

    /**
     * 🚀 INITIALIZE: Ensures DB exists and Model is loaded
     */
    public async initialize() {
        console.log("🔌 Connecting to Qdrant...");

        // 1. Create Collection if missing
        const result = await this.qdrant.getCollections();
        const exists = result.collections.some(c => c.name === this.COLLECTION_NAME);

        if (!exists) {
            console.log(`📦 Creating collection: ${this.COLLECTION_NAME}`);
            await this.qdrant.createCollection(this.COLLECTION_NAME, {
                vectors: { size: 384, distance: 'Cosine' } // BGE-Small is 384 dim
            });
        }

        // 2. Load AI Model (Singleton)
        if (!this.embedder) {
            console.log(`🧠 Loading AI Model: ${this.MODEL_NAME}...`);
            this.embedder = await pipeline('feature-extraction', this.MODEL_NAME);
            console.log("✅ Model Loaded!");
        }
    }

    /**
     * 🧠 LEARN: The Logic from your Javadoc
     * Embeds the tag and saves it with "type=TAG"
     */
    public async learnTag(tagName: string): Promise<void> {
        if (!this.embedder) await this.initialize();

        console.log(`🎓 Learning concept: "${tagName}"`);

        // 1. Convert Text -> Vector
        // The model returns a Tensor, we need a plain array
        const output = await this.embedder(tagName, { pooling: 'mean', normalize: true });
        const vector = Array.from(output.data) as number[];

        // 2. Save to Qdrant
        await this.qdrant.upsert(this.COLLECTION_NAME, {
            wait: true,
            points: [
                {
                    id: uuidv4(), // Generate unique ID
                    vector: vector,
                    payload: {
                        original_name: tagName,
                        type: "TAG" // <--- Crucial Metadata
                    }
                }
            ]
        });

        console.log(`✅ Learned: ${tagName}`);
    }
}