package dev.kbd.vekku_server.services.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private java.util.UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private ContentType type;

    @Column(nullable = false)
    private String userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(name = "updated_at")
    private LocalDateTime updated;

    @PrePersist
    public void onCreate() {
        this.created = java.time.LocalDateTime.now();
        this.updated = java.time.LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updated = java.time.LocalDateTime.now();
    }
}
