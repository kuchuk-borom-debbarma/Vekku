package dev.kbd.vekku_server.cli;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import lombok.RequiredArgsConstructor;

@ShellComponent
@RequiredArgsConstructor
public class BrainCommands {

    private final BrainService brainService;
    private final dev.kbd.vekku_server.services.independent.taxonomyService.TaxonomyService taxonomyService;

    @ShellMethod(key = "brain learn", value = "Teach the AI a concept (Tag)")
    public void brainLearn(@ShellOption String tag) {
        brainService.learnTag(tag);
    }

    @ShellMethod(key = "brain suggest", value = "Get tag suggestions (Raw & Regions)")
    public void suggest(@ShellOption String text,
            @ShellOption(defaultValue = "0.3") double threshold,
            @ShellOption(defaultValue = "50") int topK) {

        System.out.println("🧠 Analyzing: \"" + text.substring(0, Math.min(text.length(), 50)) + "...\"");

        // 1. Get Raw Tags
        var rawTags = brainService.getRawTagsByEmbedding(text, threshold, topK);
        System.out.println("   🌍 Overall Raw Tags:");
        if (rawTags.isEmpty()) {
            System.out.println("      (No tags found)");
        } else {
            rawTags.forEach(t -> System.out
                    .println("      ⭐ " + t.name() + " (" + String.format("%.2f", t.score()) + ")"));
        }
        System.out.println("   --------------------------------------------------");

        // 2. Get Region Tags
        var regions = brainService.getRegionTags(text, threshold);

        if (regions.isEmpty()) {
            System.out.println("   (No specific regions found)");
        } else {
            for (var region : regions) {
                System.out
                        .println("   👉 Region [" + region.regionStartIndex() + "-" + region.regionEndIndex() + "]: \""
                                + region.regionContent().replace("\n", " ").substring(0,
                                        Math.min(region.regionContent().length(), 40))
                                + "...\"");

                if (!region.tagScores().isEmpty()) {
                    region.tagScores().forEach(tagScore -> System.out
                            .println("         🏷️ " + tagScore.name() + " ("
                                    + String.format("%.2f", tagScore.score()) + ")"));
                } else {
                    System.out.println("         (No tags)");
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