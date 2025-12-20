package dev.kbd.vekku_server.controllers;

import dev.kbd.vekku_server.dto.content.ContentPageDto;
import dev.kbd.vekku_server.dto.content.CreateContentRequest;
import dev.kbd.vekku_server.dto.content.SaveTagsForContentRequest;
import dev.kbd.vekku_server.model.content.Content;
import dev.kbd.vekku_server.services.core.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    public ResponseEntity<Content> createContent(@RequestBody CreateContentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Content content = contentService.createContent(request, userId);
        return ResponseEntity.ok(content);
    }

    @PostMapping("/tags")
    public void saveTagsForContent(@RequestBody SaveTagsForContentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        contentService.saveTagsForContent(request, userId);
    }

    @PostMapping("/{id}/suggestions/refresh")
    public void refreshSuggestions(@PathVariable java.util.UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        // Refresh everything by default
        contentService.refreshSuggestions(id, userId,
                java.util.EnumSet.allOf(dev.kbd.vekku_server.event.ContentProcessingAction.class));
    }

    @PostMapping("/{id}/suggestions/tags/refresh")
    public void refreshTagSuggestions(@PathVariable java.util.UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        contentService.refreshSuggestions(id, userId,
                java.util.EnumSet.of(dev.kbd.vekku_server.event.ContentProcessingAction.SUGGEST_TAGS));
    }

    @PostMapping("/{id}/suggestions/keywords/refresh")
    public void refreshKeywordSuggestions(@PathVariable java.util.UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        contentService.refreshSuggestions(id, userId,
                java.util.EnumSet.of(dev.kbd.vekku_server.event.ContentProcessingAction.SUGGEST_KEYWORDS));
    }

    @GetMapping("/{id}")
    public ResponseEntity<dev.kbd.vekku_server.dto.content.ContentDetailDto> getContent(@PathVariable java.util.UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(contentService.getContent(id, userId));
    }

    @GetMapping
    public ResponseEntity<ContentPageDto> getAllContent(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(contentService.getAllContent(userId, limit, cursor));
    }

    @PostMapping("/keywords")
    public ResponseEntity<java.util.List<dev.kbd.vekku_server.services.brain.model.TagScore>> getKeywordsOnDemand(
            @RequestBody dev.kbd.vekku_server.services.brain.dto.ExtractKeywordsRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        // Authenticated users can request keyword extraction on raw text
        return ResponseEntity.ok(contentService.extractKeywordsOnDemand(request.content()));
    }

    @GetMapping("/{id}/keywords")
    public ResponseEntity<java.util.List<dev.kbd.vekku_server.model.content.ContentKeywordSuggestion>> getKeywordsForContent(
            @PathVariable java.util.UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(contentService.getContentKeywords(id, userId));
    }
}
