package dev.kbd.vekku_server.suggestion;

import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionServiceImpl implements ISuggestionService {

    private final VectorStore vectorStore;

    @Override
    public Map<String, Double> createSuggestionsForContent(
        String contentId,
        String content,
        double threshold,
        int count
    ) {
        log.info("Creating suggestions for content: {}", contentId);

        // Search for similar tags (assuming tags are stored in the vector store)
        List<Document> similarDocs = vectorStore.similaritySearch(
            SearchRequest.query(content)
                .withTopK(count)
                .withSimilarityThreshold(threshold)
        );

        Map<String, Double> suggestions = new HashMap<>();
        // Assuming the ID of the similar document corresponds to the Tag ID
        for (Document doc : similarDocs) {
            // Check metadata for score/distance if ScoredDocument is not available
            if (doc.getMetadata().containsKey("distance")) {
                 Object dist = doc.getMetadata().get("distance");
                 if (dist instanceof Number) {
                     suggestions.put(doc.getId(), ((Number) dist).doubleValue());
                 } else {
                     suggestions.put(doc.getId(), 0.0);
                 }
            } else {
                 suggestions.put(doc.getId(), 0.0);
            }
        }

        // Store the suggestions with the contentId as part of the metadata
        // We do not store the content itself, just the suggestions.
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentId", contentId);
        metadata.put("suggestedTags", suggestions);

        // Create a document with empty text (or minimal) as we only care about metadata storage for this ID
        Document suggestionDoc = new Document(contentId, "", metadata);
        vectorStore.add(List.of(suggestionDoc));

        return suggestions;
    }

    @Override
    public Set<String> getKeywords(String content, int count) {
        return Collections.emptySet();
    }

    @Override
    public void deleteSuggestionsOfContent(String id) {
        log.info("Deleting suggestions for content: {}", id);
        try {
            vectorStore.delete(List.of(id));
        } catch (Exception e) {
            log.warn("Error deleting from VectorStore: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Double> getSuggestionsOfContent(
        String contentId,
        String fromCursor,
        int limit,
        String direction,
        String userId
    ) {
        return Collections.emptyMap();
    }
}
