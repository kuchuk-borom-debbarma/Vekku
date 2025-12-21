package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.api.TagDTOs.TagContentDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface TagMapper {
    TagDTO toTagDTO(TagEntity tagEntity);
    TagContentDTO toTagContentDTO(TagContentEntity tagContentEntity);
}
