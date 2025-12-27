package dev.kbd.vekku_server.suggestion;

import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/suggestions")
@RequiredArgsConstructor
@Slf4j
class SuggestionController {

    private final ISuggestionService suggestionService;

    Map<String, Double> getSuggestionsOfContent(
        String contentId,
        String fromCursor,
        int limit,
        String direction,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return suggestionService.getSuggestionsOfContent(
            contentId,
            fromCursor,
            limit,
            direction,
            jwt.getSubject()
        );
    }
}
