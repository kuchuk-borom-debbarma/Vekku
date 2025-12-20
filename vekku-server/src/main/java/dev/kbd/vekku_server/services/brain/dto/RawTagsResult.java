package dev.kbd.vekku_server.services.brain.dto;

import java.util.List;

import dev.kbd.vekku_server.services.common.dtos.TagScore;

public record RawTagsResult(List<TagScore> tags) {
}
