package dev.kbd.vekku_server.services.orchestration;

import dev.kbd.vekku_server.model.Tag;
import dev.kbd.vekku_server.model.content.Content;
import dev.kbd.vekku_server.model.content.ContentTagSuggestion;
import dev.kbd.vekku_server.model.content.ContentKeywordSuggestion;
import dev.kbd.vekku_server.repository.ContentKeywordSuggestionRepository;
import dev.kbd.vekku_server.repository.ContentTagSuggestionRepository;
import dev.kbd.vekku_server.repository.TagRepository;
import dev.kbd.vekku_server.services.brain.model.TagScore;
import dev.kbd.vekku_server.services.core.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentProcessingOrchestrator {

    private final EmbeddingService embeddingService;
    private final TagRepository tagRepository;
    private final ContentTagSuggestionRepository contentTagSuggestionRepository;
    private final ContentKeywordSuggestionRepository contentKeywordSuggestionRepository;

    private final dev.kbd.vekku_server.repository.ContentRepository contentRepository;

    @RabbitListener(queues = "${vekku.rabbitmq.queue}")
    public void processContent(dev.kbd.vekku_server.event.ContentProcessingEvent event) {
        log.info("Processing event for content: {} with actions: {}", event.getContentId(), event.getActions());

        Content content = contentRepository.findById(event.getContentId()).orElse(null);
        if (content == null) {
            log.warn("Content not found for processing: {}", event.getContentId());
            return;
        }

        try {
            // Action: SUGGEST_TAGS
            if (event.getActions().contains(dev.kbd.vekku_server.event.ContentProcessingAction.SUGGEST_TAGS)) {
                processTags(content);
            }

            // Action: SUGGEST_KEYWORDS
            if (event.getActions().contains(dev.kbd.vekku_server.event.ContentProcessingAction.SUGGEST_KEYWORDS)) {
                processKeywords(content);
            }

        } catch (Exception e) {
            log.error("Error processing content: {}", content.getId(), e);
        }
    }

    private void processTags(Content content) {
        log.info("Generating tag suggestions for content: {}", content.getId());
        contentTagSuggestionRepository.deleteByContentId(content.getId());

        List<TagScore> tagScores = embeddingService.getRawTagsByEmbedding(content.getText(), 0.45, 10);

        for (TagScore tagScore : tagScores) {
            String tagName = tagScore.name();
            Double score = tagScore.score();
            String userId = content.getUserId();

            // Get the id of the tag
            Tag tag = tagRepository.findByNameAndUserId(tagName, userId)
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
        log.info("Successfully generated {} tag suggestions for content: {}", tagScores.size(), content.getId());
    }

    private void processKeywords(Content content) {
        log.info("Extracting keywords for content: {}", content.getId());
        contentKeywordSuggestionRepository.deleteByContentId(content.getId());

        List<TagScore> keywords = embeddingService.extractKeywords(content.getText(), 5, 0.5);

        for (TagScore keyword : keywords) {
            ContentKeywordSuggestion suggestion = ContentKeywordSuggestion.builder()
                    .content(content)
                    .keyword(keyword.name())
                    .score(keyword.score())
                    .userId(content.getUserId())
                    .build();
            contentKeywordSuggestionRepository.save(suggestion);
        }
        log.info("Successfully extracted {} keywords for content: {}", keywords.size(), content.getId());
    }
}
