package dev.kbd.vekku_server.services.content.repo;

import dev.kbd.vekku_server.services.content.model.ContentKeywordSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentKeywordSuggestionRepository extends JpaRepository<ContentKeywordSuggestion, java.util.UUID> {
    List<ContentKeywordSuggestion> findByContentId(java.util.UUID contentId);

    void deleteByContentId(java.util.UUID contentId);
}
