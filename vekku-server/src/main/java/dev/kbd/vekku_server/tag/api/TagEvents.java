package dev.kbd.vekku_server.tag.api;

public class TagEvents {

    public static final String TAG_CREATED = "tag.created";

    private TagEvents() {}

    public record TagCreatedEvent(
        String tagId,
        String tagName,
        String userId
    ) {}
}
