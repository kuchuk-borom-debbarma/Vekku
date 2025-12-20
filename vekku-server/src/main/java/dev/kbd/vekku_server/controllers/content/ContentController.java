package dev.kbd.vekku_server.controllers.content;

import dev.kbd.vekku_server.services.content.IContentService;
import dev.kbd.vekku_server.services.content.dtos.Content;
import dev.kbd.vekku_server.services.content.dtos.ContentDetail;
import dev.kbd.vekku_server.services.content.dtos.ContentKeywordSuggestion;
import dev.kbd.vekku_server.services.content.dtos.ContentPage;
import dev.kbd.vekku_server.services.content.dtos.CreateContentParam;
import dev.kbd.vekku_server.services.content.dtos.SaveTagsForContentParam;
import dev.kbd.vekku_server.shared.events.ContentProcessingAction;
import dev.kbd.vekku_server.infrastructure.ratelimiter.RateLimit;
import dev.kbd.vekku_server.orchestrators.content.ContentOrchestrator;
import dev.kbd.vekku_server.services.brain.IBrainService;
import dev.kbd.vekku_server.services.brain.dto.ExtractKeywordsParam;
import dev.kbd.vekku_server.services.common.dtos.TagScore;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@RateLimit(limit = 100, duration = 60)
public class ContentController {

    private final IContentService contentService;
    private final IBrainService brainService;
    private final ContentOrchestrator contentOrchestrator;

    @PostMapping
    public ResponseEntity<Content> createContent(@RequestBody CreateContentParam request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Content content = contentOrchestrator.createContent(request, userId);
        return ResponseEntity.ok(content);
    }

    @PostMapping("/tags")
    public void saveTagsForContent(@RequestBody SaveTagsForContentParam request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        contentService.updateTagsOfContent(request, userId);
    }

    @PostMapping("/{id}/suggestions/refresh")
    public void refreshSuggestions(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        // Refresh everything by default
        contentOrchestrator.refreshSuggestions(id, userId,
                EnumSet.allOf(ContentProcessingAction.class));
    }

    @PostMapping("/{id}/suggestions/tags/refresh")
    public void refreshTagSuggestions(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        contentOrchestrator.refreshSuggestions(id, userId,
                EnumSet.of(ContentProcessingAction.SUGGEST_TAGS));
    }

    @PostMapping("/{id}/suggestions/keywords/refresh")
    public void refreshKeywordSuggestions(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        contentOrchestrator.refreshSuggestions(id, userId,
                EnumSet.of(ContentProcessingAction.SUGGEST_KEYWORDS));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentDetail> getContent(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(contentService.getContent(id, userId));
    }

    @GetMapping
    public ResponseEntity<ContentPage> getAllContent(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(contentService.getAllContent(userId, limit, cursor));
    }

    @PostMapping("/keywords")
    public ResponseEntity<List<TagScore>> getKeywordsOnDemand(
            @RequestBody ExtractKeywordsParam request,
            @AuthenticationPrincipal Jwt jwt) {
        // Authenticated users can request keyword extraction on raw text
        // Use BrainService directly
        return ResponseEntity.ok(brainService.extractKeywords(request.content(), request.topK(), request.diversity()));
    }

    @GetMapping("/{id}/keywords")
    public ResponseEntity<List<ContentKeywordSuggestion>> getKeywordsForContent(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(contentService.getContentKeywords(id, userId));
    }
}
