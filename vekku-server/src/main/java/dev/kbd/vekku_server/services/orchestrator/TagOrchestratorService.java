package dev.kbd.vekku_server.services.orchestrator;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import dev.kbd.vekku_server.services.independent.brainService.model.ContentRegionTags;
import dev.kbd.vekku_server.services.independent.taxonomyService.TaxonomyService;
import dev.kbd.vekku_server.services.independent.taxonomyService.models.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagOrchestratorService {
    private final TaxonomyService taxonomyService;
    private final BrainService brainService;

    /**
     * Orchestrates the creation of a tag across the Knowledge Graph (Neo4j)
     * and the Semantic Brain (Qdrant).
     * 
     * <p>
     * <b>Transactionality:</b>
     * This method is @Transactional. If the Brain Service call fails (HTTP Error),
     * this transaction will roll back, and the Neo4j write will be undone.
     * This ensures we don't end up with "Ghost Tags" in the graph that the brain
     * doesn't know about.
     */
    @Transactional
    public Tag createTag(String name, String parent) {
        log.info("Orchestrating Tag Creation: '{}' (Parent: '{}')", name, parent);

        // 1. Source of Truth: Create structure in Neo4j
        Tag tag = taxonomyService.createTag(name, parent);

        // 2. Semantic Index: Teach the Brain
        // Note: If this fails, the RuntimeException triggers a rollback of Step 1.
        brainService.learnTag(name);

        return tag;
    }

    public List<ContentRegionTags> suggestTags(String content) {
        log.info("Orchestrating Tag suggestion for content length: {}", content.length());

        log.debug("Getting raw result from brain service");
        List<ContentRegionTags> contentRegionTags = brainService.suggestTags(content);
        List<ContentRegionTags> refinedRegions = new java.util.ArrayList<>();

        log.debug("Refining tags with Recursive Deepening");

        for (var region : contentRegionTags) {
            java.util.Map<String, Double> accumulatedScores = new java.util.HashMap<>();
            java.util.Set<String> visited = new java.util.HashSet<>();

            for (var tag : region.tagScores()) {
                // Start exploration from each initial suggestion
                exploreAndScore(tag.name(), tag.score(), 0, accumulatedScores, visited, region.regionContent());
            }

            List<dev.kbd.vekku_server.services.independent.brainService.model.TagScore> finalTags = pruneAndRank(
                    accumulatedScores);

            refinedRegions.add(new ContentRegionTags(
                    region.regionContent(),
                    region.regionStartIndex(),
                    region.regionEndIndex(),
                    finalTags));
        }

        return refinedRegions;
    }

    /**
     * Recursively explores the tag hierarchy downwards (Drill Down).
     * <p>
     * <b>Strategy:</b>
     * 1. Accumulate score for current tag.
     * 2. Fetch children.
     * 3. Batch score children against content.
     * 4. Recurse depth-first.
     */
    private void exploreAndScore(String tagName, double currentScore, int depth,
            java.util.Map<String, Double> accumulatedScores,
            java.util.Set<String> visited,
            String content) {
        // Stop if max depth reached or cycle detected
        if (depth > 2 || visited.contains(tagName))
            return;
        visited.add(tagName);

        // Boost score if visited multiple times (Theme Accumulation)
        accumulatedScores.merge(tagName, currentScore, Double::sum);

        // Fetch children from Graph
        List<Tag> children = taxonomyService.getChildren(tagName);
        if (children.isEmpty())
            return;

        // Batch Score children with Brain
        List<String> childNames = children.stream().map(Tag::getName).toList();
        List<dev.kbd.vekku_server.services.independent.brainService.model.TagScore> childScores = brainService
                .scoreTags(childNames, content);

        for (var child : childScores) {
            // Optimization: Only explore if child has some relevance (> 0.4)
            if (child.score() > 0.4) {
                exploreAndScore(child.name(), child.score(), depth + 1, accumulatedScores, visited, content);
            }
        }
    }

    /**
     * Filters out generic parents if specific children are present.
     * e.g., If [Java, Programming] exists, keep only [Java].
     */
    private List<dev.kbd.vekku_server.services.independent.brainService.model.TagScore> pruneAndRank(
            java.util.Map<String, Double> accumulatedScores) {
        // 1. Sort by Score (Desc)
        List<String> sortedTags = accumulatedScores.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Double>comparingByValue().reversed())
                .map(java.util.Map.Entry::getKey)
                .toList();

        java.util.Set<String> toRemove = new java.util.HashSet<>();

        // 2. Prune Ancestors
        // O(N^2) but N is small (usually < 20 tags)
        for (String candidate : sortedTags) {
            if (toRemove.contains(candidate))
                continue;

            // Get ancestors of this candidate
            List<Tag> ancestors = taxonomyService.getAncestors(candidate);
            for (Tag ancestor : ancestors) {
                // If an ancestor is also in our list, mark it for removal
                if (accumulatedScores.containsKey(ancestor.getName())) {
                    toRemove.add(ancestor.getName());
                }
            }
        }

        // 3. Construct Final List
        return sortedTags.stream()
                .filter(t -> !toRemove.contains(t))
                .limit(5) // Top 5
                .map(name -> new dev.kbd.vekku_server.services.independent.brainService.model.TagScore(name,
                        accumulatedScores.get(name)))
                .toList();
    }
}
