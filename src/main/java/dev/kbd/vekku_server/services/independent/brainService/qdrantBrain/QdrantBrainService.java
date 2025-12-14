package dev.kbd.vekku_server.services.independent.brainService.qdrantBrain;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class QdrantBrainService implements BrainService {
    final VectorStore vectorStore;

    @Override
    public void learnTag(String tagName) {
        log.trace("learnTag({})", tagName);

        // 1. Create Metadata
        // We add "type=TAG" so later we can say "Search only TAGs, not Documents"
        Map<String, Object> metadata = Map.of(
                "type", "TAG",
                "original_name", tagName);

        // 2. Wrap it in a "Document" object
        // This is what Spring AI uses to hold Text + Metadata
        Document doc = new Document(tagName, metadata);

        // 3. Save to Qdrant
        // The Embedding Model (BGE-Small) runs automatically in the background here!
        vectorStore.add(List.of(doc));

        System.out.println("🧠 Brain has learned the concept: " + tagName);
    }
}
