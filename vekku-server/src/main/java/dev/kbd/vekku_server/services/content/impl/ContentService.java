package dev.kbd.vekku_server.services.content.impl;

import dev.kbd.vekku_server.services.content.model.Content;
import dev.kbd.vekku_server.services.content.model.ContentTag;
import dev.kbd.vekku_server.services.content.model.ContentTagSuggestion;
import dev.kbd.vekku_server.services.content.model.ContentKeywordSuggestion;
import dev.kbd.vekku_server.services.content.repo.ContentRepository;
import dev.kbd.vekku_server.services.content.repo.ContentTagRepository;
import dev.kbd.vekku_server.services.content.repo.ContentTagSuggestionRepository;
import dev.kbd.vekku_server.services.content.repo.ContentKeywordSuggestionRepository;
import dev.kbd.vekku_server.controllers.content.models.ContentDetailDto;
import dev.kbd.vekku_server.controllers.content.models.ContentPageDto;
import dev.kbd.vekku_server.controllers.content.models.CreateContentRequest;
import dev.kbd.vekku_server.controllers.content.models.SaveTagsForContentRequest;
import dev.kbd.vekku_server.services.content.interfaces.IContentService;
import dev.kbd.vekku_server.services.tags.interfaces.ITagService;
import dev.kbd.vekku_server.services.tags.model.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService implements IContentService {

    private final ContentRepository contentRepository;
    private final RabbitTemplate rabbitTemplate;
    // Replace TagRepository with ITagService for independence
    private final ITagService tagService;
    // Remove EmbeddingService dependency
    // private final EmbeddingService embeddingService;

    private final ContentTagRepository contentTagRepository;
    private final ContentTagSuggestionRepository contentTagSuggestionRepository;
    private final ContentKeywordSuggestionRepository contentKeywordSuggestionRepository;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Value("${vekku.rabbitmq.routingkey}")
    private String routingKey;

    @Override
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
        dev.kbd.vekku_server.shared.events.ContentProcessingEvent event = dev.kbd.vekku_server.shared.events.ContentProcessingEvent
                .builder()
                .contentId(savedContent.getId())
                .actions(java.util.EnumSet.allOf(dev.kbd.vekku_server.shared.events.ContentProcessingAction.class))
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        return savedContent;
    }

    @Override
    public ContentPageDto getAllContent(String userId, Integer limit, String cursor) {
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

        return new ContentPageDto(contents, nextCursor);
    }

    @Override
    public void saveTagsForContent(SaveTagsForContentRequest request, String userId) {
        Content content = contentRepository.findById(java.util.UUID.fromString(request.contentId()))
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.toAddTags() != null) {
            for (String tagIdStr : request.toAddTags()) {
                java.util.UUID tagId = java.util.UUID.fromString(tagIdStr);
                // Use tagService instead of direct repository access
                Tag tag = tagService.getTag(tagId); // Throws if not found

                // Check if already exists
                boolean exists = contentTagRepository.findByContentId(content.getId()).stream()
                        .anyMatch(ct -> ct.getTag().getId().equals(tagId));

                if (!exists) {
                    ContentTag contentTag = ContentTag.builder()
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

    // New method required by interface, redirect to saveTags logic or keep as is?
    // Interface defines addTag and removeTag. I should implement them.
    @Override
    public void addTagToContent(java.util.UUID contentId, java.util.UUID tagId, String userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        Tag tag = tagService.getTag(tagId);

        boolean exists = contentTagRepository.findByContentId(content.getId()).stream()
                .anyMatch(ct -> ct.getTag().getId().equals(tagId));

        if (!exists) {
            ContentTag contentTag = ContentTag.builder()
                    .content(content)
                    .tag(tag)
                    .userId(userId)
                    .build();
            contentTagRepository.save(contentTag);
        }
    }

    @Override
    public void removeTagFromContent(java.util.UUID contentId, java.util.UUID tagId, String userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        contentTagRepository.findByContentId(content.getId()).stream()
                .filter(ct -> ct.getTag().getId().equals(tagId))
                .findFirst()
                .ifPresent(contentTagRepository::delete);
    }

    @Override
    public void refreshSuggestions(java.util.UUID contentId, String userId,
            java.util.Set<dev.kbd.vekku_server.shared.events.ContentProcessingAction> actions) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        log.info("Refreshing suggestions for content: {} with actions: {}", contentId, actions);
        dev.kbd.vekku_server.shared.events.ContentProcessingEvent event = dev.kbd.vekku_server.shared.events.ContentProcessingEvent
                .builder()
                .contentId(content.getId())
                .actions(actions)
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    @Override
    public ContentDetailDto getContent(java.util.UUID contentId, String userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        java.util.List<ContentTag> manualTags = contentTagRepository
                .findByContentId(contentId);
        java.util.List<ContentTagSuggestion> suggestedTags = contentTagSuggestionRepository
                .findByContentId(contentId);

        return ContentDetailDto.builder()
                .content(content)
                .manualTags(manualTags)
                .suggestedTags(suggestedTags)
                .build();
    }

    @Override
    public java.util.List<ContentKeywordSuggestion> getContentKeywords(
            java.util.UUID contentId, String userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return contentKeywordSuggestionRepository.findByContentId(contentId);
    }

    @Override
    public void saveTagSuggestions(java.util.UUID contentId,
            java.util.List<dev.kbd.vekku_server.services.brain.dto.TagScore> scores, String userId) {
        // Find content to ensure ownership and existence
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized"); // Or log warning if system process? Usually system process has
                                                        // userId context or bypass.
            // Orchestrator calls this. Orchestrator gets content from repo.
            // If Orchestrator runs async (RabbitMQ), userId verification might be implicit
            // by content ownership.
            // We'll enforce ownership check against passed userId.
        }

        contentTagSuggestionRepository.deleteByContentId(contentId);

        for (dev.kbd.vekku_server.services.brain.dto.TagScore tagScore : scores) {
            String tagName = tagScore.name();
            Double score = tagScore.score();

            // Get the id of the tag via TagService
            Tag tag = tagService.getTagByName(tagName, userId)
                    .orElseThrow(() -> new RuntimeException("Tag not found for name: " + tagName));

            // Save ContentTagSuggestion
            ContentTagSuggestion contentTag = ContentTagSuggestion.builder()
                    .content(content)
                    .tag(tag)
                    .score(score)
                    .userId(userId)
                    .build();

            contentTagSuggestionRepository.save(contentTag);
        }
    }

    @Override
    public void saveKeywordSuggestions(java.util.UUID contentId,
            java.util.List<dev.kbd.vekku_server.services.brain.dto.TagScore> keywords, String userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        contentKeywordSuggestionRepository.deleteByContentId(contentId);

        for (dev.kbd.vekku_server.services.brain.dto.TagScore keyword : keywords) {
            ContentKeywordSuggestion suggestion = ContentKeywordSuggestion.builder()
                    .content(content)
                    .keyword(keyword.name())
                    .score(keyword.score())
                    .userId(userId)
                    .build();
            contentKeywordSuggestionRepository.save(suggestion);
        }
    }

    @Override
    public Content getContentInternal(java.util.UUID id) {
        return contentRepository.findById(id).orElseThrow(() -> new RuntimeException("Content not found"));
    }
}
