package dev.kbd.vekku_server.suggestion;

import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import dev.kbd.vekku_server.tag.api.TagEvents.TagCreatedEvent;
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
    public void handleTagCreatedEvent(TagCreatedEvent event) {
        log.info("Received TagCreatedEvent for tag ID: {}, name: {}, userId: {}", event.tagId(), event.tagName(), event.userId());
        // TODO: Implement logic to generate embeddings for the new tag
        generateEmbeddingsForTag(event.tagId(), event.tagName());
    }

    private void generateEmbeddingsForTag(String tagId, String tagName) {
        log.info("Generating embeddings for tag ID: {}, name: {}", tagId, tagName);
        // Create a Document from the tag name
        Map<String, String> metadata = new HashMap<>();
        metadata.put("tagId", tagId);
        metadata.put("type", "tag"); // Distinguish from content suggestions

        Document document = new Document(tagId, tagName, metadata);

        // Add the document to the vector store
        vectorStore.add(List.of(document));
        log.info("Embeddings generated and stored for tag ID: {}", tagId);
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
