package dev.kbd.vekku_server.services.content.impl.entities;

import dev.kbd.vekku_server.services.tags.model.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "content_tag_suggestions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentTagSuggestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private ContentEntity content;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    private Double score;

    @Column(nullable = false)
    private String userId;
}
