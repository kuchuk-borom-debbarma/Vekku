package dev.kbd.vekku_server.services.content.dto;

import java.util.Set;

public record SaveTagsForContentRequest(String contentId, Set<String> toRemoveTags, Set<String> toAddTags) {

}
