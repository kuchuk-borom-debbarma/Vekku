package dev.kbd.vekku_server.services.content.repo;

import dev.kbd.vekku_server.services.content.model.ContentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentTagRepository extends JpaRepository<ContentTag, UUID> {
    List<ContentTag> findByContentId(UUID contentId);

    void deleteByContentId(UUID contentId);
}
