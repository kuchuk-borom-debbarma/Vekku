package dev.kbd.vekku_server.services.brain.model;

import java.util.List;

public record SuggestTagsResponse(
                List<ContentRegionTags> regions,
                ContentRegionTags overallTags) {
}
