package dev.kbd.vekku_server.cli;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Set;

@ShellComponent
public class BrainCommands {

    private final BrainService brainService;

    public BrainCommands(BrainService brainService) {
        this.brainService = brainService;
    }

    @ShellMethod(key = "brain learn", value = "Teach the AI a concept (Tag)")
    public void brainLearn(@ShellOption String tag) {
        brainService.learnTag(tag);
    }

    @ShellMethod(key = "brain suggest", value = "Get tag suggestions for text")
    public void suggest(@ShellOption String text) {
        // Now returns a Set (Unique collection of tags)
        Set<String> suggestions = brainService.suggestTags(text);

        System.out.println("🧠 Based on: \"" + text + "\"");
        if (suggestions.isEmpty()) {
            System.out.println("   (No tags confident enough to suggest)");
        } else {
            System.out.println("   I suggest: " + suggestions);
        }
    }
}