package dev.kbd.vekku_server.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
import dev.kbd.vekku_server.services.brain.model.TagScore;
import dev.kbd.vekku_server.services.brain.BrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/brain")
@RequiredArgsConstructor
@Slf4j
public class BrainController {

    private final BrainService brainService;

    /**
     * Suggests tags for the given content.
     * This orchestrates the Brain Service for semantic analysis and the Taxonomy
     * Service
     * for hierarchical context, returning a refined list of suggestions.
     *
     * @param content The text content to analyze.
     * @return List of ContentRegionTags suggestions.
     */
    /**
     * Get Raw Tags purely from Embedding Model
     */
    public record TagRequest(String content, Double threshold, Integer topK) {
    }

    /**
     * Get Raw Tags purely from Embedding Model
     */
    @PostMapping("/raw")
    public List<TagScore> getRawTags(@RequestBody TagRequest request) {
        String content = request.content();
        Double threshold = request.threshold() != null ? request.threshold() : 0.3;
        Integer topK = request.topK() != null ? request.topK() : 50;

        log.info("Requesting Raw Tags (Len: {}, Thresh: {}, TopK: {})", content.length(), threshold, topK);
        return brainService.getRawTagsByEmbedding(content, threshold, topK);
    }

    /**
     * Get Content Regions with Tags
     */
    @PostMapping("/regions")
    public List<ContentRegionTags> getRegionTags(@RequestBody TagRequest request) {
        String content = request.content();
        Double threshold = request.threshold() != null ? request.threshold() : 0.3;
        Integer topK = request.topK() != null ? request.topK() : 5;

        log.info("Requesting Region Tags (Len: {}, Thresh: {}, TopK: {})", content.length(), threshold, topK);
        return brainService.getRegionTags(content, threshold, topK);
    }

}
