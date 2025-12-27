package dev.kbd.vekku_server.tag.api;

import dev.kbd.vekku_server.tag.api.TagDTOs.LinkTagsToContentRequest;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagContentDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.UnlinkTagsFromContentRequest;
import java.util.List;

public interface ITagContentService {
    TagContentDTO getTagContent(String id, String userId);
    List<TagDTO> getTagsOfContent(
        String contentId,
        String from,
        int limit,
        String direction,
        String subject
    );
    void linkTagsToContent(LinkTagsToContentRequest request, String userId);
    void unlinkTagsFromContent(
        UnlinkTagsFromContentRequest request,
        String userId
    );
}
