package dev.kbd.vekku_server.services.brain.dto;

import java.util.List;

public record LearnTagRequest(String id, String alias, List<String> synonyms) {
}
