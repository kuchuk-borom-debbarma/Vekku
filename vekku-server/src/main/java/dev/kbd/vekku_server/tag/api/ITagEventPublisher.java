package dev.kbd.vekku_server.tag.api;

import dev.kbd.vekku_server.tag.api.TagEvents.TagCreatedEvent;

public interface ITagEventPublisher {
    void publishTagCreated(TagCreatedEvent event);
}
