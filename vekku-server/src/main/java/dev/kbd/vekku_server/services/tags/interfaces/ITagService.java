package dev.kbd.vekku_server.services.tags.interfaces;

import dev.kbd.vekku_server.controllers.tag.models.TagPageDto;
import dev.kbd.vekku_server.services.tags.model.Tag;
import java.util.List;
import java.util.UUID;

public interface ITagService {
    Tag createTag(String alias, List<String> synonyms, String userId);

    Tag updateTag(UUID id, String alias, List<String> synonyms, String userId);

    void deleteTag(UUID id, String userId);

    Tag getTag(UUID id);

    List<Tag> getAllTags();

    TagPageDto getTags(String userId, Integer limit, String cursor);

    java.util.Optional<Tag> getTagByName(String name, String userId);
}
