package dev.kbd.vekku_server.controllers;

import dev.kbd.vekku_server.dto.DocDto;
import dev.kbd.vekku_server.services.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    public ResponseEntity<DocDto.DocResponse> createDoc(@RequestBody DocDto.CreateDocRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(contentService.createDoc(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocDto.DocResponse> getDoc(@PathVariable UUID id) {
        return ResponseEntity.ok(contentService.getDoc(id));
    }

    @GetMapping
    public ResponseEntity<List<DocDto.DocResponse>> getAllDocs(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        return ResponseEntity.ok(contentService.getAllDocs(jwt.getSubject()));
    }
}
