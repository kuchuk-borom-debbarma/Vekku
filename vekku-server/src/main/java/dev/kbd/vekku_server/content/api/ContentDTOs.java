package dev.kbd.vekku_server.content.api;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class ContentDTOs {

    public record ContentDTO(
        UUID id,
        String title,
        String content,
        String userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Set<String> tags
    ) {}

    public record CreateContentRequest(
        String title,
        String content,
        ContentType contentType,
        Set<String> tags
    ) {}

    public record UpdateContentRequest(
        String id,
        String updatedTitle,
        String updatedContent,
        ContentType updatedContentType,
        Set<String> toRemoveTags,
        Set<String> toAddTags
    ) {}

    public enum ContentType {
        PLAIN_TEXT,
        MARKDOWN,
    }
}
