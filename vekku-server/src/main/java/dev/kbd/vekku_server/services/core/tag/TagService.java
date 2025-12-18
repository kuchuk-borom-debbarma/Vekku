package dev.kbd.vekku_server.services.core.tag;

import dev.kbd.vekku_server.model.Tag;
import dev.kbd.vekku_server.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 🏷️ Tag Service (Core)
 * <p>
 * Responsible ONLY for database persistence of Tags.
 * Completely unaware of external AI/Embedding services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    /**
     * Persists a new Tag entity.
     */
    @Transactional
    public Tag createTag(String alias, List<String> synonyms, String userId) {
        if (synonyms == null || synonyms.isEmpty()) {
            synonyms = List.of(alias);
        }
        log.info("Creating tag in DB: {}", alias);

        Tag tag = Tag.builder()
                .name(alias)
                .synonyms(synonyms)
                .userId(userId)
                .build();

        return tagRepository.save(tag);
    }

    /**
     * Updates an existing Tag entity.
     */
    @Transactional
    public Tag updateTag(UUID id, String alias, List<String> synonyms) {
        log.info("Updating tag in DB: {}", id);

        if (synonyms == null || synonyms.isEmpty()) {
            synonyms = List.of(alias);
        }

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        tag.setName(alias);
        tag.setSynonyms(synonyms);
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        log.info("Deleting tag from DB: {}", id);
        // We need to return the tag details or name before delete if Orchestrator needs
        // it,
        // but here we just delete. Orchestrator handles fetching if needed.
        // Actually, Orchestrator might need the name to delete from Brain.
        // So this method might need to return the deleted tag or Orchestrator fetches
        // it first.
        // Let's keep it void for now, Orchestrator fetches first.
        tagRepository.deleteById(id);
    }

    public Tag getTag(UUID id) {
        return tagRepository.findById(id).orElseThrow();
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public dev.kbd.vekku_server.services.core.tag.dto.TagPageDto getTags(Integer limit, String cursor) {
        log.info("Fetching tags with limit: {}, cursor: {}", limit, cursor);
        int fetchLimit = limit + 1; // Fetch one extra to check if there's a next page

        List<Tag> tags;
        if (cursor != null && !cursor.isEmpty()) {
            tags = tagRepository.findByNameGreaterThanOrderByNameAsc(cursor,
                    org.springframework.data.domain.PageRequest.of(0, fetchLimit));
        } else {
            tags = tagRepository.findAllByOrderByNameAsc(org.springframework.data.domain.PageRequest.of(0, fetchLimit));
        }

        String nextCursor = null;
        if (tags.size() > limit) {
            // We have a next page
            tags = tags.subList(0, limit);
            nextCursor = tags.get(tags.size() - 1).getName();
        }

        return new dev.kbd.vekku_server.services.core.tag.dto.TagPageDto(tags, nextCursor);
    }
}
