package dev.kbd.vekku_server.services.independent.taxonomyService.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import dev.kbd.vekku_server.services.independent.taxonomyService.models.Tag;

@Repository
public interface Neo4jRepo extends Neo4jRepository<Tag, Long> {
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

    /**
     * <b>FIND IMMEDIATE CHILDREN:</b>
     * <p>
     * Finds tags that have a CHILD_OF relationship pointing TO this tag.
     * <code>(child)-[:CHILD_OF]->(parent)</code>
     */
    @Query("MATCH (parent:Tag {name: $name})<-[:CHILD_OF]-(child:Tag) RETURN child")
    List<Tag> findChildrenByName(String name);

    /**
     * <b>FIND ALL PATHS TO ROOT:</b>
     * <p>
     * Finds all paths starting from a root node (no parents) down to the specified
     * tag.
     * <ul>
     * <li><code>MATCH p=(root)-[:CHILD_OF*]->(leaf:Tag {name: $name})</code>: Finds
     * paths ending at 'leaf'.</li>
     * <li><code>WHERE NOT ()-[:CHILD_OF]->(root)</code>: Ensures 'root' is truly a
     * root (no incoming child_of).</li>
     * <li><code>RETURN nodes(p)</code>: Returns the list of nodes for each path
     * found.</li>
     * </ul>
     */
    @Query("MATCH p=(root)-[:CHILD_OF*]->(leaf:Tag {name: $name}) WHERE NOT ()-[:CHILD_OF]->(root) RETURN nodes(p)")
    List<List<Tag>> findPathsToTag(String name);
}
