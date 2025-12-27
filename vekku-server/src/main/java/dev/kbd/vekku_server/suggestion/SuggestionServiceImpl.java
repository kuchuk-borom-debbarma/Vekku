package dev.kbd.vekku_server.suggestion;

import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
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

        List<Map<String, Object>> suggestions = new ArrayList<>();
        for (Document doc : similarDocs) {
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("tagId", doc.getId());

            Object distance = doc.getMetadata().getOrDefault("distance", 0.0);
            suggestion.put("distance", distance);

            suggestions.add(suggestion);
        }

        // Store the suggestions with the contentId as part of the metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentId", contentId);
        metadata.put("suggestedTags", suggestions);
        metadata.put("type", "SUGGESTION");

        Document suggestionDoc = new Document(contentId, "", metadata);
        vectorStore.add(List.of(suggestionDoc));

        Map<String, Double> result = new HashMap<>();
        for (Map<String, Object> s : suggestions) {
            result.put(
                (String) s.get("tagId"),
                ((Number) s.get("distance")).doubleValue()
            );
        }
        return result;
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

    @Override
    public void saveTag(String tagId, String tagName) {
        log.info("Saving tag with ID: {} and name: {}", tagId, tagName);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tagId", tagId);
        metadata.put("type", "TAG");

        Document document = new Document(tagId, tagName, metadata);
        vectorStore.add(List.of(document));
        log.info("Tag saved to VectorStore: {}", tagId);
    }
}
