package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface ContentMapper {
    ContentDTO toDto(Content content);
}
