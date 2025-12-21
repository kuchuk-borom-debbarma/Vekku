package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.api.ITagContentService;
import dev.kbd.vekku_server.tag.api.TagDTOs.LinkTagsToContentRequest;
import dev.kbd.vekku_server.tag.api.TagDTOs.UnlinkTagsFromContentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tag-content")
@Slf4j
@RequiredArgsConstructor
class TagContentController {

    final ITagContentService tagContentService;

    public void linkTagsToContent(
        @RequestBody LinkTagsToContentRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        tagContentService.linkTagsToContent(request, jwt.getSubject());
    }

    public void unlinkTagsFromContent(
        @RequestBody UnlinkTagsFromContentRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        tagContentService.unlinkTagsFromContent(request, jwt.getSubject());
    }
}
