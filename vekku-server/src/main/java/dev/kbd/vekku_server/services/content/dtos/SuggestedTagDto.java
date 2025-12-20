package dev.kbd.vekku_server.services.content.dtos;

import java.util.UUID;

public record SuggestedTagDto(UUID tagId, Double score) {
}
