package dev.kbd.vekku_server.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.kbd.vekku_server.services.independent.brainService.model.ContentRegionTags;
import dev.kbd.vekku_server.services.orchestrator.TagOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/brain")
@RequiredArgsConstructor
@Slf4j
public class BrainController {

    private final TagOrchestratorService tagOrchestratorService;

    /**
     * Suggests tags for the given content.
     * This orchestrates the Brain Service for semantic analysis and the Taxonomy
     * Service
     * for hierarchical context, returning a refined list of suggestions.
     *
     * @param content The text content to analyze.
     * @return List of ContentRegionTags suggestions.
     */
    @PostMapping("/suggest")
    public List<ContentRegionTags> suggestTags(@RequestBody String content) {
        log.info("Received tag suggestion request for content length: {}", content.length());
        // TODO: We might want a DTO here later if we need to pass more than just raw
        // text (e.g., existing tags, context).
        // For now, raw string body is sufficient.
        return tagOrchestratorService.suggestTags(content);
    }
}
