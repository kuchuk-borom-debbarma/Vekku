package dev.kbd.vekku_server.tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/tag")
@RequiredArgsConstructor
@Slf4j
class TagController {

    final ITagService tagService;

    @GetMapping("/{id}")
    public TagDTO getTag(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.info("Getting tag {} for user {}", id, userId);

        TagDTO tag = tagService.getTag(userId, id);
        log.info("Tag retrieved: {}", tag);
        return tag;
    }

    @GetMapping
    public List<TagDTO> getTags(
        @RequestParam(required = false, name = "from") String fromCursor,
        @RequestParam(required = false, defaultValue = "10") int limit,
        @RequestParam(required = false, defaultValue = "next") String dir,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.info(
            "Fetching tags (cursor: {}, limit: {}, dir: {}) for user {}",
            fromCursor,
            limit,
            dir,
            userId
        );

        List<TagDTO> tags = tagService.getTags(userId, fromCursor, limit, dir);

        log.info("Retrieved {} tags", tags.size());
        return tags;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public TagDTO createTag(
        @RequestBody CreateTagRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.info("Creating tag '{}' for user {}", request.tagName(), userId);

        TagDTO createdTag = tagService.createTag(
            userId,
            request.tagName(),
            request.synonyms()
        );
        log.info("Tag created with ID: {}", createdTag.id());
        return createdTag;
    }

    @PutMapping
    public TagDTO updateTag(
        @RequestBody UpdateTagRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.info("Updating tag {} for user {}", request.tagId(), userId);

        TagDTO updatedTag = tagService.updateTag(
            userId,
            request.tagId(),
            request.tagName(),
            request.synsToAdd(),
            request.synsToRemove()
        );
        log.info("Tag updated successfully");
        return updatedTag;
    }

    @DeleteMapping("/{id}")
    public void deleteTag(
        @PathVariable("id") String tagId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("Deleting tag {} for user {}", tagId, jwt.getSubject());
        tagService.deleteTag(jwt.getSubject(), tagId);
        log.info("Tag deleted");
    }
}

// DTOs
record CreateTagRequest(String tagName, Set<String> synonyms) {}

record UpdateTagRequest(
    String tagId,
    String tagName,
    Set<String> synsToRemove,
    Set<String> synsToAdd
) {}

record TagDTO(
    String id,
    String name,
    String userId,
    Set<String> synonyms,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
