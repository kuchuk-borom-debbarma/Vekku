package dev.kbd.vekku_server.services.content.dtos;

import java.util.List;

public record ContentPage(List<Content> content, String nextCursor) {
}
