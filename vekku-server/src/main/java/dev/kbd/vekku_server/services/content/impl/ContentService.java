package dev.kbd.vekku_server.services.content.impl;

import dev.kbd.vekku_server.services.common.dtos.TagScore;
import dev.kbd.vekku_server.services.content.IContentService;
import dev.kbd.vekku_server.services.content.dtos.Content;
import dev.kbd.vekku_server.services.content.dtos.ContentDetail;
import dev.kbd.vekku_server.services.content.dtos.ContentKeywordSuggestion;
import dev.kbd.vekku_server.services.content.dtos.ContentPage;
import dev.kbd.vekku_server.services.content.dtos.CreateContentParam;
import dev.kbd.vekku_server.services.content.dtos.SaveTagsForContentParam;
import dev.kbd.vekku_server.services.content.impl.entities.ContentEntity;
import dev.kbd.vekku_server.services.content.impl.entities.ContentKeywordSuggestionEntity;
import dev.kbd.vekku_server.services.content.impl.entities.ContentTagEntity;
import dev.kbd.vekku_server.services.content.impl.entities.ContentTagSuggestionEntity;
import dev.kbd.vekku_server.services.content.impl.mappers.ContentMapper;
import dev.kbd.vekku_server.services.content.impl.repo.ContentKeywordSuggestionRepository;
import dev.kbd.vekku_server.services.content.impl.repo.ContentRepository;
import dev.kbd.vekku_server.services.content.impl.repo.ContentTagRepository;
import dev.kbd.vekku_server.services.content.impl.repo.ContentTagSuggestionRepository;
import dev.kbd.vekku_server.services.tags.interfaces.ITagService;
import dev.kbd.vekku_server.services.tags.model.Tag;
import dev.kbd.vekku_server.shared.events.ContentProcessingAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService implements IContentService {

    // TODO remove ITagService dependency. It should be done using orchestrators

    private final ContentRepository contentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ITagService tagService;
    private final ContentMapper contentMapper;

    private final ContentTagRepository contentTagRepository;
    private final ContentTagSuggestionRepository contentTagSuggestionRepository;
    private final ContentKeywordSuggestionRepository contentKeywordSuggestionRepository;

    @Value("${vekku.rabbitmq.exchange}")
    private String exchange;

    @Value("${vekku.rabbitmq.routingkey}")
    private String routingKey;

    @Override
    public Content createContent(CreateContentParam request, String userId) {
        log.info("Creating content for user: {}", userId);

        // Save the content in database
        ContentEntity content = ContentEntity.builder()
                .content(request.getText())
                .contentType(request.getType())
                .userId(userId)
                .build();

        if (content == null) {
            throw new RuntimeException("Content cannot be null");
        }

        ContentEntity savedContent = contentRepository.save(content);

        // Publish to RabbitMQ
        log.info("Publishing content creation event for contentId: {}", savedContent.getId());
        dev.kbd.vekku_server.shared.events.ContentProcessingEvent event = dev.kbd.vekku_server.shared.events.ContentProcessingEvent
                .builder()
                .contentId(savedContent.getId())
                .actions(java.util.EnumSet.allOf(dev.kbd.vekku_server.shared.events.ContentProcessingAction.class))
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        return contentMapper.toContent(content);
    }

    @Override
    public ContentPage getAllContent(String userId, Integer limit, String cursor) {
        log.info("Fetching content for user: {}, limit: {}, cursor: {}", userId, limit, cursor);
        int fetchLimit = limit + 1;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                fetchLimit);

        java.util.List<ContentEntity> contents;
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
            nextCursor = contents.get(contents.size() - 1).getCreatedAt().toString();
        }

        return new ContentPage(contents.stream().map(contentMapper::toContent).collect(Collectors.toList()),
                nextCursor);
    }

    @Override
    public void updateTagsOfContent(SaveTagsForContentParam request, String userId) {
        ContentEntity content = contentRepository.findById(java.util.UUID.fromString(request.contentId()))
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
                    ContentTagEntity contentTag = ContentTagEntity.builder()
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

    @Override
    public void refreshSuggestions(UUID contentId, String userId,
            Set<ContentProcessingAction> actions) {
        ContentEntity content = contentRepository.findById(contentId)
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
    public ContentDetail getContent(UUID contentId, String userId) {
        ContentEntity content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        List<ContentTagEntity> manualTags = contentTagRepository
                .findByContentId(contentId);
        List<ContentTagSuggestionEntity> suggestedTags = contentTagSuggestionRepository
                .findByContentId(contentId);

        return ContentDetail.builder()
                .content(contentMapper.toContent(content))
                .manualTags(manualTags)
                .suggestedTags(suggestedTags)
                .build();
    }

    @Override
    public List<ContentKeywordSuggestion> getContentKeywords(
            UUID contentId, String userId) {
        ContentEntity content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        var entities = contentKeywordSuggestionRepository.findByContentId(contentId);
        return entities.stream().map(contentMapper::toContentKeywordSuggestion).toList();
    }

    @Override
    public void saveTagSuggestions(UUID contentId,
            List<TagScore> scores, String userId) {
        // Find content to ensure ownership and existence
        ContentEntity content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        contentTagSuggestionRepository.deleteByContentId(contentId);

        for (dev.kbd.vekku_server.services.common.dtos.TagScore tagScore : scores) {
            String tagName = tagScore.name();
            Double score = tagScore.score();

            // Get the id of the tag via TagService
            Tag tag = tagService.getTagByName(tagName, userId)
                    .orElseThrow(() -> new RuntimeException("Tag not found for name: " + tagName));

            // Save ContentTagSuggestion
            ContentTagSuggestionEntity contentTag = ContentTagSuggestionEntity.builder()
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
            java.util.List<dev.kbd.vekku_server.services.common.dtos.TagScore> keywords, String userId) {
        ContentEntity content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        contentKeywordSuggestionRepository.deleteByContentId(contentId);

        for (dev.kbd.vekku_server.services.common.dtos.TagScore keyword : keywords) {
            ContentKeywordSuggestionEntity suggestion = ContentKeywordSuggestionEntity.builder()
                    .content(content)
                    .keyword(keyword.name())
                    .score(keyword.score())
                    .userId(userId)
                    .build();
            contentKeywordSuggestionRepository.save(suggestion);
        }
    }

    @Override
    public Content getContent(java.util.UUID id) {
        return contentMapper
                .toContent(contentRepository.findById(id).orElseThrow(() -> new RuntimeException("Content not found")));
    }
}
