package dev.kbd.vekku_server.services.independent.brainService;

import dev.kbd.vekku_server.services.independent.brainService.model.ContentRegionTags;
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
    public void learnTag(String tagName);

    /**
     * 🔎 SUGGEST: Finds tags conceptually related to content.
     * <p>
     * Returns a list of semantic regions, each with its own specific tags.
     * This allows for granular tagging of long content.
     */
    public List<ContentRegionTags> suggestTags(String content);
}
