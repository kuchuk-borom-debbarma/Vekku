package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.content.ContentKeywordSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentKeywordSuggestionRepository extends JpaRepository<ContentKeywordSuggestion, java.util.UUID> {
    List<ContentKeywordSuggestion> findByContentId(java.util.UUID contentId);

    void deleteByContentId(java.util.UUID contentId);
}
