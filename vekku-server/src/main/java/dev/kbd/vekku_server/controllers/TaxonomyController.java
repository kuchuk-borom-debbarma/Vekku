package dev.kbd.vekku_server.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.kbd.vekku_server.services.independent.taxonomyService.TaxonomyService;
import dev.kbd.vekku_server.services.independent.taxonomyService.models.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/taxonomy")
@RequiredArgsConstructor
@Slf4j
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    @GetMapping("/tree")
    public List<Tag> getTree() {
        log.info("Fetching taxonomy tree");
        // For now, we return the flat list. The client will reconstruct the tree.
        return taxonomyService.getAllTags();
    }
}
