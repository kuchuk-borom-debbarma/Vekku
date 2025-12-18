package dev.kbd.vekku_server.controllers;

import dev.kbd.vekku_server.services.brain.BrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final BrainService brainService;

    @PostMapping("/learn")
    public ResponseEntity<Void> learnTag(@RequestBody LearnTagRequest request) {
        log.info("Learning new tag: {}", request.tagName());
        brainService.learnTag(request.tagName());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<BrainService.TagListDto> getAllTags(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String offset) {
        return ResponseEntity.ok(brainService.getAllTags(limit, offset));
    }

    @DeleteMapping("/{tagName}")
    public ResponseEntity<Void> deleteTag(@PathVariable String tagName) {
        brainService.deleteTag(tagName);
        return ResponseEntity.ok().build();
    }

    public record LearnTagRequest(String tagName) {
    }
}
