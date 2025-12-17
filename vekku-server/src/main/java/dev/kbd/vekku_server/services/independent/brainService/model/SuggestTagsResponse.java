package dev.kbd.vekku_server.services.independent.brainService.model;

import java.util.List;

public record SuggestTagsResponse(
                List<ContentRegionTags> regions,
                ContentRegionTags overallTags) {
}
