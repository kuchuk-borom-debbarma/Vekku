package dev.kbd.vekku_server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.UUID;

@Entity
@Table(name = "docs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Docs {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private DocType type;

    private String userId;

    public enum DocType {
        TEXT,
        MARKDOWN
    }
}
