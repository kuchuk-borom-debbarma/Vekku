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
     * <b>RECURSIVE ANCESTOR SEARCH:</b>
     * <p>
     * This query utilizes Cypher's Variable Length Path matching.
     * <ul>
     * <li><code>MATCH (t:Tag {name: $name})</code>: Finds the starting node (e.g.,
     * "Java").</li>
     * <li><code>-[:CHILD_OF*]-></code>: The <code>*</code> is the magic operator.
     * It keeps hopping across "CHILD_OF" relationships until it runs out of
     * paths.</li>
     * <li><code>(parent:Tag)</code>: Binds every node found along the way to the
     * "parent" variable.</li>
     * </ul>
     * This effectively flattens the entire hierarchy above the tag into a List.
     */
    @Query("MATCH (t:Tag {name: $name})-[:CHILD_OF*]->(parent:Tag) RETURN parent")
    List<Tag> findAllAncestors(String name);
}
