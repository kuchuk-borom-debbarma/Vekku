package dev.kbd.vekku_server.tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TagRepo extends JpaRepository<TagEntity, UUID> {
    Optional<TagEntity> findByUserIdAndId(String userId, UUID id);

    List<TagEntity> findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(
        String userId,
        LocalDateTime createdAt,
        Pageable pageable
    );

    List<TagEntity> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
        String userId,
        LocalDateTime createdAt,
        Pageable pageable
    );

    List<TagEntity> findByUserIdOrderByCreatedAtAsc(String userId, Pageable pageable);

    List<TagEntity> findByUserIdOrderByCreatedAtDesc(
        String userId,
        Pageable pageable
    );
}
