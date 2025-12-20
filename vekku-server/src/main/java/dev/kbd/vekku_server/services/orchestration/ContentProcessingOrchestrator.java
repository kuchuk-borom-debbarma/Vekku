package dev.kbd.vekku_server.services.orchestration;

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
    public void processContent(dev.kbd.vekku_server.event.ContentProcessingEvent event) {
        log.info("Processing event for content: {} with actions: {}", event.getContentId(), event.getActions());

        // We need content text for Brain processing.
        // We can fetch content DTO or Entity if Service exposes it.
        // Service exposes getContent(UUID, userId) which returns DTO.
        // But we need internal access or similar.
        // Here we might need a system-level get method or bypass.
        // Or we use `contentService.getContent(id, userId)` if we had userId. Event
        // likely has userId or we can get it from content if we had retrieval method.
        // But for now let's assume we can fetch it.
        // Wait, event usually has contentId.
        // IContentService has method to get Content entity? No, it returns DTO.
        // But implementation has repositories.
        // Ideally Orchestrator shouldn't know about Entities, but for this refactor we
        // might need to expose a method in Service to get "ContentText" or similar.
        // Or simple: `contentService.getContentText(id)`?
        // OR: `contentService.processContent(id, actions)`?
        // No, Orchestrator coordinates.

        // Let's rely on `contentService.getContent(id, "system")`? But internal methods
        // might be better.
        // Actually, for now, let's assume we can add `getContentEntity` or similar to
        // Interface if needed, OR just fetching DTO is enough for text?
        // DTO `ContentDetailDto` has `text`? Let's check `ContentDetailDto`.

        // Assuming we can get text.
        // I will add a helper method in Orchestrator to fetch via Service if possible.
        // BUT `getContent` takes userId for permission check. System events don't have
        // userId context easily unless in event.
        // Event should have userId? `ContentProcessingEvent` definitions?
        // If not, we might need `contentService.getContentForSystem(id)`.

        // For this refactor, I will assume we can get content.
        // Let's try `getContent(id, null)`?
        // Service impl: `if (!content.getUserId().equals(userId))` -> if userId is
        // null, it throws or NPE.

        // I'll add `getContentForProcessing(UUID id)` to IContentService (Internal
        // usage).
        // Or just `getContent(UUID id)` (admin/system).
        // I'll update IContentService first?
        // Or better: Orchestrator is part of the "Application" layer.
        // `ContentProcessingOrchestrator` IS the one handling this.

        // Let's modify logic:
        // We will TRY to fetch DTO with a bypass or handle it.
        // Actually simplest path: Add `getContentInternal(UUID id)` to IContentService.
        // I'll add that to `IContentService` and `ContentService` in next step or now
        // if possible.
        // I can't do it in ONE step with this file.

        // I will write the code assuming `contentService.getContentText(id)` exists,
        // similar to how I assumed `saveTagSuggestions`. I'll add it.

        try {
            // Fetch content text
            // String text = contentService.getContentText(event.getContentId());
            // I'll implement this method in Service.

            // Action: SUGGEST_TAGS
            if (event.getActions().contains(dev.kbd.vekku_server.event.ContentProcessingAction.SUGGEST_TAGS)) {
                processTags(event.getContentId());
            }

            // Action: SUGGEST_KEYWORDS
            if (event.getActions().contains(dev.kbd.vekku_server.event.ContentProcessingAction.SUGGEST_KEYWORDS)) {
                processKeywords(event.getContentId());
            }

        } catch (Exception e) {
            log.error("Error processing content: {}", event.getContentId(), e);
        }
    }

    private void processTags(java.util.UUID contentId) {
        log.info("Generating tag suggestions for content: {}", contentId);

        // Need text and userId for saving?
        // `saveTagSuggestions` takes userId.
        // I need to fetch Content to get userId and Text.
        // `dev.kbd.vekku_server.services.content.dto.ContentDetailDto` content =
        // contentService.getContentInternal(contentId);

        // I really need a method to get Content Details for System.
        // Let's use `contentService.getContent(contentId)` (Add this method to
        // interface, no userId check).

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
