package dev.kbd.vekku_server.services.tags.dto;

import java.util.List;

public record CreateTagRequest(String alias, List<String> synonyms) {
}