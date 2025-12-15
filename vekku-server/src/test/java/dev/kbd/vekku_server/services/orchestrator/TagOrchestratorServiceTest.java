package dev.kbd.vekku_server.services.orchestrator;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import dev.kbd.vekku_server.services.independent.brainService.model.ContentRegionTags;
import dev.kbd.vekku_server.services.independent.brainService.model.TagScore;
import dev.kbd.vekku_server.services.independent.taxonomyService.TaxonomyService;
import dev.kbd.vekku_server.services.independent.taxonomyService.models.Tag;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class TagOrchestratorServiceTest {

    @Mock
    private TaxonomyService taxonomyService;

    @Mock
    private BrainService brainService;

    @InjectMocks
    private TagOrchestratorService orchestrator;

    /**
     * Scenario: "SDE -> Java"
     * 1. Brain says: "SDE", "Coding".
     * 2. Orchestrator finds: SDE->Coding->Java.
     * 3. Brain scores: Java > Coding > SDE.
     * 4. Result: Only "Java".
     */
    @Test
    void testPruningLogic_SDE_to_Java() {
        String content = "Java code";

        // 1. Initial Suggestion
        TagScore sde = new TagScore("SDE", 0.8);
        TagScore coding = new TagScore("Coding", 0.7);
        ContentRegionTags region = new ContentRegionTags(content, 0, 10, List.of(sde, coding), List.of());

        when(brainService.suggestTags(content)).thenReturn(List.of(region));

        // 2. Mock Taxonomy (SDE -> Coding -> Java)
        Tag tagSDE = new Tag("SDE");
        Tag tagCoding = new Tag("Coding");
        Tag tagJava = new Tag("Java");

        // getChildren
        when(taxonomyService.getChildren("SDE")).thenReturn(List.of(tagCoding));
        when(taxonomyService.getChildren("Coding")).thenReturn(List.of(tagJava));
        when(taxonomyService.getChildren("Java")).thenReturn(List.of());

        // getAncestors (For Pruning Step)
        when(taxonomyService.getAncestors("Java")).thenReturn(List.of(tagCoding, tagSDE));

        // 3. Mock Brain Scoring (Batch Calls)
        // Note: exploreAndScore calls brainService.scoreTags(childNames, content)
        when(brainService.scoreTags(List.of("Coding"), content)).thenReturn(List.of(new TagScore("Coding", 0.85)));
        when(brainService.scoreTags(List.of("Java"), content)).thenReturn(List.of(new TagScore("Java", 0.95)));

        // Note: In logic we fetch paths using getPaths.
        // We need to mock getPaths for Java to return [SDE, Coding, Java]
        when(taxonomyService.getPaths("Java")).thenReturn(List.of(List.of(tagSDE, tagCoding, tagJava)));

        List<ContentRegionTags> result = orchestrator.suggestTags(content);

        // 5. Verify
        assertFalse(result.isEmpty());
        List<TagScore> finalTags = result.get(0).tagScores();

        System.out.println("Final Tags: " + finalTags);

        // Should contain Java
        assertTrue(finalTags.stream().anyMatch(t -> t.name().equals("Java")));
        // Should NOT contain SDE or Coding (Pruned)
        assertFalse(finalTags.stream().anyMatch(t -> t.name().equals("SDE")));
        assertFalse(finalTags.stream().anyMatch(t -> t.name().equals("Coding")));

        assertEquals(1, finalTags.size(), "Only Java should remain");

        // 6. Verify Paths
        List<dev.kbd.vekku_server.services.independent.brainService.model.TagPath> paths = result.get(0)
                .taxonomyPaths();
        assertFalse(paths.isEmpty());
        // We expect one path for Java: SDE -> Coding -> Java
        // Note: In logic we fetch paths using getPaths.
        // We need to mock getPaths for Java to return [SDE, Coding, Java]
    }

    @Test
    void testPolyHierarchy_React() {
        String content = "React JS";
        ContentRegionTags region = new ContentRegionTags(content, 0, 8, List.of(new TagScore("React", 0.9)), List.of());
        when(brainService.suggestTags(content)).thenReturn(List.of(region));

        // Mock Taxonomy
        // Library -> React
        // Meta -> React
        Tag react = new Tag("React");
        Tag library = new Tag("Library");
        Tag meta = new Tag("Meta");

        // getChildren (Recursion stops immediately as React has no children in this
        // mock or valid child score)
        when(taxonomyService.getChildren("React")).thenReturn(List.of());

        // getPaths for React
        when(taxonomyService.getPaths("React")).thenReturn(List.of(
                List.of(library, react),
                List.of(meta, react)));

        List<ContentRegionTags> result = orchestrator.suggestTags(content);

        assertEquals(1, result.size());
        List<dev.kbd.vekku_server.services.independent.brainService.model.TagPath> paths = result.get(0)
                .taxonomyPaths();
        assertEquals(2, paths.size(), "Should return 2 paths for React");
    }
}
