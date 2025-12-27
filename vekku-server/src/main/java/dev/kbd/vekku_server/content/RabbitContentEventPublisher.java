package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentEvents;
import dev.kbd.vekku_server.content.api.ContentEvents.ContentCreatedEvent;
import dev.kbd.vekku_server.content.api.IContentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitContentEventPublisher implements IContentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Override
    public void publishContentCreated(ContentCreatedEvent event) {
        log.info("Publishing ContentCreatedEvent for content ID: {}", event.contentId());
        rabbitTemplate.convertAndSend(
            exchange,
            ContentEvents.CONTENT_CREATED,
            event
        );
    }
}
