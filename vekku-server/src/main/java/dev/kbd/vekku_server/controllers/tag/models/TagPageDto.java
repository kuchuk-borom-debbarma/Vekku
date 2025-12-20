package dev.kbd.vekku_server.controllers.tag.models;

import dev.kbd.vekku_server.services.tags.model.Tag;
import java.util.List;

public record TagPageDto(List<Tag> tags, String nextCursor) {
}
