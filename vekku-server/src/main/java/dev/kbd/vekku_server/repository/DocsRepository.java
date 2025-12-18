package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.Docs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocsRepository extends JpaRepository<Docs, UUID> {
}
