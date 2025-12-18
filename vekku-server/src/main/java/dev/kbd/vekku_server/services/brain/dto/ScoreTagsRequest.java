package dev.kbd.vekku_server.services.brain.dto;

import java.util.List;

public record ScoreTagsRequest(List<String> tags, String content) {
}
