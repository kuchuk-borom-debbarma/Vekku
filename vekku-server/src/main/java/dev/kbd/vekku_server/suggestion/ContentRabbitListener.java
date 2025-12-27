package dev.kbd.vekku_server.suggestion;

import dev.kbd.vekku_server.content.api.ContentEvents.ContentCreatedEvent;
import dev.kbd.vekku_server.infrastructure.config.RabbitMQConfig;
import dev.kbd.vekku_server.suggestion.api.ISuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class ContentRabbitListener {

    private final ISuggestionService suggestionService;

    @RabbitListener(queues = RabbitMQConfig.CONTENT_CREATION_QUEUE)
    public void handleContentCreatedEvent(ContentCreatedEvent event) {
        log.info(
            "Received ContentCreatedEvent for content ID: {}, userId: {}",
            event.contentId(),
            event.userId()
        );
        try {
            suggestionService.createSuggestionsForContent(
                event.contentId(),
                event.content(),
                0.45, // Default threshold
                10 // Default count
            );
            log.info(
                "Suggestions created for content ID: {}",
                event.contentId()
            );
        } catch (Exception e) {
            log.error(
                "Error creating suggestions for content ID: {}",
                event.contentId(),
                e
            );
        }
    }
}
