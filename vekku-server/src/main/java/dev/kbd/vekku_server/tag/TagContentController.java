package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.api.ITagContentService;
import dev.kbd.vekku_server.tag.api.TagDTOs.LinkTagsToContentRequest;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagContentDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.TagDTO;
import dev.kbd.vekku_server.tag.api.TagDTOs.UnlinkTagsFromContentRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tag-content")
@Slf4j
@RequiredArgsConstructor
class TagContentController {

    final ITagContentService tagContentService;

    @GetMapping("/{id}")
    public ResponseEntity<TagContentDTO> getTagContent(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            tagContentService.getTagContent(id, jwt.getSubject())
        );
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> getTagsOfContent(
        @RequestParam(required = true) String contentId,
        @RequestParam(required = false) String from,
        @RequestParam(required = false, defaultValue = "10") int limit,
        @RequestParam(required = false, defaultValue = "next") String direction,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            tagContentService.getTagsOfContent(
                contentId,
                from,
                limit,
                direction,
                jwt.getSubject()
            )
        );
    }

    @PostMapping
    public void linkTagsToContent(
        @RequestBody LinkTagsToContentRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        tagContentService.linkTagsToContent(request, jwt.getSubject());
    }

    @PutMapping
    public void unlinkTagsFromContent(
        @RequestBody UnlinkTagsFromContentRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        tagContentService.unlinkTagsFromContent(request, jwt.getSubject());
    }
}
