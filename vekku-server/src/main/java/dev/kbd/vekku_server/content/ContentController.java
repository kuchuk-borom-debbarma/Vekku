package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import dev.kbd.vekku_server.content.api.ContentDTOs.CreateContentRequest;
import dev.kbd.vekku_server.content.api.ContentDTOs.UpdateContentRequest;
import dev.kbd.vekku_server.content.api.IContentService;
import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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

        //TODO message broker
        CompletableFuture.runAsync(() -> {
            suggestionService.createSuggestionsForContent(
                content.id().toString(),
                content.content(),
                0.45,
                10
            ); //TODO dynamic number of tags based on count of content
        });

        return content;
    }

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

        CompletableFuture.runAsync(() -> {
            suggestionService.createSuggestionsForContent(
                content.id().toString(),
                content.content(),
                0.45,
                10
            ); //TODO dynamic number of tags based on count of content
        });
        return content;
    }

    public void deleteContent(String id, @AuthenticationPrincipal Jwt jwt) {
        log.info(
            "Attempting to delete content {} for user {}",
            id,
            jwt.getSubject()
        );

        //TODO publish event
        suggestionService.deleteSuggestionsOfContent(id);
        contentService.deleteContent(id, jwt.getSubject());
    }

    public ContentDTO getContent(String id, @AuthenticationPrincipal Jwt jwt) {
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
