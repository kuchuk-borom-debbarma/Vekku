package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.content.ContentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentTagRepository extends JpaRepository<ContentTag, UUID> {
    List<ContentTag> findByContentId(UUID contentId);

    void deleteByContentId(UUID contentId);
}
