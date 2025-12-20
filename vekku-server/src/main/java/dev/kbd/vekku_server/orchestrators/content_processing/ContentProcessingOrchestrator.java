package dev.kbd.vekku_server.orchestrators.content_processing;

import dev.kbd.vekku_server.services.brain.model.TagScore;
import dev.kbd.vekku_server.services.brain.interfaces.IBrainService;
import dev.kbd.vekku_server.services.content.interfaces.IContentService;
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

        var content = contentService.getContentInternal(contentId); // Implementing this.

        List<TagScore> tagScores = brainService.getRawTagsByEmbedding(content.getText(), 0.45, 10);

        contentService.saveTagSuggestions(contentId, tagScores, content.getUserId());

        log.info("Successfully generated {} tag suggestions for content: {}", tagScores.size(), contentId);
    }

    private void processKeywords(java.util.UUID contentId) {
        log.info("Extracting keywords for content: {}", contentId);

        var content = contentService.getContentInternal(contentId);

        List<TagScore> keywords = brainService.extractKeywords(content.getText(), 5, 0.5);

        contentService.saveKeywordSuggestions(contentId, keywords, content.getUserId());

        log.info("Successfully extracted {} keywords for content: {}", keywords.size(), contentId);
    }
}
