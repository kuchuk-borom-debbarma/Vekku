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
    public ResponseEntity<DocDto.DocResponse> createDoc(@RequestBody DocDto.CreateDocRequest request) {
        // Hardcoded userId for now as Auth handling might need SecurityContext
        // or we can pass it if secured. Assuming 'test-user' or extraction if Auth is
        // implemented.
        // The prompt says "userId" in DB, but didn't specify how we get it.
        // I will assume a default or extracted from context if possible.
        // For MVP, I'll use a placeholder string or "anonymous" if not auth.
        String userId = "user-1";
        return ResponseEntity.ok(contentService.createDoc(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocDto.DocResponse> getDoc(@PathVariable UUID id) {
        return ResponseEntity.ok(contentService.getDoc(id));
    }

    @GetMapping
    public ResponseEntity<List<DocDto.DocResponse>> getAllDocs() {
        return ResponseEntity.ok(contentService.getAllDocs("user-1"));
    }
}
