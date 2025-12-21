package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
class ContentServiceImpl implements IContentService {

    final ContentRepo contentRepo;
    final ContentMapper contentMapper;

    @Override
    public ContentDTO createContent(
        String userId,
        CreateContentRequest request
    ) {
        Content toSaveContent = Content.builder()
            .userId(userId)
            .content(request.content())
            .title(request.title())
            .contentType(request.contentType())
            .tags(request.tags())
            .build();
        contentRepo.save(toSaveContent);
        return contentMapper.toDto(toSaveContent);
    }

    @Override
    public ContentDTO updateContent(
        String userId,
        UpdateContentRequest request
    ) {
        var existing = contentRepo
            .findById(UUID.fromString(request.id()))
            .orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException(
                String.format(
                    "Content with id {} not found for user {}",
                    request.id(),
                    userId
                )
            );
        }

        if (StringUtils.hasText(request.updatedTitle())) {
            existing.setTitle(request.updatedTitle());
        }
        if (StringUtils.hasText(request.updatedContent())) {
            existing.setContent(request.updatedContent());
        }
        if (request.updatedContentType() != null) {
            existing.setContentType(request.updatedContentType());
        }

        //tags updates
        if (!request.toRemoveTags().isEmpty()) {
            existing.getTags().removeAll(request.toRemoveTags());
        }
        if (!request.toAddTags().isEmpty()) {
            existing.getTags().addAll(request.toAddTags());
        }

        contentRepo.save(existing);

        return contentMapper.toDto(existing);
    }

    @Override
    public void deleteContent(String id, String userId) {
        //validate userId
        Content existing = contentRepo
            .findById(UUID.fromString(id))
            .orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException(
                String.format(
                    "Content with id {} not found for user {}",
                    id,
                    userId
                )
            );
        }

        //delete
        contentRepo.delete(existing);
    }

    @Override
    public ContentDTO getContentOfUser(String id, String userId) {
        Content content = contentRepo
            .findById(UUID.fromString(id))
            .filter(c -> c.getUserId().equals(userId))
            .orElseThrow(() ->
                new RuntimeException(
                    String.format(
                        "Content with id %s not found for user %s",
                        id,
                        userId
                    )
                )
            );
        return contentMapper.toDto(content);
    }

    @Override
    public List<ContentDTO> getContentsOfUser(
        String userId,
        String cursor,
        int limit,
        String direction
    ) {
        // Default direction to "next" (or DESC) if not specified
        boolean isDirectionPrev = "prev".equalsIgnoreCase(direction);
        Sort sort = Sort.by(
            isDirectionPrev ? Sort.Direction.ASC : Sort.Direction.DESC,
            "createdAt"
        );
        PageRequest pageable = PageRequest.of(0, limit, sort);

        Specification<Content> spec = Specification.where((root, query, cb) ->
            cb.equal(root.get("userId"), userId)
        );

        if (StringUtils.hasText(cursor)) {
            try {
                LocalDateTime cursorDateTime = LocalDateTime.parse(cursor);
                if (isDirectionPrev) {
                    spec = spec.and((root, query, cb) ->
                        cb.greaterThan(root.get("createdAt"), cursorDateTime)
                    );
                } else {
                    spec = spec.and((root, query, cb) ->
                        cb.lessThan(root.get("createdAt"), cursorDateTime)
                    );
                }
            } catch (Exception e) {
                log.error("Invalid cursor format: {}", cursor, e);
                return List.of();
            }
        }

        // This assumes ContentRepo extends JpaSpecificationExecutor<Content>
        List<Content> contentList = contentRepo
            .findAll(spec, pageable)
            .getContent();

        if (isDirectionPrev) {
            // Reverse the list for "prev" to maintain chronological order for the client
            Collections.reverse(contentList);
        }

        return contentList
            .stream()
            .map(contentMapper::toDto)
            .collect(Collectors.toList());
    }
}
