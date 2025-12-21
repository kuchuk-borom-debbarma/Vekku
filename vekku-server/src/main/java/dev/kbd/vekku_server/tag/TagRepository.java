package dev.kbd.vekku_server.tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByUserIdAndId(String userId, UUID id);

    List<Tag> findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(
        String userId,
        LocalDateTime createdAt,
        Pageable pageable
    );

    List<Tag> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
        String userId,
        LocalDateTime createdAt,
        Pageable pageable
    );

    List<Tag> findByUserIdOrderByCreatedAtAsc(String userId, Pageable pageable);

    List<Tag> findByUserIdOrderByCreatedAtDesc(
        String userId,
        Pageable pageable
    );
}
