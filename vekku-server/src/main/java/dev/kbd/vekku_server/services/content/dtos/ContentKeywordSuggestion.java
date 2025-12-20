package dev.kbd.vekku_server.services.content.dtos;

import java.util.UUID;

public record ContentKeywordSuggestion(UUID id,
                UUID contentId,
                String keyword,
                Double score,
                String userId) {

}
