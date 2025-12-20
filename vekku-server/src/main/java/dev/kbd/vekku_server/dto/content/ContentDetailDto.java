package dev.kbd.vekku_server.dto.content;

import dev.kbd.vekku_server.model.content.Content;
import dev.kbd.vekku_server.model.content.ContentTag;
import dev.kbd.vekku_server.model.content.ContentTagSuggestion;
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
