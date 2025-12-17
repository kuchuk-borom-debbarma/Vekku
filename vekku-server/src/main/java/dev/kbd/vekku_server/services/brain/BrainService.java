package dev.kbd.vekku_server.services.brain;

import dev.kbd.vekku_server.services.brain.model.TagScore;
import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
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
     * 🔎 GET RAW TAGS: Purely embedding-based tag retrieval
     */
    List<TagScore> getRawTagsByEmbedding(String content, Double threshold, Integer topK);

    /**
     * 🧩 GET REGION TAGS: Chunk-based tag retrieval
     */
    List<ContentRegionTags> getRegionTags(String content, Double threshold, Integer topK);

    /**
     * ⚖️ SCORE: Evaluates relevance of specific tags against content.
     * <p>
     * Used during the Refinement phase to check if a specific "Child Tag" (which
     * wasn't
     * in the initial search results) is relevant to the text.
     */
    List<TagScore> scoreTags(List<String> tags, String content);

    /**
     * 🧠 SUGGEST COMBINED: Overall + Regional Tags (Deduplicated)
     */
    List<TagScore> suggestCombinedTags(String content, Double threshold, Integer topK);
}
