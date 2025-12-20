package dev.kbd.vekku_server.services.content.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContentParam {
    private String text;
    private ContentType type;
}
