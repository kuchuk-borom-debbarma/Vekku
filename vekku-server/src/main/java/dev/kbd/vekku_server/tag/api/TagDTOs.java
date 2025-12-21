package dev.kbd.vekku_server.tag.api;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class TagDTOs {

    private TagDTOs() {}

    public record TagDTO(
        String id,
        String name,
        String userId,
        Set<String> synonyms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record TagContentDTO(
        String id,
        UUID tagId,
        UUID contentId,
        String userId
    ) {}

    public record LinkTagsToContentRequest(
        Set<String> tagIdsToLink,
        String contentId
    ) {}

    public record UnlinkTagsFromContentRequest(
        Set<String> tagIdsToUnlink,
        String contentId
    ) {}

    public record CreateTagRequest(String tagName, Set<String> synonyms) {}

    public record UpdateTagRequest(
        String tagId,
        String tagName,
        Set<String> synsToRemove,
        Set<String> synsToAdd
    ) {}
}
