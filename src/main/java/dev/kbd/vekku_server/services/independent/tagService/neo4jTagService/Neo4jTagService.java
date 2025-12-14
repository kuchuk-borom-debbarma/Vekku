package dev.kbd.vekku_server.services.independent.tagService.neo4jTagService;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kbd.vekku_server.services.independent.tagService.TagService;
import dev.kbd.vekku_server.services.independent.tagService.neo4jTagService.models.Tag;
import io.netty.util.internal.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Neo4jTagService implements TagService {
    private final Neo4jTagRepo repo;

    @Transactional
    @Override
    public Tag createTag(String tagName, String parentTagName) {
        log.trace("createTag({}, {})", tagName, parentTagName);

        // 1. Find or create the main tag
        Tag tag = repo.findByName(tagName)
                .orElse(new Tag(tagName));

        // 2. If a parent is provided, handle the linking
        if (!StringUtil.isNullOrEmpty(parentTagName)) {
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

}
