package dev.kbd.vekku_server.services.independent.tagService.neo4jTagService;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import dev.kbd.vekku_server.services.independent.tagService.neo4jTagService.models.Tag;

@Repository
public interface Neo4jTagRepo extends Neo4jRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    /**
     * RECURSIVE:
     * [:CHILD_OF*] means "follow the CHILD_OF relationship as many times as
     * possible".
     * This grabs Parents, Grandparents, Great-Grandparents, etc.
     */
    @Query("MATCH (t:Tag {name: $name})-[:CHILD_OF*]->(parent:Tag) RETURN parent")
    List<Tag> findAllAncestors(String name);
}
