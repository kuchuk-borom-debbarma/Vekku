package dev.kbd.vekku_server.services.content.impl.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kbd.vekku_server.services.content.impl.entities.ContentTagEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentTagRepository extends JpaRepository<ContentTagEntity, UUID> {
    List<ContentTagEntity> findByContentId(UUID contentId);

    void deleteByContentId(UUID contentId);
}
