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

import dev.kbd.vekku_server.services.independent.brainService.model.SuggestTagsResponse;

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

                SuggestTagsResponse mockResponse = new SuggestTagsResponse(List.of(region), List.of());
                when(brainService.suggestTags(eq(content), anyDouble(), anyInt())).thenReturn(mockResponse);

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
                when(brainService.scoreTags(List.of("Coding"), content))
                                .thenReturn(List.of(new TagScore("Coding", 0.85)));
                when(brainService.scoreTags(List.of("Java"), content)).thenReturn(List.of(new TagScore("Java", 0.95)));

                // Note: In logic we fetch paths using getSerializedPaths.
                // Format: Leaf$$$Parent$$$Root
                when(taxonomyService.getSerializedPaths("Java")).thenReturn(List.of("Java$$$Coding$$$SDE"));

                SuggestTagsResponse resultResponse = orchestrator.suggestTags(content, 0.3, 50);
                List<ContentRegionTags> result = resultResponse.regions();

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
        }

        @Test
        void testPolyHierarchy_React() {
                String content = "React JS";
                ContentRegionTags region = new ContentRegionTags(content, 0, 8, List.of(new TagScore("React", 0.9)),
                                List.of());
                SuggestTagsResponse mockResponse = new SuggestTagsResponse(List.of(region), List.of());
                when(brainService.suggestTags(eq(content), anyDouble(), anyInt())).thenReturn(mockResponse);

                // Mock Taxonomy
                // Library -> React
                // Meta -> React
                Tag react = new Tag("React");
                Tag library = new Tag("Library");
                Tag meta = new Tag("Meta");

                // getChildren (Recursion stops immediately as React has no children in this
                // mock or valid child score)
                when(taxonomyService.getChildren("React")).thenReturn(List.of());

                // getPaths for React (Leaf -> Root)
                when(taxonomyService.getSerializedPaths("React")).thenReturn(List.of(
                                "React$$$Library",
                                "React$$$Meta"));

                // Mock Brain Scoring for Roots (Library vs Meta)
                // Library score > Meta score -> System should choose Library path
                when(brainService.scoreTags(argThat(list -> list.contains("Library") && list.contains("Meta")),
                                eq(content)))
                                .thenReturn(List.of(
                                                new TagScore("Library", 0.8),
                                                new TagScore("Meta", 0.2)));

                SuggestTagsResponse resultResponse = orchestrator.suggestTags(content, 0.3, 50);
                List<ContentRegionTags> result = resultResponse.regions();

                assertEquals(1, result.size());
                List<dev.kbd.vekku_server.services.independent.brainService.model.TagPath> paths = result.get(0)
                                .taxonomyPaths();
                assertEquals(1, paths.size(), "Should return exactly 1 best path for React");

                // Verify it picked Library path (Library score 0.8 > Meta score 0.2)
                List<TagScore> bestPath = paths.get(0).path();

                System.out.println("Generated Path:");
                String pathStr = bestPath.stream()
                                .map(node -> node.name() + "(" + String.format("%.2f", node.score()) + ")")
                                .collect(java.util.stream.Collectors.joining(" -> "));
                System.out.println(pathStr);

                assertEquals("Library", bestPath.get(0).name());
                assertEquals("React", bestPath.get(1).name());
        }
}
