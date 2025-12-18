package dev.kbd.vekku_server.services.brain.dto;

import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
import java.util.List;

public record RegionTagsResponse(List<ContentRegionTags> regions) {
}
