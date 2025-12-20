package dev.kbd.vekku_server.services.brain.dto;

/**
 * Represents a single tag and its relevance score.
 */
public record TagScore(String name, double score) {
}
