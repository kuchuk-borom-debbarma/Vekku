package dev.kbd.vekku_server.orchestrators.content;

import dev.kbd.vekku_server.services.content.IContentService;
import dev.kbd.vekku_server.services.content.dtos.Content;
import dev.kbd.vekku_server.services.content.dtos.CreateContentParam;
import dev.kbd.vekku_server.shared.events.ContentProcessingAction;
import dev.kbd.vekku_server.shared.events.ContentProcessingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentOrchestrator {

    private final IContentService contentService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Value("${vekku.rabbitmq.routingkey}")
    private String routingKey;

    public Content createContent(CreateContentParam request, String userId) {
        // Delegate to service to create content in DB
        Content content = contentService.createContent(request, userId);
        if (content != null) {
            // Publish event for background processing
            publishContentCreatedEvent(content);
        }

        return content;
    }

    private void publishContentCreatedEvent(Content content) {
        log.info("Publishing content creation event for contentId: {}", content.id());
        ContentProcessingEvent event = ContentProcessingEvent.builder()
                .contentId(content.id())
                .actions(EnumSet.allOf(ContentProcessingAction.class))
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void refreshSuggestions(java.util.UUID contentId, String userId,
            java.util.Set<ContentProcessingAction> actions) {
        // Validate content existence and ownership
        contentService.getContent(contentId, userId);

        log.info("Refreshing suggestions for content: {} with actions: {}", contentId, actions);
        ContentProcessingEvent event = ContentProcessingEvent.builder()
                .contentId(contentId)
                .actions(actions)
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
