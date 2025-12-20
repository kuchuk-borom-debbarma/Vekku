package dev.kbd.vekku_server.services.content.impl;

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
// ITagService import removed
// Tag import removed
import dev.kbd.vekku_server.services.tags.impl.entities.TagEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService implements IContentService {

    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;

    // TODO let tag handle tag related stuff.
    private final ContentTagRepository contentTagRepository;
    private final ContentTagSuggestionRepository contentTagSuggestionRepository;
    private final ContentKeywordSuggestionRepository contentKeywordSuggestionRepository;

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

        return contentMapper.toContent(savedContent);
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
                contents = Collections.emptyList();
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

                // Check if already exists
                boolean exists = contentTagRepository.findByContentId(content.getId()).stream()
                        .anyMatch(ct -> ct.getTag().getId().equals(tagId));

                if (!exists) {
                    ContentTagEntity contentTag = ContentTagEntity.builder()
                            .content(content)
                            .tag(TagEntity.builder().id(tagId).build())
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
            List<dev.kbd.vekku_server.services.content.dtos.SuggestedTagDto> tagScores, String userId) {
        // Find content to ensure ownership and existence
        ContentEntity content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        if (!content.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        contentTagSuggestionRepository.deleteByContentId(contentId);

        for (dev.kbd.vekku_server.services.content.dtos.SuggestedTagDto tagScore : tagScores) {
            java.util.UUID tagId = tagScore.tagId();
            Double score = tagScore.score();

            // Save ContentTagSuggestion
            ContentTagSuggestionEntity contentTag = ContentTagSuggestionEntity.builder()
                    .content(content)
                    .tag(TagEntity.builder().id(tagId).build())
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
