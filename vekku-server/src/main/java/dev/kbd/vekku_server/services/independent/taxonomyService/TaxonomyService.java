package dev.kbd.vekku_server.services.independent.taxonomyService;

import java.util.List;

import dev.kbd.vekku_server.services.independent.taxonomyService.models.Tag;

public interface TaxonomyService {
    Tag createTag(String tagName, String parentTagName);

    List<Tag> getAncestors(String tagName);

    List<Tag> getChildren(String tagName);

    List<Tag> getParents(String tagName);

    List<Tag> getAllTags();

    List<List<Tag>> getPaths(String tagName);

    List<List<String>> getPathNames(String tagName);

    long countNodes(String tagName);

    List<String> getSerializedPaths(String tagName);
}
