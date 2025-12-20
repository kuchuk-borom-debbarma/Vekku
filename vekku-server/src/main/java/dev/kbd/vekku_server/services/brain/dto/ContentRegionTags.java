package dev.kbd.vekku_server.services.brain.dto;

import java.util.List;

import dev.kbd.vekku_server.services.common.dtos.TagScore;

/**
 * Represents a semantic region of text and its associated tags.
 */
public record ContentRegionTags(
        String regionContent,
        int regionStartIndex,
        int regionEndIndex,
        List<TagScore> tagScores) {

}
