package dev.kbd.vekku_server.services.independent.brainService.model;

import java.util.List;

/**
 * Represents a semantic region of text and its associated tags.
 */
public record ContentRegionTags(
                String regionContent,
                int regionStartIndex,
                int regionEndIndex,
                List<TagScore> tagScores,
                List<TagPath> taxonomyPaths) {

        public ContentRegionTags(String regionContent, int regionStartIndex, int regionEndIndex,
                        List<TagScore> tagScores) {
                this(regionContent, regionStartIndex, regionEndIndex, tagScores, java.util.Collections.emptyList());
        }
}
