package dev.kbd.vekku_server.controllers.content.models;

import dev.kbd.vekku_server.services.content.model.Content;
import java.util.List;

public record ContentPageDto(List<Content> content, String nextCursor) {
}
