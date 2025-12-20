package dev.kbd.vekku_server.controllers.tag;

import dev.kbd.vekku_server.services.tags.ITagService;
import dev.kbd.vekku_server.services.tags.dtos.Tag;
import dev.kbd.vekku_server.services.tags.dtos.TagPage;
import dev.kbd.vekku_server.controllers.tag.models.CreateTagRequest;
import dev.kbd.vekku_server.infrastructure.ratelimiter.RateLimit;
import dev.kbd.vekku_server.orchestrators.tag_orchestration.TagOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Tag Controller
 * <p>
 * REST API for managing Semantic Tags.
 * Endpoints consumed by the Frontend (ManageTags.tsx).
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
@RateLimit(limit = 100, duration = 60)
public class TagController {

    private final ITagService tagService;
    private final TagOrchestrator tagOrchestrator;

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody CreateTagRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Tag tag = tagOrchestrator.createTag(request.alias(), request.synonyms(), userId);
        return ResponseEntity.ok(tag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable UUID id, @RequestBody CreateTagRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Tag tag = tagOrchestrator.updateTag(id, request.alias(), request.synonyms(), userId);
        return ResponseEntity.ok(tag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        tagOrchestrator.deleteTag(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<TagPage> getAllTags(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(tagService.getTags(userId, limit, cursor));
    }

}
