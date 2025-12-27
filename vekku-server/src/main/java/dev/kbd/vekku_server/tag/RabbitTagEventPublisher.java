package dev.kbd.vekku_server.tag;

import dev.kbd.vekku_server.tag.api.ITagEventPublisher;
import dev.kbd.vekku_server.tag.api.TagEvents;
import dev.kbd.vekku_server.tag.api.TagEvents.TagCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitTagEventPublisher implements ITagEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Override
    public void publishTagCreated(TagCreatedEvent event) {
        log.info("Publishing TagCreatedEvent for tag ID: {}", event.tagId());
        rabbitTemplate.convertAndSend(
            exchange,
            TagEvents.TAG_CREATED,
            event
        );
    }
}
