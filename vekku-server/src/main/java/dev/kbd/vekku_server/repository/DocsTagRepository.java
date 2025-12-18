package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.DocsTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface DocsTagRepository extends JpaRepository<DocsTag, UUID> {
    List<DocsTag> findByDocId(UUID docId);
}
