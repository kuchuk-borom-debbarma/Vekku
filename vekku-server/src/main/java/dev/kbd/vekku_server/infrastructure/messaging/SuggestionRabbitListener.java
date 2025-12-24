package dev.kbd.vekku_server.infrastructure.messaging;

import dev.kbd.vekku_server.infrastructure.config.RabbitMQConfig;
import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import dev.kbd.vekku_server.tag.api.TagEvents.TagCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuggestionRabbitListener {

    private final ISuggestionService suggestionService;

    @RabbitListener(queues = RabbitMQConfig.TAG_QUEUE)
    public void handleTagCreatedEvent(TagCreatedEvent event) {
        log.info("RabbitListener received TagCreatedEvent for tag ID: {}", event.tagId());
        suggestionService.handleTagCreatedEvent(event);
    }
}
