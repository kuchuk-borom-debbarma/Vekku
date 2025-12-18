package dev.kbd.vekku_server.cli;

import dev.kbd.vekku_server.services.brain.BrainService;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import lombok.RequiredArgsConstructor;

@ShellComponent
@RequiredArgsConstructor
public class BrainCommands {

    private final BrainService brainService;

    @ShellMethod(key = "brain learn", value = "Teach the AI a concept (Tag)")
    public void brainLearn(@ShellOption String tag, @ShellOption(defaultValue = "") String synonyms) {
        var synonymList = synonyms.isEmpty() ? java.util.List.of(tag)
                : java.util.List.of(synonyms.split(","));
        brainService.learnTag(java.util.UUID.randomUUID(), tag, synonymList);
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
        var regions = brainService.getRegionTags(text, threshold, topK);

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

}