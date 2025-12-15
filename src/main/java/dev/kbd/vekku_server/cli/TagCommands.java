package dev.kbd.vekku_server.cli;

import java.util.List;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

<<<<<<< HEAD
import dev.kbd.vekku_server.services.independent.tagService.TagService;
import dev.kbd.vekku_server.services.independent.tagService.neo4jTagService.models.Tag;
=======
import dev.kbd.vekku_server.services.independent.taxonomyService.TaxonomyService;
import dev.kbd.vekku_server.services.independent.taxonomyService.models.Tag;
import dev.kbd.vekku_server.services.orchestrator.TagOrchestratorService;
>>>>>>> gg-sync/server-spring/1765835959
import lombok.RequiredArgsConstructor;

/**
 * <b>CLI INTERFACE:</b>
 * <p>
 * This class exposes our backend logic to the command line using Spring Shell.
 * It's perfect for testing without a frontend.
 * <ul>
 * <li><b>@ShellComponent:</b> Registers this class as a command provider.</li>
 * <li><b>@RequiredArgsConstructor:</b> Lombok magic to generate the constructor
 * for dependency injection.</li>
 * </ul>
 */
@ShellComponent
@RequiredArgsConstructor
public class TagCommands {
<<<<<<< HEAD
    final TagService tagService;
=======
    final TaxonomyService taxonomyService;
    final TagOrchestratorService tagOrchestratorService;
>>>>>>> gg-sync/server-spring/1765835959

    // Command: tag create <name> --parent <parentName>
    @ShellMethod(key = "tag create", value = "Create a tag with optional parent")
    public void createTag(@ShellOption(help = "The name of the tag") String tagName,
            @ShellOption(defaultValue = "", help = "The parent tag name") String parentTagName) {
<<<<<<< HEAD
        Tag t = tagService.createTag(tagName, parentTagName);
=======
        Tag t = tagOrchestratorService.createTag(tagName, parentTagName);
>>>>>>> gg-sync/server-spring/1765835959
        System.out.println("Created tag: " + t.getName());

        if (!t.getParents().isEmpty()) {
            t.getParents().forEach(p -> System.out.println("   └── Linked to Parent: " + p.getName()));
        }
    }

    // Command: tag ancestors <name>
    @ShellMethod(key = "tag ancestors", value = "Show the hierarchy for a tag")
    public void showAncestors(@ShellOption(help = "The tag to analyze") String name) {
        System.out.println("🔍 Hierarchy for '" + name + "':");

<<<<<<< HEAD
        List<Tag> ancestors = tagService.getAncestors(name);
=======
        List<Tag> ancestors = taxonomyService.getAncestors(name);
>>>>>>> gg-sync/server-spring/1765835959

        if (ancestors.isEmpty()) {
            System.out.println("   (No parents found)");
        } else {
            // Print them in order
            ancestors.forEach(p -> System.out.println("   ⬆️ " + p.getName()));
        }
    }
}
