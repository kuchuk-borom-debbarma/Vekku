package dev.kbd.vekku_server.controllers;

import dev.kbd.vekku_server.model.Tag;
import dev.kbd.vekku_server.services.core.tag.TagService;
import dev.kbd.vekku_server.services.orchestration.TagOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 🎮 Tag Controller
 * <p>
 * REST API for managing Semantic Tags.
 * Endpoints consumed by the Frontend (ManageTags.tsx).
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;
    private final TagOrchestrator tagOrchestrator;

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody CreateTagRequest request) {
        Tag tag = tagOrchestrator.createTag(request.alias(), request.synonyms(), "user-default"); // simple user id for
                                                                                                  // now
        return ResponseEntity.ok(tag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable UUID id, @RequestBody CreateTagRequest request) {
        Tag tag = tagOrchestrator.updateTag(id, request.alias(), request.synonyms());
        return ResponseEntity.ok(tag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagOrchestrator.deleteTag(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    public record CreateTagRequest(String alias, List<String> synonyms) {
    }
}
