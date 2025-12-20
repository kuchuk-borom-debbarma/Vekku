package dev.kbd.vekku_server.services.brain.dto;

import dev.kbd.vekku_server.services.brain.model.TagScore;
import java.util.List;

public record ExtractKeywordsResponse(List<TagScore> keywords) {
}
