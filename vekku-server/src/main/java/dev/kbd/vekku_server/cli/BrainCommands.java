package dev.kbd.vekku_server.cli;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;

import dev.kbd.vekku_server.services.orchestrator.TagOrchestratorService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import lombok.RequiredArgsConstructor;

/**
 * <b>AI INTERFACE (CLI):</b>
 * <p>
 * This class lets us talk to the 'Brain' (Vector Database + Embedding Model)
 * from the terminal.
 * It's magical because we can input raw text ("How to cook") and get back
 * related Tags ("Food", "Cooking"),
 * even if we never explicitly used those words.
 */
@ShellComponent
@RequiredArgsConstructor
public class BrainCommands {

    private final BrainService brainService;
    private final TagOrchestratorService orchestrator;
    private final dev.kbd.vekku_server.services.independent.taxonomyService.TaxonomyService taxonomyService;

    @ShellMethod(key = "brain learn", value = "Teach the AI a concept (Tag)")
    public void brainLearn(@ShellOption String tag) {
        brainService.learnTag(tag);
    }

    @ShellMethod(key = "brain suggest", value = "Get tag suggestions for text (Smart Refinement)")
    public void suggest(@ShellOption String text,
            @ShellOption(defaultValue = "0.3") double threshold,
            @ShellOption(defaultValue = "50") int topK) {
        // Now returns a SuggestTagsResponse containing Regions and Overall Tags
        var response = orchestrator.suggestTags(text, threshold, topK);
        var regions = response.regions();
        var overallTags = response.overallTags();

        System.out.println("🧠 Based on: \"" + text + "\"");

        // Print Overall Tags
        if (!overallTags.isEmpty()) {
            System.out.println("   🌍 Overall Topics:");
            overallTags.forEach(t -> System.out
                    .println("      ⭐ " + t.name() + " (" + String.format("%.2f", t.score()) + ")"));
            System.out.println("   --------------------------------------------------");
        }

        if (regions.isEmpty()) {
            System.out.println("   (No specific regions found)");
        } else {
            for (var region : regions) {
                System.out
                        .println("   👉 Region [" + region.regionStartIndex() + "-" + region.regionEndIndex() + "]: \""
                                + region.regionContent() + "\"");

                // Print Flat Tags (if any exist independently, though Orchestrator populates
                // both)
                if (!region.tagScores().isEmpty()) {
                    System.out.println("      🔹 Flat Suggestions:");
                    region.tagScores().forEach(tagScore -> System.out
                            .println("         🏷️ " + tagScore.name() + " ("
                                    + String.format("%.2f", tagScore.score()) + ")"));
                }

                // Print Taxonomy Paths
                if (!region.taxonomyPaths().isEmpty()) {
                    System.out.println("      🌳 Hierarchy Paths:");
                    for (var path : region.taxonomyPaths()) {
                        String pathStr = path.path().stream()
                                .map(node -> node.name() + "(" + String.format("%.2f", node.score()) + ")")
                                .collect(java.util.stream.Collectors.joining(" -> "));
                        System.out.println("         🚀 " + pathStr);
                    }
                } else if (region.tagScores().isEmpty()) {
                    System.out.println("      (No tags)");
                }
            }
        }
    }

    @ShellMethod(key = "brain graph", value = "Debug: Show graph neighbors")
    public void debugGraph(@ShellOption String name) {
        System.out.println("Debugging Graph for: " + name);
        // Using injected taxonomyService directly
        var parents = taxonomyService.getParents(name);
        if (parents.isEmpty()) {
            System.out.println(name + " has NO parents (Root?)");
        } else {
            System.out.println(name + " is Child Of:");
            parents.forEach(p -> System.out.println("  -> " + p.getName()));
        }
    }

    @ShellMethod(key = "brain debug-path", value = "Debug: Show raw path names")
    public void debugPath(@ShellOption String name) {
        System.out.println("Debugging Paths for: " + name);
        var paths = taxonomyService.getPathNames(name);
        System.out.println("Paths found: " + paths.size());
        for (var p : paths) {
            System.out.println(" Path: " + p);
        }
    }

    @ShellMethod(key = "brain check-node", value = "Debug: Count nodes")
    public void checkNode(@ShellOption String name) {
        long count = taxonomyService.countNodes(name);
        System.out.println("Node count for '" + name + "': " + count);
        if (count > 1) {
            System.out.println("⚠️ WARNING: Duplicate Nodes detected!");
        }
    }
}