package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.api.TagDTOs.TagContentDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    TagDTO toDTO(TagEntity tagEntity);

    List<TagDTO> toDTOs(List<TagEntity> tagEntities);

    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(
        source = "tagId",
        target = "tagId",
        qualifiedByName = "uuidToString"
    )
    @Mapping(
        source = "contentId",
        target = "contentId",
        qualifiedByName = "uuidToString"
    )
    TagContentDTO toDTO(TagContentEntity tagContentEntity);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }
}
