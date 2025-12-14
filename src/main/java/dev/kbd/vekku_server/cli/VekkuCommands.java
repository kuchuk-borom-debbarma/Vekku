package dev.kbd.vekku_server.cli;

import java.util.List;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import dev.kbd.vekku_server.services.independent.tagService.TagService;
import dev.kbd.vekku_server.services.independent.tagService.neo4jTagService.models.Tag;
import lombok.RequiredArgsConstructor;

@ShellComponent
@RequiredArgsConstructor
public class VekkuCommands {
    final TagService tagService;

    // Command: tag create <name> --parent <parentName>
    @ShellMethod(key = "tag create", value = "Create a tag with optional parent")
    public void createTag(@ShellOption(help = "The name of the tag") String tagName,
            @ShellOption(defaultValue = "", help = "The parent tag name") String parentTagName) {
        Tag t = tagService.createTag(tagName, parentTagName);
        System.out.println("Created tag: " + t.getName());

        if (!t.getParents().isEmpty()) {
            t.getParents().forEach(p -> System.out.println("   └── Linked to Parent: " + p.getName()));
        }
    }

    // Command: tag ancestors <name>
    @ShellMethod(key = "tag ancestors", value = "Show the hierarchy for a tag")
    public void showAncestors(@ShellOption(help = "The tag to analyze") String name) {
        System.out.println("🔍 Hierarchy for '" + name + "':");

        List<Tag> ancestors = tagService.getAncestors(name);

        if (ancestors.isEmpty()) {
            System.out.println("   (No parents found)");
        } else {
            // Print them in order
            ancestors.forEach(p -> System.out.println("   ⬆️ " + p.getName()));
        }
    }
}
