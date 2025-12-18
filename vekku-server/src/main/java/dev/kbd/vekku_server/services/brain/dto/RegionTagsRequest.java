package dev.kbd.vekku_server.services.brain.dto;

public record RegionTagsRequest(String content, Double threshold, Integer topK) {
}
