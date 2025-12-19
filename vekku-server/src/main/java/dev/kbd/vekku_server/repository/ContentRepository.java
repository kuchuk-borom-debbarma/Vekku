package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.content.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {
        java.util.List<Content> findAllByUserId(String userId);

        java.util.List<Content> findAllByUserIdOrderByCreatedDesc(String userId,
                        org.springframework.data.domain.Pageable pageable);

        java.util.List<Content> findByUserIdAndCreatedLessThanOrderByCreatedDesc(String userId,
                        java.time.LocalDateTime created,
                        org.springframework.data.domain.Pageable pageable);
}
