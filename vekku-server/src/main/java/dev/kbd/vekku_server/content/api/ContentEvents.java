package dev.kbd.vekku_server.content.api;

import java.util.Set;

public class ContentEvents {

    public static final String CONTENT_CREATED = "content.created";

    private ContentEvents() {}

    public record ContentCreatedEvent(
        String contentId,
        String userId,
        String content,
        Set<String> tags
    ) {}
}
