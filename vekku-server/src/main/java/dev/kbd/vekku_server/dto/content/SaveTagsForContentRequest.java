package dev.kbd.vekku_server.dto.content;

import java.util.Set;

public record SaveTagsForContentRequest(String contentId, Set<String> toRemoveTags, Set<String> toAddTags) {

}
