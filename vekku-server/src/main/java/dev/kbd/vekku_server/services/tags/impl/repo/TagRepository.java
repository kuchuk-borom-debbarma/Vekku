package dev.kbd.vekku_server.services.tags.impl.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kbd.vekku_server.services.tags.impl.entities.TagEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {
    List<TagEntity> findByNameGreaterThanAndUserIdOrderByNameAsc(String name, String userId, Pageable pageable);

    List<TagEntity> findAllByUserIdOrderByNameAsc(String userId, Pageable pageable);

    java.util.Optional<TagEntity> findByNameAndUserId(String name, String userId);
}
