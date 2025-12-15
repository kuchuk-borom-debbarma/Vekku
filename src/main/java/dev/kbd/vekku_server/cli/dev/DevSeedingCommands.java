package dev.kbd.vekku_server.cli.dev;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.kbd.vekku_server.services.orchestrator.TagOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ShellComponent
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevSeedingCommands {

    private final TagOrchestratorService orchestrator;
    private final ObjectMapper objectMapper;

    // DTO for JSON Structure
    record TagNode(String name, List<TagNode> children) {
    }

    @ShellMethod(key = "dev seed-tags", value = "🌱 (DEV) Seed tags from src/test/resources/tag-hierarchy.json")
    public void seedTags() {
        File file = new File("src/test/resources/tag-hierarchy.json");
        if (!file.exists()) {
            System.err.println("❌ File not found: " + file.getAbsolutePath());
            return;
        }

        try {
            System.out.println("🌱 Reading tags from: " + file.getPath());
            List<TagNode> roots = objectMapper.readValue(file, new TypeReference<List<TagNode>>() {
            });

            for (TagNode root : roots) {
                processNode(root, null);
            }
            System.out.println("✅ Seeding complete!");

        } catch (IOException e) {
            log.error("Failed to read JSON", e);
            System.err.println("❌ Error parsing JSON: " + e.getMessage());
        }
    }

    private void processNode(TagNode node, String parentName) {
        try {
            // Create the tag
            orchestrator.createTag(node.name(), parentName);
            System.out.println(
                    "   Created: " + node.name() + (parentName != null ? " (Child of " + parentName + ")" : ""));

            // Recurse for children
            if (node.children() != null) {
                for (TagNode child : node.children()) {
                    processNode(child, node.name());
                }
            }
        } catch (Exception e) {
            System.err.println("   ⚠️ Failed to create " + node.name() + ": " + e.getMessage());
        }
    }
}
