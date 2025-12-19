package dev.kbd.vekku_server.services.orchestration;

import dev.kbd.vekku_server.model.Tag;
import dev.kbd.vekku_server.services.core.embedding.EmbeddingService;
import dev.kbd.vekku_server.services.core.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 🎻 Tag Orchestrator
 * <p>
 * Coordinates workflows that span across multiple independent core services.
 * Specifically handles the synchronization between the Database (TagService)
 * and
 * the AI Brain (EmbeddingService).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TagOrchestrator {

    private final TagService tagService;
    private final EmbeddingService embeddingService;

    /**
     * Orchestrates the creation of a tag.
     * 1. Persist to DB.
     * 2. Push to AI Brain.
     */
    @Transactional
    public Tag createTag(String alias, List<String> synonyms, String userId) {
        log.info("Orchestrating creation of tag: {}", alias);

        // 1. DB Operation
        Tag tag = tagService.createTag(alias, synonyms, userId);

        // 2. AI Operation
        // We use the ID from the persisted tag
        embeddingService.learnTag(tag.getId(), tag.getName(), tag.getSynonyms());

        return tag;
    }

    /**
     * Orchestrates the update of a tag.
     * 1. Update DB.
     * 2. Re-learn in AI Brain.
     */
    @Transactional
    public Tag updateTag(UUID id, String alias, List<String> synonyms, String userId) {
        log.info("Orchestrating update of tag: {}", id);

        // 1. DB Operation
        Tag tag = tagService.updateTag(id, alias, synonyms, userId);

        // 2. AI Operation
        embeddingService.learnTag(tag.getId(), tag.getName(), tag.getSynonyms());

        return tag;
    }

    /**
     * Orchestrates the deletion of a tag.
     * 1. Fetch Tag (to get name for Brain).
     * 2. Delete from DB.
     * 3. Delete from AI Brain.
     */
    @Transactional
    public void deleteTag(UUID id, String userId) {
        log.info("Orchestrating deletion of tag: {}", id);

        // Need to fetch name before deletion for the legacy Brain API (which deletes by
        // name)
        Tag tag = tagService.getTag(id);

        // 1. DB Operation
        tagService.deleteTag(id, userId);

        // 2. AI Operation
        embeddingService.deleteTag(tag.getId());
    }
}
