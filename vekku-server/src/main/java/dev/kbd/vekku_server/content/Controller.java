package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.Models.ContentType;
import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/content")
@RequiredArgsConstructor
@Slf4j
class Controller {

    final IContentService contentService;

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
        //TODO publish event
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
        //TODO publish event
        return contentService.updateContent(jwt.getSubject(), request);
    }

    public void deleteContent(String id, @AuthenticationPrincipal Jwt jwt) {
        log.info(
            "Attempting to delete content {} for user {}",
            id,
            jwt.getSubject()
        );

        //TODO publish event
        contentService.deleteContent(id, jwt.getSubject());
    }
}

record CreateContentRequest(
    String title,
    String content,
    ContentType contentType,
    Set<String> tags
) {}

record UpdateContentRequest(
    String id,
    String updatedTitle,
    String updatedContent,
    ContentType updatedContentType,
    Set<String> toRemoveTags,
    Set<String> toAddTags
) {}
