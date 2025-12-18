package dev.kbd.vekku_server.repository;

import dev.kbd.vekku_server.model.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByNameGreaterThanOrderByNameAsc(String name, Pageable pageable);

    List<Tag> findAllByOrderByNameAsc(Pageable pageable);
}
