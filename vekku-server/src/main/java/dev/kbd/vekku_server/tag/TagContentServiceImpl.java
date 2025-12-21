package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import dev.kbd.vekku_server.content.api.IContentService;
import dev.kbd.vekku_server.tag.api.ITagContentService;
import dev.kbd.vekku_server.tag.api.TagDTOs.LinkTagsToContentRequest;
import dev.kbd.vekku_server.tag.api.TagDTOs.UnlinkTagsFromContentRequest;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class TagContentServiceImpl implements ITagContentService {

    private final IContentService contentService;
    private final TagContentRepo tagContentRepo;

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
