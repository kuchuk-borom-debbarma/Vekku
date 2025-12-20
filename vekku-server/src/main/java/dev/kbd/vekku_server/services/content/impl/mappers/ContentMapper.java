package dev.kbd.vekku_server.services.content.impl.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import dev.kbd.vekku_server.services.content.dtos.Content;
import dev.kbd.vekku_server.services.content.dtos.ContentKeywordSuggestion;
import dev.kbd.vekku_server.services.content.impl.entities.ContentEntity;
import dev.kbd.vekku_server.services.content.impl.entities.ContentKeywordSuggestionEntity;

@Mapper(componentModel = "spring")
public interface ContentMapper {

    Content toContent(ContentEntity contentEntity);

    @Mapping(target = "contentId", source = "e.content.id")
    ContentKeywordSuggestion toContentKeywordSuggestion(ContentKeywordSuggestionEntity e);
}
