package dev.kbd.vekku_server.services.content.dto;

import dev.kbd.vekku_server.services.content.model.ContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContentRequest {
    private String text;
    private ContentType type;
}
