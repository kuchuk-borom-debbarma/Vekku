package dev.kbd.vekku_server.content;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ContentRepo extends CrudRepository<Content, UUID> {}
