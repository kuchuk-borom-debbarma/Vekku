package dev.kbd.vekku_server.services.brain.dto;

import java.util.List;

/**
 * Represents a semantic region of text and its associated tags.
 */
public record ContentRegionTags(
        String regionContent,
        int regionStartIndex,
        int regionEndIndex,
        List<TagScore> tagScores) {

}
