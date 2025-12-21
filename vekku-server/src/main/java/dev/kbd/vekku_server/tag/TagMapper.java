package dev.kbd.vekku_server.tag;

import org.mapstruct.Mapper;

import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;

@Mapper(componentModel = "spring")
interface TagMapper {
    TagDTO toTagDTO(Tag tagEntity);
}
