package dev.kbd.vekku_server.services.content.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record Content(UUID id,
        String content,
        ContentType contentType,
        String userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
