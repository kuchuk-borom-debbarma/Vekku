package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.TagDTOs.TagDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagDTO toTagDTO(TagEntity tagEntity);
}
