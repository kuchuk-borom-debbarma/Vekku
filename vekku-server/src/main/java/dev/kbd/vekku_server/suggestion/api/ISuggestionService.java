package dev.kbd.vekku_server.suggestion.api;

import java.util.Map;
import java.util.Set;

public interface ISuggestionService {
    Map<String, Double> createSuggestionsForContent(
        String contentId,
        String content,
        double threshold,
        int count
    );

    Set<String> getKeywords(String content, int count);

    void deleteSuggestionsOfContent(String id);

    Map<String, Double> getSuggestionsOfContent(
        String contentId,
        String fromCursor,
        int limit,
        String direction,
        String userId
    );
}
