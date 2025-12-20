package dev.kbd.vekku_server.services.content.impl.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kbd.vekku_server.services.content.impl.entities.ContentTagSuggestionEntity;

import java.util.UUID;
import java.util.List;

@Repository
public interface ContentTagSuggestionRepository extends JpaRepository<ContentTagSuggestionEntity, UUID> {
    void deleteByContentId(UUID contentId);

    List<ContentTagSuggestionEntity> findByContentId(UUID contentId);
}
