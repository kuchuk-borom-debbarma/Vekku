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
    private final dev.kbd.vekku_server.repository.TagRepository tagRepository;
    private final dev.kbd.vekku_server.repository.ContentTagRepository contentTagRepository;
    private final dev.kbd.vekku_server.repository.ContentTagSuggestionRepository contentTagSuggestionRepository;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Value("${vekku.rabbitmq.routingkey}")
    private String routingKey;

    public Content createContent(CreateContentRequest request, String userId) {
        log.info("Creating content for user: {}", userId);

        // Save the content in database
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

    public dev.kbd.vekku_server.dto.content.ContentPageDto getAllContent(String userId, Integer limit, String cursor) {
        log.info("Fetching content for user: {}, limit: {}, cursor: {}", userId, limit, cursor);
        int fetchLimit = limit + 1;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                fetchLimit);

        java.util.List<Content> contents;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                java.time.LocalDateTime createdCursor = java.time.LocalDateTime.parse(cursor);
                contents = contentRepository.findByUserIdAndCreatedLessThanOrderByCreatedDesc(userId, createdCursor,
                        pageable);
            } catch (java.time.format.DateTimeParseException e) {
                log.error("Invalid cursor format: {}", cursor);
                contents = java.util.Collections.emptyList();
            }
        } else {
            contents = contentRepository.findAllByUserIdOrderByCreatedDesc(userId, pageable);
        }

        String nextCursor = null;
        if (contents.size() > limit) {
            contents = contents.subList(0, limit);
            nextCursor = contents.get(contents.size() - 1).getCreated().toString();
        }

        return new dev.kbd.vekku_server.dto.content.ContentPageDto(contents, nextCursor);
    }

    public void saveTagsForContent(dev.kbd.vekku_server.dto.content.SaveTagsForContentRequest request, String userId) {
        Content content = contentRepository.findById(java.util.UUID.fromString(request.contentId()))
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.toAddTags() != null) {
            for (String tagIdStr : request.toAddTags()) {
                java.util.UUID tagId = java.util.UUID.fromString(tagIdStr);
                dev.kbd.vekku_server.model.Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new RuntimeException("Tag not found"));

                // Check if already exists
                boolean exists = contentTagRepository.findByContentId(content.getId()).stream()
                        .anyMatch(ct -> ct.getTag().getId().equals(tagId));

                if (!exists) {
                    dev.kbd.vekku_server.model.content.ContentTag contentTag = dev.kbd.vekku_server.model.content.ContentTag
                            .builder()
                            .content(content)
                            .tag(tag)
                            .userId(userId)
                            .build();
                    contentTagRepository.save(contentTag);
                }
            }
        }

        if (request.toRemoveTags() != null) {
            for (String tagIdStr : request.toRemoveTags()) {
                java.util.UUID tagId = java.util.UUID.fromString(tagIdStr);
                // Find and delete
                contentTagRepository.findByContentId(content.getId()).stream()
                        .filter(ct -> ct.getTag().getId().equals(tagId))
                        .findFirst()
                        .ifPresent(contentTagRepository::delete);
            }
        }
    }

    public void refreshSuggestions(java.util.UUID contentId, String userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        log.info("Refreshing suggestions for content: {}", contentId);
        rabbitTemplate.convertAndSend(exchange, routingKey, content);
    }

    public dev.kbd.vekku_server.dto.content.ContentDetailDto getContent(java.util.UUID contentId, String userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        java.util.List<dev.kbd.vekku_server.model.content.ContentTag> manualTags = contentTagRepository
                .findByContentId(contentId);
        java.util.List<dev.kbd.vekku_server.model.content.ContentTagSuggestion> suggestedTags = contentTagSuggestionRepository
                .findByContentId(contentId);

        return dev.kbd.vekku_server.dto.content.ContentDetailDto.builder()
                .content(content)
                .manualTags(manualTags)
                .suggestedTags(suggestedTags)
                .build();
    }
}
