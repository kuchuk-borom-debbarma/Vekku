package dev.kbd.vekku_server.services.core.tag.dto;

import dev.kbd.vekku_server.model.Tag;
import java.util.List;

public record TagPageDto(List<Tag> tags, String nextCursor) {
}
