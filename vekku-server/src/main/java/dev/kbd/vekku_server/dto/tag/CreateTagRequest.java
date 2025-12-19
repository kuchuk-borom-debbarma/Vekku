package dev.kbd.vekku_server.dto.tag;

import java.util.List;

public record CreateTagRequest(String alias, List<String> synonyms) {
}