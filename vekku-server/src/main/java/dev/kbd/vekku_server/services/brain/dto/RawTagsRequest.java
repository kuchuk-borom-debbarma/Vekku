package dev.kbd.vekku_server.services.brain.dto;

public record RawTagsRequest(String content, Double threshold, Integer topK) {
}
