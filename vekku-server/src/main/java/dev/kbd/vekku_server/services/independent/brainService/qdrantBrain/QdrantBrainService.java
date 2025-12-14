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

/**
 * Implementation of the {@link BrainService} that uses Qdrant (via Spring AI)
 * as the underlying engine.
 * <p>
 * This service demonstrates the <b>RAG (Retrieval-Augmented Generation)</b>
 * pattern's retrieval step,
 * even though we are using it for simple classification/suggestion here.
 * </p>
 *
 * <h2>Key Concepts:</h2>
 * <ul>
 * <li><b>VectorStore:</b> The database for embeddings (numbers representing
 * text meaning).</li>
 * <li><b>Embedding:</b> Converting text ("Cooking") into a vector (e.g., [0.1,
 * -0.5, ...]).</li>
 * <li><b>Metadata Filtering:</b> We tag entries with "type=TAG" to distinguish
 * them from other stored docs.</li>
 * </ul>
 */
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
        // Spring AI iterates over the list, calls the EmbeddingClient for each doc,
        // and then pushes the vectors + metadata to Qdrant.
        vectorStore.add(List.of(doc));

        // Use SLF4J logger instead of System.out for proper log management
        log.info("🧠 Brain has learned the concept: {}", tagName);
    }

    @Override
    public Set<String> suggestTags(String content) {
        log.trace("suggestTags({})", content);

        // 1. Setup the Search
        // We want to find the closest matches in the "meaning space".
        // If content is "how to make cake", "Baking" tag should be close.
        SearchRequest request = SearchRequest.builder()
                .query(content)
                // Return top 5 matches to give the user variety
                .topK(5)
                // Similarity threshold (0.0 to 1.0).
                // 0.7+ is usually "very close". 0.3 is "somewhat related".
                // Since user input might be vague, we keep it loose (0.3).
                .similarityThreshold(0.3)
                // Metadata Filter: Ensure we don't suggest a whole Document body as a Tag.
                // This acts like a SQL "WHERE type = 'TAG'" clause.
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
