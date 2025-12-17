package dev.kbd.vekku_server.services.brain.model;

import java.util.List;

/**
 * Represents a hierarchical path of tags (e.g. SDE -> Coding -> Java).
 * Used for poly-hierarchy context.
 */
public record TagPath(List<TagScore> path, double finalScore) {
}
