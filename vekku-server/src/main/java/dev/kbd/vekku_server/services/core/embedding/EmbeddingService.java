package dev.kbd.vekku_server.services.core.embedding;

import dev.kbd.vekku_server.services.brain.dto.TagListDto;
import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
import dev.kbd.vekku_server.services.brain.model.TagScore;

import java.util.List;
import java.util.UUID;

/**
 * 🧩 Embedding Service
 * <p>
 * Core Service for interacting with the AI/Embedding System.
 * Purely responsible for sending data to the AI model/vector DB.
 * Unaware of SQL Entities like 'Tag' objects, deals with primitives/DTOs.
 */
public interface EmbeddingService {

    /**
     * Learns a concept by embedding its synonyms.
     */
    void learnTag(UUID id, String alias, List<String> synonyms);

    /**
     * Deletes a concept from the embedding space.
     */
    void deleteTag(UUID id);

    // Retrieval Methods

    List<TagScore> getRawTagsByEmbedding(String content, Double threshold, Integer topK);

    List<ContentRegionTags> getRegionTags(String content, Double threshold, Integer topK);

    List<TagScore> scoreTags(List<String> tags, String content);

    List<TagScore> extractKeywords(String content, Integer topK, Double diversity);

    // Using primitive/DTO return types to avoid coupling with SQL Entities
    TagListDto getAllTags(Integer limit, String offset);
}
