package dev.kbd.vekku_server.services.brain.dto;

public record ExtractKeywordsRequest(String content, Integer topK, Double diversity) {
}
