package dev.kbd.vekku_server.services.content.interfaces;

import dev.kbd.vekku_server.services.content.dto.ContentDetailDto;
import dev.kbd.vekku_server.services.content.dto.ContentPageDto;
import dev.kbd.vekku_server.services.content.dto.CreateContentRequest;
import dev.kbd.vekku_server.services.content.dto.SaveTagsForContentRequest;
import dev.kbd.vekku_server.services.content.model.Content;
import dev.kbd.vekku_server.services.content.model.ContentKeywordSuggestion;
import dev.kbd.vekku_server.event.ContentProcessingAction; // Assuming this exists or needs moving?
import java.util.List;
import java.util.UUID;
import java.util.Set;

public interface IContentService {
    Content createContent(CreateContentRequest request, String userId);

    ContentPageDto getAllContent(String userId, Integer limit, String cursor);

    ContentDetailDto getContent(UUID contentId, String userId);

    // This allows orchestrator to link tags.
    // Ideally we should have a cleaner way, but for now we follow existing logic's
    // needs.
    // If we move logic to Orchestrator, this method might change or disappear from
    // Service.
    // But for the FIRST step of refactoring, keeping the method signature but
    // cleaning internals is easier.
    // HOWEVER, the goal is to decouple.
    // If I keep saveTagsForContent here, IContentService depends on Tag logic
    // (indirectly via DTO).
    // The implementations of this method will change.

    void saveTagsForContent(SaveTagsForContentRequest request, String userId); // Orchestrator will call this? NO,
                                                                               // Orchestrator IS the one calling logic.
    // Actually, Controller calls this.
    // If I move logic to Orchestrator, Controller should call Orchestrator.
    // So IContentService MIGHT NOT need this.
    // But `ContentService` (Impl) has it.
    // Let's keep it for now but I will REFACTOR the implementation to leverage
    // Orchestrator or be cleaner.
    // Wait, if I move logic to Orchestrator, then ContentService shouldn't have it.
    // I'll keep it for now to minimize compilation errors in Controller,
    // BUT I will mark it as deprecated or planned for removal if I can.
    // Actually, better to expose `addTag` and `removeTag` and let Orchestrator
    // loop.

    void addTagToContent(UUID contentId, UUID tagId, String userId);

    void removeTagFromContent(UUID contentId, UUID tagId, String userId);

    void refreshSuggestions(UUID contentId, String userId, Set<ContentProcessingAction> actions);

    List<ContentKeywordSuggestion> getContentKeywords(UUID contentId, String userId);

    // extractedKeywordsOnDemand was a proxy to BrainService.
    // I should REMOVE it from IContentService and let Controller call IBrainService
    // directly.
    // So I won't add it here.

    // Suggestion management
    void saveTagSuggestions(UUID contentId, List<dev.kbd.vekku_server.services.brain.model.TagScore> scores,
            String userId);

    void saveKeywordSuggestions(UUID contentId, List<dev.kbd.vekku_server.services.brain.model.TagScore> keywords,
            String userId);

    // Internal usage for Orchestrators
    dev.kbd.vekku_server.services.content.model.Content getContentInternal(UUID id);
}
