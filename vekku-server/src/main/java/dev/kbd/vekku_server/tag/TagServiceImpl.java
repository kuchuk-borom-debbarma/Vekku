package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.api.ITagService;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;
import dev.kbd.vekku_server.tag.api.TagEvents.TagCreatedEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
class TagServiceImpl implements ITagService {

    final TagRepo tagRepository;
    final TagMapper mapper;
    final ITagEventPublisher eventPublisher;

    @Override
    public TagDTO getTag(String userId, String id) {
        log.info("GetTag of user {} with id {}", userId, id);
        return mapper.toDTO(
            tagRepository
                .findByUserIdAndId(userId, UUID.fromString(id))
                .orElse(null)
        );
    }

    @Override
    public List<TagDTO> getTags(
        String userId,
        String fromCursor,
        int limit,
        String dir
    ) {
        log.info(
            "Get tags of user {} from {} limit {} dir {}",
            userId,
            fromCursor,
            limit,
            dir
        );

        boolean isNext = !"prev".equalsIgnoreCase(dir);
        PageRequest pageable = PageRequest.of(0, limit);

        List<TagEntity> tags;

        if (!StringUtils.hasText(fromCursor)) {
            tags = isNext
                ? tagRepository.findByUserIdOrderByCreatedAtAsc(
                      userId,
                      pageable
                  )
                : tagRepository.findByUserIdOrderByCreatedAtDesc(
                      userId,
                      pageable
                  );
        } else {
            TagEntity cursorEntity = tagRepository
                .findById(UUID.fromString(fromCursor))
                .orElse(null);
            if (cursorEntity == null) {
                return Collections.emptyList();
            }

            if (isNext) {
                tags =
                    tagRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(
                        userId,
                        cursorEntity.getCreatedAt(),
                        pageable
                    );
            } else {
                tags =
                    tagRepository.findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                        userId,
                        cursorEntity.getCreatedAt(),
                        pageable
                    );
            }
        }

        List<TagDTO> tagDTOs = tags.stream().map(mapper::toDTO).toList();

        if (!isNext) {
            Collections.reverse(tagDTOs);
        }

        return tagDTOs;
    }

    @Override
    public TagDTO createTag(
        String userId,
        String tagName,
        Set<String> synonyms
    ) {
        log.info(
            "Create tag for {}, with name {} syns {}",
            userId,
            tagName,
            synonyms.size()
        );
        TagEntity toSave = TagEntity.builder()
            .name(tagName)
            .userId(userId)
            .synonyms(new ArrayList<>(synonyms))
            .build();
        TagEntity saved = tagRepository.save(toSave);
        log.info("Tag created with id {}", saved.getId());

        eventPublisher.publishTagCreated(
            new TagCreatedEvent(
                saved.getId().toString(),
                saved.getName(),
                saved.getUserId()
            )
        );

        return mapper.toDTO(saved);
    }

    @Override
    public TagDTO updateTag(
        String userId,
        String tagId,
        String tagName,
        Set<String> synsToAdd,
        Set<String> synsToRemove
    ) {
        log.info("Update tag for user {} with id {}", userId, tagId);
        TagEntity tagEntity = tagRepository
            .findByUserIdAndId(userId, UUID.fromString(tagId))
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        if (StringUtils.hasText(tagName)) {
            tagEntity.setName(tagName);
        }

        Set<String> currentSynonyms = new HashSet<>(tagEntity.getSynonyms());
        if (synsToAdd != null) {
            currentSynonyms.addAll(synsToAdd);
        }
        if (synsToRemove != null) {
            currentSynonyms.removeAll(synsToRemove);
        }
        tagEntity.setSynonyms(new ArrayList<>(currentSynonyms));

        TagEntity updatedTag = tagRepository.save(tagEntity);
        return mapper.toDTO(updatedTag);
    }

    @Override
    public void deleteTag(String subject, String tagId) {
        log.info("Delete tag for user {} with id {}", subject, tagId);
        TagEntity tagEntity = tagRepository
            .findByUserIdAndId(subject, UUID.fromString(tagId))
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Tag not found or you don't have permission to delete it"
                )
            );
        tagRepository.delete(tagEntity);
    }
}
