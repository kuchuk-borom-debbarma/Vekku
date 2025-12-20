package dev.kbd.vekku_server.controllers;

import dev.kbd.vekku_server.dto.content.ContentPageDto;
import dev.kbd.vekku_server.dto.content.CreateContentRequest;
import dev.kbd.vekku_server.dto.content.SaveTagsForContentRequest;
import dev.kbd.vekku_server.model.content.Content;
import dev.kbd.vekku_server.service.ContentService;
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
        contentService.refreshSuggestions(id, userId);
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
}
