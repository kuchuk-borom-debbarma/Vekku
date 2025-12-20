package dev.kbd.vekku_server.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentProcessingEvent implements Serializable {
    private UUID contentId;
    private Set<ContentProcessingAction> actions;
}
