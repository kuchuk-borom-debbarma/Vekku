package dev.kbd.vekku_server.services.taxonomy.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kbd.vekku_server.services.taxonomy.TaxonomyService;
import dev.kbd.vekku_server.services.taxonomy.models.Tag;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxonomyServiceImpl implements TaxonomyService {
    private final Neo4jRepo repo;

    /**
     * Creates a new tag and optionally links it to a parent.
     * <p>
     * <b>@Transactional:</b> This is crucial. It ensures that all database
     * operations within this method happen in a single "Unit of Work".
     * If saving the parent fails, the child won't be saved either.
     * <p>
     * <b>Logic:</b>
     * 1. Find/Create Child.
     * 2. Find/Create Parent (if provided).
     * 3. Link them in memory.
     * 4. Save Child -> Neo4j cascades the save to the relationship and parent.
     */
    @Transactional
    @Override
    public Tag createTag(String tagName, String parentTagName) {
        log.trace("createTag({}, {})", tagName, parentTagName);

        // 1. Find or create the main tag
        Tag tag = repo.findByName(tagName)
                .orElse(new Tag(tagName));

        // 2. If a parent is provided, handle the linking
        if (StringUtils.hasText(parentTagName)) {
            // Find or auto create the parent
            Tag parent = repo.findByName(parentTagName)
                    .orElseGet(() -> repo.save(new Tag(parentTagName)));
            // Add to the Set (Neo4j handles the unique relationship)
            tag.getParents().add(parent);
        }
        // 3. Save updated tag
        return repo.save(tag);
    }

    public List<Tag> getAncestors(String tagName) {
        return repo.findAllAncestors(tagName);
    }

    @Override
    public List<Tag> getChildren(String tagName) {
        return repo.findChildrenByName(tagName);
    }

    @Override
    public List<Tag> getAllTags() {
        return repo.findAll();
    }

    @Override
    public List<Tag> getParents(String tagName) {
        return repo.findParentsByName(tagName);
    }

    @Override
    public List<List<Tag>> getPaths(String tagName) {
        return repo.findPathsToTag(tagName);
    }

    @Override
    public List<List<String>> getPathNames(String tagName) {
        return repo.findPathNames(tagName);
    }

    @Override
    public long countNodes(String tagName) {
        return repo.countNodesByName(tagName);
    }

    @Override
    public List<String> getSerializedPaths(String tagName) {
        return repo.findSerializedPaths(tagName);
    }
}
