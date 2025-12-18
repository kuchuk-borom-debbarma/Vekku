package dev.kbd.vekku_server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.UUID;

@Entity
@Table(name = "docs_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocsTag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID docId;

    private String tagId;

    private Double score;

    private String userId;
}
