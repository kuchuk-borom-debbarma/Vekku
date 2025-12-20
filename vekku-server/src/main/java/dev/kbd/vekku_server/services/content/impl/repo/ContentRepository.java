package dev.kbd.vekku_server.services.content.impl.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kbd.vekku_server.services.content.impl.entities.ContentEntity;

import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<ContentEntity, UUID> {
        java.util.List<ContentEntity> findAllByUserId(String userId);

        java.util.List<ContentEntity> findAllByUserIdOrderByCreatedDesc(String userId,
                        org.springframework.data.domain.Pageable pageable);

        java.util.List<ContentEntity> findByUserIdAndCreatedLessThanOrderByCreatedDesc(String userId,
                        java.time.LocalDateTime created,
                        org.springframework.data.domain.Pageable pageable);
}
