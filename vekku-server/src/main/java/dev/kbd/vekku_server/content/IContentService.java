package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import java.util.List;

public interface IContentService {
    ContentDTO createContent(String userId, CreateContentRequest request);

    ContentDTO updateContent(String userId, UpdateContentRequest request);

    void deleteContent(String id, String userId);

    ContentDTO getContentOfUser(String id, String userId);

    List<ContentDTO> getContentsOfUser(
        String userId,
        String cursor,
        int limit,
        String direction
    );
}
