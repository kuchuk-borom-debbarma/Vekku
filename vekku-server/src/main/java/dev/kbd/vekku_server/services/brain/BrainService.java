package dev.kbd.vekku_server.services.brain;

import dev.kbd.vekku_server.services.brain.model.TagScore;
import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
import java.util.List;

public interface BrainService {
    /**
     * 🧠 LEARN: The Process of Embedding
     *
     * Converts a Tag string into a semantic Vector (embedding) using the AI Model.
     * This allows retrieval by semantic similarity later.
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
     */
    List<TagScore> scoreTags(List<String> tags, String content);

    /**
     * 📜 GET ALL TAGS: Paginated list
     */
    TagListDto getAllTags(Integer limit, String offset);

    /**
     * 🗑️ DELETE TAG
     */
    void deleteTag(String tagName);

    record TagListDto(java.util.List<TagScoreDto> tags, String nextOffset) {
    }

    record TagScoreDto(String id, String name) {
    }
}
