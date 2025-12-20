package dev.kbd.vekku_server.services.brain.dto;

import java.util.List;

public record TagListDto(List<BrainTag> tags, Object nextOffset) {
}
