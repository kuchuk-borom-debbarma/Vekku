package dev.kbd.vekku_server.tag;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface TagContentRepo
    extends
        JpaRepository<TagContentEntity, UUID>,
        JpaSpecificationExecutor<TagContentEntity> {
    @Modifying
    @Query(
        "DELETE FROM TagContentEntity tce WHERE tce.contentId = :contentId AND tce.tagId IN :tagIds"
    )
    void deleteLinks(
        @Param("contentId") UUID contentId,
        @Param("tagIds") Set<UUID> tagIds
    );
}
