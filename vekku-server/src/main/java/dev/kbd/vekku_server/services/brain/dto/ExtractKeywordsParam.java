package dev.kbd.vekku_server.services.brain.dto;

public record ExtractKeywordsParam(String content, Integer topK, Double diversity) {
}
