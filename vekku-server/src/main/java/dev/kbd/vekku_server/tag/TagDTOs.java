package dev.kbd.vekku_server.tag;

import java.time.LocalDateTime;
import java.util.Set;

class TagDTOs {

    private TagDTOs() {}

    public record TagDTO(
        String id,
        String name,
        String userId,
        Set<String> synonyms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}
