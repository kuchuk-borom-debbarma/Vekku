package dev.kbd.vekku_server.dto.content;

import dev.kbd.vekku_server.model.content.Content;
import java.util.List;

public record ContentPageDto(List<Content> content, String nextCursor) {
}
