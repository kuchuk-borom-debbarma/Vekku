package dev.kbd.vekku_server.events;

import dev.kbd.vekku_server.model.content.Content;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshSuggestionsEvent {
    private Content content;
}
