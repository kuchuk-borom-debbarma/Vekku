package dev.kbd.vekku_server.services.orchestrator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import dev.kbd.vekku_server.services.independent.taxonomyService.TaxonomyService;
import dev.kbd.vekku_server.services.independent.taxonomyService.neo4jTaxonomyService.models.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagOrchestratorService {
    private final TaxonomyService taxonomyService;
    private final BrainService brainService;

    /**
     * Orchestrates the creation of a tag across the Knowledge Graph (Neo4j)
     * and the Semantic Brain (Qdrant).
     * 
     * <p>
     * <b>Transactionality:</b>
     * This method is @Transactional. If the Brain Service call fails (HTTP Error),
     * this transaction will roll back, and the Neo4j write will be undone.
     * This ensures we don't end up with "Ghost Tags" in the graph that the brain
     * doesn't know about.
     */
    @Transactional
    public Tag createTag(String name, String parent) {
        log.info("Orchestrating Tag Creation: '{}' (Parent: '{}')", name, parent);

        // 1. Source of Truth: Create structure in Neo4j
        Tag tag = taxonomyService.createTag(name, parent);

        // 2. Semantic Index: Teach the Brain
        // Note: If this fails, the RuntimeException triggers a rollback of Step 1.
        brainService.learnTag(name);

        return tag;
    }
}
