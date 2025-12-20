package dev.kbd.vekku_server.services.content.dtos;

import java.util.Set;

public record SaveTagsForContentParam(String contentId, Set<String> toRemoveTags, Set<String> toAddTags) {

}
