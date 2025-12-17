import { QdrantClient } from '@qdrant/js-client-rest';
import { config } from '../../config';

export class QdrantService {
    private static instance: QdrantService;
    private client: QdrantClient;

    private constructor() {
        this.client = new QdrantClient({ url: config.qdrant.url });
    }

    public static getInstance(): QdrantService {
        if (!QdrantService.instance) {
            QdrantService.instance = new QdrantService();
        }
        return QdrantService.instance;
    }

    public async initialize() {
        console.log("🔌 Connecting to Qdrant...");
        const result = await this.client.getCollections();
        const exists = result.collections.some(c => c.name === config.qdrant.collectionName);

        if (!exists) {
            console.log(`📦 Creating collection: ${config.qdrant.collectionName}`);
            await this.client.createCollection(config.qdrant.collectionName, {
                vectors: { size: 384, distance: 'Cosine' } // BGE-Small is 384 dim
            });
        }
    }

    public async upsert(points: any[]) {
        await this.client.upsert(config.qdrant.collectionName, {
            wait: true,
            points: points
        });
    }

    public async search(vector: number[], limit: number, scoreThreshold: number, filter?: any) {
        return await this.client.search(config.qdrant.collectionName, {
            vector: vector,
            limit: limit,
            score_threshold: scoreThreshold,
            filter: filter
        });
    }
}
