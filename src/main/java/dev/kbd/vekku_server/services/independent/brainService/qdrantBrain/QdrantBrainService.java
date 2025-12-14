package dev.kbd.vekku_server.services.independent.brainService.qdrantBrain;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class QdrantBrainService implements BrainService {
    final VectorStore vectorStore;

    @Override
    public void learnTag(String tagName) {
        log.trace("learnTag({})", tagName);

        // 1. Create Metadata
        // We add "type=TAG" so later we can say "Search only TAGs, not Documents"
        Map<String, Object> metadata = Map.of(
                "type", "TAG",
                "original_name", tagName);

        // 2. Wrap it in a "Document" object
        // This is what Spring AI uses to hold Text + Metadata
        Document doc = new Document(tagName, metadata);

        // 3. Save to Qdrant
        // The Embedding Model (BGE-Small) runs automatically in the background here!
        vectorStore.add(List.of(doc));

        System.out.println("🧠 Brain has learned the concept: " + tagName);
    }

    @Override
    public Set<String> suggestTags(String content) {
        log.trace("suggestTags({})", content);

        // 1. Setup the Search
        SearchRequest request = SearchRequest.builder()
                .query(content)
                .topK(3)
                .similarityThreshold(0.6)
                .filterExpression("type == 'TAG'")
                .build();

        // 2. Ask Qdrant (Magic happens here)
        /*
         * 🔍 UNDER THE HOOD: This single line performs a TWO-STEP relay:
         *
         * STEP A (The Translator):
         * First, it calls the EmbeddingClient. It takes the query string
         * ("how to bake")
         * and runs it through the local SBERT ONNX model.
         * Result: A `float[384]` array (The Vector).
         *
         * STEP B (The Librarian):
         * Then, it sends that `float[]` array to Qdrant over the network.
         * Qdrant compares it against millions of stored vectors using HNSW
         * (Hierarchical Navigable Small World)
         * graphs to find the nearest neighbors instantly.
         */
        List<Document> similarDocs = vectorStore.similaritySearch(request);

        // 3. Return names
        return similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.toSet());
    }
}
