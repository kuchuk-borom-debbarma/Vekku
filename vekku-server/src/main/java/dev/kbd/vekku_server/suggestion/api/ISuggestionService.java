package dev.kbd.vekku_server.suggestion.api;

import java.util.Map;
import java.util.Set;

public interface ISuggestionService {
    /**
     * Create the suggestion score of a content.
     */
    Map<String, Double> createSuggestionsForContent(
        String contentId,
        String content,
        double threshold,
        int count
    );

    /**
     * Get the keywords of a content
     * @param content the content to be analyzed
     * @param count the maximum number of keywords to return
     * @return a set of keywords
     */
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
