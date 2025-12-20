package dev.kbd.vekku_server.services.tags.impl;

import org.mapstruct.Mapper;

import dev.kbd.vekku_server.services.tags.dtos.Tag;
import dev.kbd.vekku_server.services.tags.impl.entities.TagEntity;

@Mapper(componentModel = "spring")
public interface TagMapper {

    Tag tagEntityToTag(TagEntity tagEntity);

}
