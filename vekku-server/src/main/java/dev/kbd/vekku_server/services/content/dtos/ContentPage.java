package dev.kbd.vekku_server.services.content.dtos;

import java.util.List;

import dev.kbd.vekku_server.services.content.impl.entities.ContentEntity;

public record ContentPage(List<ContentEntity> content, String nextCursor) {
}
