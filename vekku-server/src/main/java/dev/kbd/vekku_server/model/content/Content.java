package dev.kbd.vekku_server.model.content;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Long created;

    @Column(nullable = false)
    private Long updated;

    @PrePersist
    public void onCreate() {
        this.created = System.currentTimeMillis();
        this.updated = System.currentTimeMillis();
    }

    @PreUpdate
    public void onUpdate() {
        this.updated = System.currentTimeMillis();
    }
}
