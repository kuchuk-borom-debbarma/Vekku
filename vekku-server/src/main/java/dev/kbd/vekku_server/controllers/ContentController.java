package dev.kbd.vekku_server.controllers;

import dev.kbd.vekku_server.dto.content.CreateContentRequest;
import dev.kbd.vekku_server.model.content.Content;
import dev.kbd.vekku_server.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    public ResponseEntity<Content> createContent(@RequestBody CreateContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Content content = contentService.createContent(request, userId);
        return ResponseEntity.ok(content);
    }
}
