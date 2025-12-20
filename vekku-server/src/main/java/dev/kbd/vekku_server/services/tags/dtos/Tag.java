package dev.kbd.vekku_server.services.tags.dtos;

import java.util.List;
import java.util.UUID;

public record Tag(UUID id, String name, String userId, List<String> synonyms) {

}
