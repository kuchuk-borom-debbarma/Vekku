import dotenv from 'dotenv';
dotenv.config();

export const config = {
    port: process.env.PORT || 3000,
    qdrant: {
        url: process.env.QDRANT_URL || 'http://localhost:6333',
        collectionName: process.env.QDRANT_COLLECTION || 'vekku_brain',
    },
    ai: {
        modelName: process.env.AI_MODEL_NAME || 'Xenova/bge-small-en-v1.5',
    }
};
