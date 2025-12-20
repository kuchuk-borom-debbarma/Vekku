package dev.kbd.vekku_server.controllers.tag.models;

import java.util.List;

public record CreateTagRequest(String alias, List<String> synonyms) {
}