package dev.kbd.vekku_server.tag.api;

public class TagEvents {

    private TagEvents() {}

    public record TagCreatedEvent(
        String tagId,
        String tagName,
        String userId
    ) {}
}
