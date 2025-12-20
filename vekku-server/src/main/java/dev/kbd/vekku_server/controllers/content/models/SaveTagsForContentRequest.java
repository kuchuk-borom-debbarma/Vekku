package dev.kbd.vekku_server.controllers.content.models;

import java.util.Set;

public record SaveTagsForContentRequest(String contentId, Set<String> toRemoveTags, Set<String> toAddTags) {

}
