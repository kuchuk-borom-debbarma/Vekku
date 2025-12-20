package dev.kbd.vekku_server.services.content.impl.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "content_keyword_suggestions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentKeywordSuggestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private ContentEntity content;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private Double score;

    @Column(name = "user_id", nullable = false)
    private String userId;
}
