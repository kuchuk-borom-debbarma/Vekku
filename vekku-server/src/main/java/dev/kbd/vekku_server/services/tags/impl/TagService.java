package dev.kbd.vekku_server.services.tags.impl;

import dev.kbd.vekku_server.services.tags.ITagService;
import dev.kbd.vekku_server.services.tags.dtos.Tag;
import dev.kbd.vekku_server.services.tags.dtos.TagPage;
import dev.kbd.vekku_server.services.tags.impl.entities.TagEntity;
import dev.kbd.vekku_server.services.tags.impl.repo.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 🏷️ Tag Service (Core)
 * <p>
 * Responsible ONLY for database persistence of Tags.
 * Completely unaware of external AI/Embedding services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TagService implements ITagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    /**
     * Persists a new Tag entity.
     */
    @Override
    @Transactional
    public Tag createTag(String alias, List<String> synonyms, String userId) {
        if (synonyms == null || synonyms.isEmpty()) {
            synonyms = List.of(alias);
        }
        log.info("Creating tag in DB: {}", alias);

        TagEntity tag = TagEntity.builder()
                .name(alias)
                .synonyms(synonyms)
                .userId(userId)
                .build();

        return tagMapper.tagEntityToTag(tagRepository.save(tag));
    }

    /**
     * Updates an existing Tag entity.
     */
    @Override
    @Transactional
    public Tag updateTag(UUID id, String alias, List<String> synonyms, String userId) {
        log.info("Updating tag in DB: {}", id);

        if (synonyms == null || synonyms.isEmpty()) {
            synonyms = List.of(alias);
        }

        TagEntity tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        if (!tag.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to update this tag");
        }

        tag.setName(alias);
        tag.setSynonyms(synonyms);
        return tagMapper.tagEntityToTag(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public void deleteTag(UUID id, String userId) {
        log.info("Deleting tag from DB: {}", id);
        TagEntity tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        if (!tag.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to delete this tag");
        }

        tagRepository.delete(tag);
    }

    @Override
    public Tag getTag(UUID id) {
        return tagMapper
                .tagEntityToTag(tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag not found")));
    }

    @Override
    public TagPage getTags(String userId, Integer limit, String cursor) {
        log.info("Fetching tags for user: {}, limit: {}, cursor: {}", userId, limit, cursor);
        int fetchLimit = limit + 1; // Fetch one extra to check if there's a next page

        List<TagEntity> tags;
        if (cursor != null && !cursor.isEmpty()) {
            tags = tagRepository.findByNameGreaterThanAndUserIdOrderByNameAsc(cursor, userId,
                    org.springframework.data.domain.PageRequest.of(0, fetchLimit));
        } else {
            // Check if this method exists in repo, assuming yes or will fix
            tags = tagRepository.findAllByUserIdOrderByNameAsc(userId,
                    org.springframework.data.domain.PageRequest.of(0, fetchLimit));
        }

        String nextCursor = null;
        if (tags.size() > limit) {
            // We have a next page
            tags = tags.subList(0, limit);
            nextCursor = tags.get(tags.size() - 1).getName();
        }

        return new TagPage(tags.stream().map(tagMapper::tagEntityToTag).collect(Collectors.toList()), nextCursor);
    }

    @Override
    public Optional<Tag> getTagByName(String name, String userId) {
        return tagRepository.findByNameAndUserId(name, userId).map(tagMapper::tagEntityToTag);
    }
}
