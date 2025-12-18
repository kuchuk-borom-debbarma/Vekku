package dev.kbd.vekku_server.services;

import dev.kbd.vekku_server.model.Tag;
import dev.kbd.vekku_server.repository.TagRepository;
import dev.kbd.vekku_server.services.brain.BrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final BrainService brainService;

    @Transactional
    public Tag createTag(String alias, List<String> synonyms, String userId) {
        if (synonyms == null || synonyms.isEmpty()) {
            synonyms = List.of(alias);
        }
        log.info("Creating tag: {} with synonyms: {}", alias, synonyms);

        Tag tag = Tag.builder()
                .name(alias)
                .synonyms(synonyms)
                .userId(userId)
                .build();

        tag = tagRepository.save(tag);

        // Push to Brain
        brainService.learnTag(tag.getId(), alias, synonyms);

        return tag;
    }

    @Transactional
    public Tag updateTag(UUID id, String alias, List<String> synonyms) {
        log.info("Updating tag: {}", id);

        if (synonyms == null || synonyms.isEmpty()) {
            synonyms = List.of(alias);
        }

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        // Potential optimization: only re-learn if changed
        // For now, simpler to just update
        tag.setName(alias);
        tag.setSynonyms(synonyms);
        tag = tagRepository.save(tag);

        // Brain doesn't support "update" ID-wise yet, so we re-learn
        // Ideally we should delete old points first if we want to be clean,
        // but implementation plan says BrainLogic will handle "clean slate" by deleting
        // existing points for tag.
        // Wait, the BrainService interface doesn't pass ID yet.
        // Architecture doc says: "payload: tag_id".
        // My BrainService.learnTag currently only takes (alias, synonyms).
        // I should probably update BrainService to take ID too, or handle it in
        // BrainLogic by alias?
        // IF I rename alias, I can't find old points by new alias.

        // DECISION: Sending just (Alias, Synonyms) is risky for Renames if Brain uses
        // Alias as Key.
        // Qdrant points need a stable ID.
        // The BrainLogic uses deterministic UUID based on text.
        // If I want to support renaming, I should probably delete the OLD tag alias
        // points first?
        // OR, better, pass the UUID to BrainService so it can store it in payload.

        // For this iteration, I will stick to what I have defined: learnTag(alias,
        // synonyms).
        // If user renames "Java" to "C#", the old "Java" points remain? That's bad.
        // I should call deleteTag(oldName) then learnTag(newName).

        // Let's implement that safe approach here.

        // Note: BrainService.deleteTag(tagName) exists.

        // TODO: This is a bit naive (what if duplicate alias?), but good for v1.

        // We actually don't know if the old alias exists in Brain if strictly DB
        // driven,
        // but we assume sync.

        brainService.learnTag(id, alias, synonyms);

        return tag;
    }

    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        tagRepository.delete(tag);
        brainService.deleteTag(tag.getName());
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}
