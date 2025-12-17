package dev.kbd.vekku_server.services.taxonomy.models;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Node
@NoArgsConstructor
@Getter
@Setter
public class Tag {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    /**
     * THE HIERARCHY: Defines that this tag is a "Child Of" another tag.
     * <p>
     * <b>Concept:</b> In a Graph, relationships are first-class citizens.
     * Unlike SQL where you might have a "parent_id" column, here we have an actual
     * Link (Edge) pointing to another Node.
     * <p>
     * <b>Direction.OUTGOING</b> means:
     * (This Tag) ---[CHILD_OF]---> (Parent Tag)
     * <p>
     * <b>Set&lt;Tag&gt;:</b> Using a Set ensures we don't accidentally link to the
     * same parent twice.
     * It also allows for "Polyhierarchy" (a tag can belong to multiple categories).
     * e.g., "SpringBoot" is a child of "Java" AND "Frameworks".
     */
    @Relationship(type = "CHILD_OF", direction = Relationship.Direction.OUTGOING)
    private Set<Tag> parents = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }
}
