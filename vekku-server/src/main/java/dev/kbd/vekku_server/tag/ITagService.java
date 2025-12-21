package dev.kbd.vekku_server.tag;

import java.util.List;
import java.util.Set;

import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;

interface ITagService {
    TagDTO getTag(String userId, String id);

    List<TagDTO> getTags(
        String userId,
        String fromCursor,
        int limit,
        String dir
    );

    TagDTO createTag(String userId, String tagName, Set<String> synonyms);

    TagDTO updateTag(
        String userId,
        String tagId,
        String tagName,
        Set<String> synsToAdd,
        Set<String> synsToRemove
    );

    void deleteTag(String subject, String tagId);
}
