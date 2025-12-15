declare module 'semantic-chunking' {
    export function chunkit(
        documents: Array<{ document_name?: string; document_text: string; }>,
        options?: {
            onnxEmbeddingModel?: string;
            returnEmbedding?: boolean;
            similarityThreshold?: number;
            logging?: boolean;
            [key: string]: any;
        }
    ): Promise<Array<{
        text: string;
        embedding?: number[] | Float32Array;
        [key: string]: any;
    }>>;
}
