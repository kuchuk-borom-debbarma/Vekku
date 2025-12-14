package dev.kbd.vekku_server.services.independent.tagService.neo4jTagService.models;

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

    // THE HIERARCHY: Defines that this tag is a "Child Of" another tag.
    // Direction.OUTGOING means: (This Tag) -> [CHILD_OF] -> (Parent Tag)
    @Relationship(type = "CHILD_OF", direction = Relationship.Direction.OUTGOING)
    private Set<Tag> parents = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }
}
