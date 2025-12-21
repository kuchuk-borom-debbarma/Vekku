package dev.kbd.vekku_server.tag.api;

import dev.kbd.vekku_server.tag.api.TagDTOs.LinkTagsToContentRequest;
import dev.kbd.vekku_server.tag.api.TagDTOs.UnlinkTagsFromContentRequest;

public interface ITagContentService {
    void linkTagsToContent(LinkTagsToContentRequest request, String userId);

    void unlinkTagsFromContent(
        UnlinkTagsFromContentRequest request,
        String userId
    );
}
