package dev.kbd.vekku_server.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kbd.vekku_server.services.brain.BrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@ShellComponent
@Profile("dev")
@RequiredArgsConstructor
public class DevBrainCommands {

    private final BrainService brainService;
    private final ObjectMapper objectMapper;

    @ShellMethod(key = "brain learn-all", value = "Dev: Learn all tags from src/test/resources/tag-hierarchy.json")
    public void learnAllTags() {
        System.out.println("🚀 Starting bulk tag learning from test resources...");

        File file = new File("src/test/resources/tag-hierarchy.json");
        if (!file.exists()) {
            System.err.println("❌ Error: File not found at " + file.getAbsolutePath());
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(file);
            Set<String> uniqueTags = new HashSet<>();
            collectTags(root, uniqueTags);

            System.out.println("📦 Found " + uniqueTags.size() + " unique tags. Starting learning process...");

            int count = 0;
            for (String tag : uniqueTags) {
                brainService.learnTag(tag);
                count++;
                if (count % 10 == 0) {
                    System.out.println("   ...learned " + count + " tags");
                }
            }

            System.out.println("✅ Successfully learned " + count + " tags!");

        } catch (IOException e) {
            System.err.println("❌ Error reading JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void collectTags(JsonNode node, Set<String> tags) {
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectTags(child, tags);
            }
        } else if (node.isObject()) {
            if (node.has("name")) {
                tags.add(node.get("name").asText());
            }
            if (node.has("children")) {
                collectTags(node.get("children"), tags);
            }
        }
    }
}
