package dev.kbd.vekku_server.content.api;

import dev.kbd.vekku_server.content.api.ContentEvents.ContentCreatedEvent;

public interface IContentEventPublisher {
    void publishContentCreated(ContentCreatedEvent event);
}
