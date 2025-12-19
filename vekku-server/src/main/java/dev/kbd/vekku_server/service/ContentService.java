package dev.kbd.vekku_server.service;

import dev.kbd.vekku_server.dto.content.CreateContentRequest;
import dev.kbd.vekku_server.model.content.Content;
import dev.kbd.vekku_server.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final ContentRepository contentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Value("${vekku.rabbitmq.routingkey}")
    private String routingKey;

    public Content createContent(CreateContentRequest request, String userId) {
        log.info("Creating content for user: {}", userId);

        Content content = Content.builder()
                .text(request.getText())
                .type(request.getType())
                .userId(userId)
                .build();

        if (content == null) {
            throw new RuntimeException("Content cannot be null");
        }

        Content savedContent = contentRepository.save(content);

        // Publish to RabbitMQ
        log.info("Publishing content creation event for contentId: {}", savedContent.getId());
        rabbitTemplate.convertAndSend(exchange, routingKey, savedContent);

        return savedContent;
    }
}
