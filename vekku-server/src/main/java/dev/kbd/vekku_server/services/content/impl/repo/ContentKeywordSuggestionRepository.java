package dev.kbd.vekku_server.services.content.impl.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kbd.vekku_server.services.content.impl.entities.ContentKeywordSuggestionEntity;

import java.util.List;

@Repository
public interface ContentKeywordSuggestionRepository
        extends JpaRepository<ContentKeywordSuggestionEntity, java.util.UUID> {
    List<ContentKeywordSuggestionEntity> findByContentId(java.util.UUID contentId);

    void deleteByContentId(java.util.UUID contentId);
}
