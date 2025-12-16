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

    @Query("MATCH (child:Tag {name: $name})-[:CHILD_OF]->(parent:Tag) RETURN parent")
    List<Tag> findParentsByName(String name);

    /**
     * <b>FIND ALL PATHS TO ROOT:</b>
     * <p>
     * Finds all paths starting from the given tag up to a root node.
     * <ul>
     * <li><code>MATCH p=(leaf:Tag {name: $name})-[:CHILD_OF*0..]->(root)</code>:
     * Finds
     * paths from leaf traversing UP to root.</li>
     * <li><code>WHERE NOT (root)-[:CHILD_OF]->()</code>: Ensures 'root' has no
     * parents (is top-level).</li>
     * <li><code>RETURN nodes(p)</code>: Returns the list of nodes [Leaf, Parent,
     * ..., Root].</li>
     * </ul>
     */
    @Query("MATCH p=(leaf:Tag {name: $name})-[:CHILD_OF*0..]->(root) WHERE NOT (root)-[:CHILD_OF]->() RETURN nodes(p)")
    List<List<Tag>> findPathsToTag(String name);

    @Query("MATCH p=(leaf:Tag {name: $name})-[:CHILD_OF*0..]->(root) WHERE NOT (root)-[:CHILD_OF]->() RETURN [n IN nodes(p) | n.name]")
    List<List<String>> findPathNames(String name);

    @Query("MATCH (n:Tag {name: $name}) RETURN count(n)")
    long countNodesByName(String name);

    @Query("MATCH (n:Tag {name: $name}) RETURN elementId(n)")
    List<String> findNodeIds(String name);

    /**
     * <b>SERIALIZED PATH:</b>
     * <p>
     * Returns paths as a delimiter-separated string to avoid SDN List mapping
     * issues.
     * Format: "Leaf$$$Parent$$$Root"
     * Delimiter: "$$$"
     */
    @Query("MATCH p=(leaf:Tag {name: $name})-[:CHILD_OF*0..]->(root) WHERE NOT (root)-[:CHILD_OF]->() " +
            "WITH nodes(p) as pathNodes " +
            "RETURN reduce(s = head(pathNodes).name, n IN tail(pathNodes) | s + '$$$' + n.name)")
    List<String> findSerializedPaths(String name);
}
