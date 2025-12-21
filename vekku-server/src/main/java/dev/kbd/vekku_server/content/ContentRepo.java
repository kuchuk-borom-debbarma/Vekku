package dev.kbd.vekku_server.content;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
interface ContentRepo
    extends
        JpaRepository<ContentEntity, UUID>,
        JpaSpecificationExecutor<ContentEntity> {}
