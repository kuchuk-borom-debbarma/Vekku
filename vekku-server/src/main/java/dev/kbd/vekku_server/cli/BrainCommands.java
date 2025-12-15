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

    @ShellMethod(key = "brain learn", value = "Teach the AI a concept (Tag)")
    public void brainLearn(@ShellOption String tag) {
        brainService.learnTag(tag);
    }

    @ShellMethod(key = "brain suggest", value = "Get tag suggestions for text (Smart Refinement)")
    public void suggest(@ShellOption String text) {
        // Now returns a List of ContentRegionTags (Semantic Chunks)
        var regions = orchestrator.suggestTags(text);

        System.out.println("🧠 Based on: \"" + text + "\"");
        if (regions.isEmpty()) {
            System.out.println("   (No tags confident enough to suggest)");
        } else {
            for (var region : regions) {
                System.out
                        .println("   👉 Region [" + region.regionStartIndex() + "-" + region.regionEndIndex() + "]: \""
                                + region.regionContent() + "\"");
                if (region.tagScores().isEmpty()) {
                    System.out.println("      (No tags)");
                } else {
                    region.tagScores().forEach(tagScore -> System.out.println("      🏷️ " + tagScore.name()
                            + " (score: " + String.format("%.2f", tagScore.score()) + ")"));
                }
            }
        }
    }
}