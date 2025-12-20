package dev.kbd.vekku_server.services.tags.dtos;

import java.util.List;

public record TagPage(List<Tag> tags, String nextCursor) {
}
