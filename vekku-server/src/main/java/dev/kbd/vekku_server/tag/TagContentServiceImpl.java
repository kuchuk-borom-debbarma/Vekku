package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import dev.kbd.vekku_server.content.api.IContentService;
import dev.kbd.vekku_server.tag.api.ITagContentService;
import dev.kbd.vekku_server.tag.api.TagDTOs.LinkTagsToContentRequest;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagContentDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.UnlinkTagsFromContentRequest;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class TagContentServiceImpl implements ITagContentService {

    private final IContentService contentService;
    private final TagContentRepo tagContentRepo;
    private final TagRepo tagRepo;
    private final TagMapper tagMapper;

    @Override
    public TagContentDTO getTagContent(String id, String userId) {
        UUID uuid = UUID.fromString(id);
        TagContentEntity entity = tagContentRepo
            .findById(uuid)
            .orElseThrow(() ->
                new RuntimeException("TagContent with id " + id + " not found")
            );

        if (!entity.getUserId().equals(userId)) {
            // TODO: Should be a more specific exception type
            throw new RuntimeException(
                "User " +
                    userId +
                    " does not have permission for TagContent with id " +
                    id
            );
        }

        return tagMapper.toDTO(entity);
    }

    @Override
    public List<TagDTO> getTagsOfContent(
        String contentId,
        String from,
        int limit,
        String direction,
        String subject
    ) {
        // 1. Validate user has access to the content
        ContentDTO content = contentService.getContentOfUser(
            contentId,
            subject
        );
        if (content == null) {
            throw new RuntimeException(
                "Content not found or user does not have permission."
            );
        }

        // 2. Pagination & Filtering logic
        UUID contentUuid = UUID.fromString(contentId);

        Specification<TagContentEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("contentId"), contentUuid));

            if (from != null) {
                try {
                    Instant cursor = Instant.parse(from);
                    if ("next".equalsIgnoreCase(direction)) {
                        predicates.add(
                            cb.greaterThan(root.get("createdAt"), cursor)
                        );
                    } else if ("prev".equalsIgnoreCase(direction)) {
                        predicates.add(
                            cb.lessThan(root.get("createdAt"), cursor)
                        );
                    }
                } catch (Exception e) {
                    log.warn("Invalid cursor format for 'from': {}", from);
                    // Decide if you want to throw an error or ignore the cursor
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction sortDirection = "prev".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(
            0,
            limit,
            Sort.by(sortDirection, "createdAt")
        );

        // 3. Find tag-content links
        Page<TagContentEntity> tagContentPage = tagContentRepo.findAll(
            spec,
            pageable
        );

        // 4. Extract tag IDs
        List<UUID> tagIds = tagContentPage
            .getContent()
            .stream()
            .map(TagContentEntity::getTagId)
            .collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return List.of();
        }

        // 5. Fetch Tag entities
        List<TagEntity> tags = tagRepo.findAllById(tagIds);

        // 6. Map to DTOs and return
        return tagMapper.toDTOs(tags);
    }

    @Override
    public void linkTagsToContent(
        LinkTagsToContentRequest request,
        String userId
    ) {
        // 1. Fetch the content and validate user ownership
        ContentDTO content = contentService.getContentOfUser(
            request.contentId(),
            userId
        );
        if (content == null) {
            throw new RuntimeException(
                "Content not found or user does not have permission."
            );
        }

        UUID contentUuid = UUID.fromString(request.contentId());

        //2. Create the link entities
        List<TagContentEntity> tagsToLink = request
            .tagIdsToLink()
            .stream()
            .map(UUID::fromString)
            .map(tagId ->
                TagContentEntity.builder()
                    .contentId(contentUuid)
                    .tagId(tagId)
                    .userId(userId)
                    .build()
            )
            .collect(Collectors.toList());

        tagContentRepo.saveAll(tagsToLink);
        log.info(
            "Linked {} tags to content {}",
            tagsToLink.size(),
            contentUuid
        );
    }

    @Override
    public void unlinkTagsFromContent(
        UnlinkTagsFromContentRequest request,
        String userId
    ) {
        // 1. Fetch the content and validate user ownership
        ContentDTO content = contentService.getContentOfUser(
            request.contentId(),
            userId
        );
        if (content == null) {
            throw new RuntimeException(
                "Content not found or user does not have permission."
            );
        }

        UUID contentUuid = UUID.fromString(request.contentId());
        Set<UUID> tagUuidsToUnlink = request
            .tagIdsToUnlink()
            .stream()
            .map(UUID::fromString)
            .collect(Collectors.toSet());

        if (tagUuidsToUnlink.isEmpty()) {
            log.warn(
                "No tag IDs provided to unlink for content {}",
                contentUuid
            );
            return;
        }

        // 2. Perform a single, efficient bulk delete operation
        tagContentRepo.deleteLinks(contentUuid, tagUuidsToUnlink);
        log.info(
            "Unlink operation performed for {} tags from content {}",
            tagUuidsToUnlink.size(),
            contentUuid
        );
    }
}
