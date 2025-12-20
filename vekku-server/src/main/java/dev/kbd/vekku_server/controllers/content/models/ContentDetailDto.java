package dev.kbd.vekku_server.controllers.content.models;

import dev.kbd.vekku_server.services.content.model.Content;
import dev.kbd.vekku_server.services.content.model.ContentTag;
import dev.kbd.vekku_server.services.content.model.ContentTagSuggestion;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContentDetailDto {
    private Content content;
    private List<ContentTag> manualTags;
    private List<ContentTagSuggestion> suggestedTags;
}
