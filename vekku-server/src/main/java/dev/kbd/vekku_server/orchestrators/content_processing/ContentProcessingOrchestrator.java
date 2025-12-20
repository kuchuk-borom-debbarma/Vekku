package dev.kbd.vekku_server.orchestrators.content_processing;

import dev.kbd.vekku_server.services.brain.IBrainService;
import dev.kbd.vekku_server.services.common.dtos.TagScore;
import dev.kbd.vekku_server.services.content.IContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentProcessingOrchestrator {

    private final IBrainService brainService;
    private final IContentService contentService;

    private final dev.kbd.vekku_server.services.tags.ITagService tagService;

    @RabbitListener(queues = "${vekku.rabbitmq.queue}")
    public void processContent(dev.kbd.vekku_server.shared.events.ContentProcessingEvent event) {
        log.info("Processing event for content: {} with actions: {}", event.getContentId(), event.getActions());

        try {

            if (event.getActions().contains(dev.kbd.vekku_server.shared.events.ContentProcessingAction.SUGGEST_TAGS)) {
                processTags(event.getContentId());
            }

            // Action: SUGGEST_KEYWORDS
            if (event.getActions()
                    .contains(dev.kbd.vekku_server.shared.events.ContentProcessingAction.SUGGEST_KEYWORDS)) {
                processKeywords(event.getContentId());
            }

        } catch (Exception e) {
            log.error("Error processing content: {}", event.getContentId(), e);
        }
    }

    private void processTags(java.util.UUID contentId) {
        log.info("Generating tag suggestions for content: {}", contentId);

        var content = contentService.getContent(contentId); // Implementing this.

        List<TagScore> tagScores = brainService.getRawTagsByEmbedding(content.content(), 0.45, 10);

        List<dev.kbd.vekku_server.services.content.dtos.SuggestedTagDto> suggestedTags = new java.util.ArrayList<>();
        for (TagScore tagScore : tagScores) {
            tagService.getTagByName(tagScore.name(), content.userId()).ifPresent(tag -> {
                suggestedTags.add(
                        new dev.kbd.vekku_server.services.content.dtos.SuggestedTagDto(tag.id(), tagScore.score()));
            });
        }

        contentService.saveTagSuggestions(contentId, suggestedTags, content.userId());

        log.info("Successfully generated {} tag suggestions for content: {}", suggestedTags.size(), contentId);
    }

    private void processKeywords(java.util.UUID contentId) {
        log.info("Extracting keywords for content: {}", contentId);

        var content = contentService.getContent(contentId);

        List<TagScore> keywords = brainService.extractKeywords(content.content(), 5, 0.5);

        contentService.saveKeywordSuggestions(contentId, keywords, content.userId());

        log.info("Successfully extracted {} keywords for content: {}", keywords.size(), contentId);
    }
}
