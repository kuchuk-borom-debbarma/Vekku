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
}
