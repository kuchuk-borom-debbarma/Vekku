package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import dev.kbd.vekku_server.content.api.ContentDTOs.CreateContentRequest;
import dev.kbd.vekku_server.content.api.ContentDTOs.UpdateContentRequest;
import dev.kbd.vekku_server.content.api.IContentService;
import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("api/v1/content")
@RequiredArgsConstructor
@Slf4j
class ContentController {

    final IContentService contentService;
    final ISuggestionService suggestionService;

    /**
     * Create content and publishes event about it
     * @param request
     * @param jwt
     * @return
     */
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED, reason = "Created content")
    public ContentDTO createContent(
        @RequestBody CreateContentRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("Creating content for {} : {}", jwt.getSubject(), request);
        ContentDTO content = contentService.createContent(
            jwt.getSubject(),
            request
        );
        log.info(
            "Created content with id {} for user {}",
            content.id(),
            jwt.getSubject()
        );
        return content;
    }

    @PutMapping
    public ContentDTO updateContent(
        @RequestBody UpdateContentRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        log.info(
            "Updating content of user {} with {}",
            jwt.getSubject(),
            request
        );

        var content = contentService.updateContent(jwt.getSubject(), request);
        return content;
    }

    @DeleteMapping("/{id}")
    public void deleteContent(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        log.info(
            "Attempting to delete content {} for user {}",
            id,
            jwt.getSubject()
        );

        //TODO publish event
        suggestionService.deleteSuggestionsOfContent(id);
        contentService.deleteContent(id, jwt.getSubject());
    }

    @GetMapping("/{id}")
    public ContentDTO getContent(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        ContentDTO content = contentService.getContentOfUser(id, userId);
        if (content == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Content for user not found"
            );
        }
        return content;
    }

    @GetMapping
    public List<ContentDTO> getContents(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false, defaultValue = "10") int limit,
        @RequestParam(required = false, defaultValue = "next") String direction,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        return contentService.getContentsOfUser(
            userId,
            cursor,
            limit,
            direction
        );
    }
}
