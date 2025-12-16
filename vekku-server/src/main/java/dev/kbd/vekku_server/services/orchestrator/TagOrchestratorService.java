package dev.kbd.vekku_server.services.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import dev.kbd.vekku_server.services.independent.brainService.model.ContentRegionTags;
import dev.kbd.vekku_server.services.independent.brainService.model.TagPath;
import dev.kbd.vekku_server.services.independent.brainService.model.TagScore;
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
        List<ContentRegionTags> refinedRegions = new ArrayList<>();

        log.debug("Refining tags with Recursive Deepening");

        for (var region : contentRegionTags) {
            Map<String, Double> accumulatedScores = exploreHierarchy(region, region.regionContent());
            List<TagScore> finalTags = pruneAndRank(accumulatedScores);
            List<TagPath> tagPaths = resolveTagPaths(finalTags, region.regionContent(), accumulatedScores);

            refinedRegions.add(new ContentRegionTags(
                    region.regionContent(),
                    region.regionStartIndex(),
                    region.regionEndIndex(),
                    finalTags,
                    tagPaths));
        }

        return refinedRegions;
    }

    private Map<String, Double> exploreHierarchy(ContentRegionTags region, String content) {
        Map<String, Double> accumulatedScores = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (var tag : region.tagScores()) {
            exploreAndScore(tag.name(), tag.score(), 0, accumulatedScores, visited, content);
        }
        return accumulatedScores;
    }

    private List<TagPath> resolveTagPaths(List<TagScore> finalTags, String content,
            Map<String, Double> accumulatedScores) {
        List<TagPath> tagPaths = new ArrayList<>();
        for (TagScore finalTag : finalTags) {
            List<List<Tag>> paths = getPathsForTag(finalTag.name());
            List<Tag> bestPath = selectBestPath(paths, accumulatedScores, content);

            if (bestPath != null) {
                // Neo4j returns [Leaf, ..., Root]. Reverse to get [Root, ..., Leaf]
                List<Tag> rootToLeaf = new ArrayList<>(bestPath);
                Collections.reverse(rootToLeaf);

                List<TagScore> pathWithScores = rootToLeaf.stream()
                        .map(node -> new TagScore(node.getName(),
                                accumulatedScores.getOrDefault(node.getName(), 0.0)))
                        .toList();
                tagPaths.add(new TagPath(pathWithScores, finalTag.score()));
            }
        }
        return tagPaths;
    }

    private List<List<Tag>> getPathsForTag(String tagName) {
        List<String> rawPaths = taxonomyService.getSerializedPaths(tagName);
        List<List<Tag>> paths = new ArrayList<>();
        for (String raw : rawPaths) {
            if (raw == null || raw.isEmpty())
                continue;
            String[] names = raw.split("\\$\\$\\$");
            List<Tag> pathNodes = new ArrayList<>();
            for (String n : names) {
                pathNodes.add(new Tag(n)); // Create detached Tag
            }
            paths.add(pathNodes);
        }
        return paths;
    }

    private List<Tag> selectBestPath(List<List<Tag>> paths, Map<String, Double> accumulatedScores, String content) {
        if (paths.isEmpty())
            return null;
        if (paths.size() == 1)
            return paths.get(0);

        // Disambiguate: "Highest Score at Root Wins"
        double bestRootScore = -1.0;
        List<Tag> bestPath = null;

        // 1. Identify Roots and Score them if needed
        Set<String> rootsToScore = new HashSet<>();
        for (List<Tag> path : paths) {
            if (path.isEmpty())
                continue;
            // Path is [Leaf, ..., Root] (Neo4j returns Leaf->Root)
            String rootName = path.get(path.size() - 1).getName();
            if (!accumulatedScores.containsKey(rootName)) {
                rootsToScore.add(rootName);
            }
        }

        if (!rootsToScore.isEmpty()) {
            List<TagScore> rootScores = brainService.scoreTags(new ArrayList<>(rootsToScore), content);
            for (TagScore rs : rootScores) {
                accumulatedScores.put(rs.name(), rs.score());
            }
        }

        // 2. Select Best Path
        for (List<Tag> path : paths) {
            if (path.isEmpty())
                continue;
            String rootName = path.get(path.size() - 1).getName();
            double rootScore = accumulatedScores.getOrDefault(rootName, 0.0);
            if (rootScore > bestRootScore) {
                bestRootScore = rootScore;
                bestPath = path;
            }
        }
        return bestPath;
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
            Map<String, Double> accumulatedScores,
            Set<String> visited,
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
        List<TagScore> childScores = brainService.scoreTags(childNames, content); // Auto-resolved import

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
    private List<TagScore> pruneAndRank(
            Map<String, Double> accumulatedScores) {
        // 1. Sort by Score (Desc)
        List<String> sortedTags = accumulatedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        Set<String> toRemove = new HashSet<>();

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
                .map(name -> new TagScore(name, accumulatedScores.get(name)))
                .toList();
    }
}
