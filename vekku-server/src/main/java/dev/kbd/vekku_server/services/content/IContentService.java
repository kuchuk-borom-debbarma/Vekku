package dev.kbd.vekku_server.services.content;

import dev.kbd.vekku_server.services.common.dtos.TagScore;
import dev.kbd.vekku_server.services.content.dtos.Content;
import dev.kbd.vekku_server.services.content.dtos.ContentDetail;
import dev.kbd.vekku_server.services.content.dtos.ContentKeywordSuggestion;
import dev.kbd.vekku_server.services.content.dtos.ContentPage;
import dev.kbd.vekku_server.services.content.dtos.CreateContentParam;
import dev.kbd.vekku_server.services.content.dtos.SaveTagsForContentParam;
import dev.kbd.vekku_server.shared.events.ContentProcessingAction;
import java.util.List;
import java.util.UUID;
import java.util.Set;

public interface IContentService {
        Content createContent(CreateContentParam request, String userId);

        ContentPage getAllContent(String userId, Integer limit, String cursor);

        ContentDetail getContent(UUID contentId, String userId);

        void updateTagsOfContent(SaveTagsForContentParam request, String userId);

        void refreshSuggestions(UUID contentId, String userId, Set<ContentProcessingAction> actions);

        // TODO move to tags
        List<ContentKeywordSuggestion> getContentKeywords(UUID contentId, String userId);

        // TODO move to tags
        void saveTagSuggestions(UUID contentId, List<TagScore> scores,
                        String userId);

        // TODO move to tags
        void saveKeywordSuggestions(UUID contentId, List<TagScore> keywords,
                        String userId);

        Content getContent(UUID id);
}
