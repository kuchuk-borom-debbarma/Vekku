package dev.kbd.vekku_server.suggestion;

import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        
        // Store the new content
        // contentId is used as the document ID
        Document document = new Document(contentId, content, Map.of("contentId", contentId));
        vectorStore.add(List.of(document));
        
        // Search for similar content
        List<Document> similarDocs = vectorStore.similaritySearch(
            SearchRequest.query(content).withTopK(count).withSimilarityThreshold(threshold)
        );
        
        Map<String, Double> suggestions = new HashMap<>();
        for (Document doc : similarDocs) {
            // Exclude the document itself from suggestions
            if (!doc.getId().equals(contentId)) {
                // In Spring AI, score is often not directly exposed in Document but filtered by threshold.
                // We'll just mark it present.
                 suggestions.put(doc.getId(), 0.0);
            }
        }
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
