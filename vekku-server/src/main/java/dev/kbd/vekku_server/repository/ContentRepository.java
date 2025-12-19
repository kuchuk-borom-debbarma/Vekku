package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.content.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {
}
