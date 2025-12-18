package dev.kbd.vekku_server.dto;

import dev.kbd.vekku_server.model.Docs;
import dev.kbd.vekku_server.model.DocsTag;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

public class DocDto {

    @Data
    public static class CreateDocRequest {
        private String content;
        private Docs.DocType type;
    }

    @Data
    @Builder
    public static class DocResponse {
        private UUID id;
        private String content;
        private Docs.DocType type;
        private String userId;
        private List<DocsTag> tags;
    }
}
