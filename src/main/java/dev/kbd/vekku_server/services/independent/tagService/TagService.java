package dev.kbd.vekku_server.services.independent.tagService;

import java.util.List;

import dev.kbd.vekku_server.services.independent.tagService.neo4jTagService.models.Tag;

public interface TagService {
    Tag createTag(String tagName, String parentTagName);

    List<Tag> getAncestors(String tagName);
}
