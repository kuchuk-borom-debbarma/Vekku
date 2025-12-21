package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.TagDTOs.TagDTO;
import java.util.List;
import java.util.Set;

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
