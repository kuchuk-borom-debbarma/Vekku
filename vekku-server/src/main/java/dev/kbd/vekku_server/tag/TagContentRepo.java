package dev.kbd.vekku_server.tag;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TagContentRepo
    extends
        SpecificationFluentQuery<TagContentEntity>,
        CrudRepository<TagContentEntity, UUID> {}
