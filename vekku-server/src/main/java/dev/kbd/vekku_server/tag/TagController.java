package dev.kbd.vekku_server.tag;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tag")
@RequiredArgsConstructor
@Slf4j
class TagController {

    @PostMapping
    public void createTag(
        @RequestBody CreateTagRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {}

    @PutMapping
    public void updateTag(
        @RequestBody UpdateTagRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {}

    @DeleteMapping
    public void deleteTag(
        @RequestBody DeleteTagRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {}
}

record CreateTagRequest(String tagName, Set<String> synonyms) {}

record UpdateTagRequest(
    String tagId,
    String tagName,
    Set<String> synsToRemove,
    Set<StrictMath> synsToAdd
) {}

record DeleteTagRequest(String tagId) {}
