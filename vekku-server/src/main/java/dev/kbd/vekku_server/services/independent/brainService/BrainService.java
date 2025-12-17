package dev.kbd.vekku_server.services.independent.brainService;

import dev.kbd.vekku_server.services.independent.brainService.model.TagScore;
import dev.kbd.vekku_server.services.independent.brainService.model.SuggestTagsResponse;
import java.util.List;

public interface BrainService {
    /**
     * 🧠 LEARN: The Process of Embedding
     *
     * What is "Learning"?
     * We are not just saving the word "Java" into a database.
     * We are converting the CONCEPT of "Java" into a mathematical Vector (a list of
     * numbers)
     * using our AI Model (BGE-Small).
     *
     * Why do this?
     * By storing the vector, we can later find this tag even if the user searches
     * for
     * "Code", "Programming", or "JVM" (words that are semantically similar but
     * spelled differently).
     *
     * The Metadata ("type=TAG"):
     * We label this vector as a 'TAG'. This is crucial because later on, we will
     * also
     * store document content in the same database. This label lets us filter
     * searches
     * to look ONLY for tags.
     */
    void learnTag(String tagName);

    /**
     * 🔎 SUGGEST: Finds tags conceptually related to content.
     * <p>
     * Returns a detailed response containing:
     * 1. Semantic regions with their specific tags
     * 2. Overall/Global tags for the entire content
     * 
     * @param content   The text content to analyze
     * @param threshold Minimum similarity score (0.0 - 1.0)
     * @param topK      Maximum number of tags to retrieve from vector search
     */
    SuggestTagsResponse suggestTags(String content, Double threshold, Integer topK);

    /**
     * ⚖️ SCORE: Evaluates relevance of specific tags against content.
     * <p>
     * Used during the Refinement phase to check if a specific "Child Tag" (which
     * wasn't
     * in the initial search results) is relevant to the text.
     */
    List<TagScore> scoreTags(List<String> tags, String content);
}
