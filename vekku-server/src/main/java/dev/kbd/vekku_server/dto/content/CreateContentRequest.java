package dev.kbd.vekku_server.dto.content;

import dev.kbd.vekku_server.model.content.ContentType;
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
