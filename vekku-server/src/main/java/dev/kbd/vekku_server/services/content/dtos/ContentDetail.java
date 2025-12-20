package dev.kbd.vekku_server.services.content.dtos;

import dev.kbd.vekku_server.services.content.impl.entities.ContentTagEntity;
import dev.kbd.vekku_server.services.content.impl.entities.ContentTagSuggestionEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContentDetail {
    private Content content;
    private List<ContentTagEntity> manualTags;
    private List<ContentTagSuggestionEntity> suggestedTags;
}
