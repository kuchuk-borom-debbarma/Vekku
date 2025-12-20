package dev.kbd.vekku_server.services.brain.dto;

import java.util.List;

public record LearnTagParam(String id, String alias, List<String> synonyms) {
}
