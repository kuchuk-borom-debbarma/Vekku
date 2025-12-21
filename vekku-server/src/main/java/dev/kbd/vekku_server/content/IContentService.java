package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;

public interface IContentService {
    ContentDTO createContent(String userId, CreateContentRequest request);

    ContentDTO updateContent(String userId, UpdateContentRequest request);

    void deleteContent(String id, String userId);
}
