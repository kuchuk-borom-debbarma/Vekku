package dev.kbd.vekku_server.suggestion;

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
class TagRabbitListener {

    private final ISuggestionService suggestionService;

    @RabbitListener(queues = RabbitMQConfig.TAG_CREATION_QUEUE)
    public void handleTagCreatedEvent(TagCreatedEvent event) {
        log.info(
            "Received TagCreatedEvent for tag ID: {}, name: {}, userId: {}",
            event.tagId(),
            event.tagName(),
            event.userId()
        );
        try {
            suggestionService.saveTag(event.tagId(), event.tagName());
        } catch (Exception e) {
            log.error("Error saving tag to VectorStore: {}", event.tagId(), e);
        }
    }
}
