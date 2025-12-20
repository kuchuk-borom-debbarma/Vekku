package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.content.ContentTagSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ContentTagSuggestionRepository extends JpaRepository<ContentTagSuggestion, UUID> {
    void deleteByContentId(UUID contentId);

    List<ContentTagSuggestion> findByContentId(UUID contentId);
}
