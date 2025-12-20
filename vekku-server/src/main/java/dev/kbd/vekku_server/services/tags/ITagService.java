package dev.kbd.vekku_server.services.tags;

import dev.kbd.vekku_server.services.tags.dtos.Tag;
import dev.kbd.vekku_server.services.tags.dtos.TagPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ITagService {
    Tag createTag(String alias, List<String> synonyms, String userId);

    Tag updateTag(UUID id, String alias, List<String> synonyms, String userId);

    void deleteTag(UUID id, String userId);

    Tag getTag(UUID id);

    TagPage getTags(String userId, Integer limit, String cursor);

    Optional<Tag> getTagByName(String name, String userId);
}
