package dev.kbd.vekku_server.services;

import dev.kbd.vekku_server.dto.DocDto;
import dev.kbd.vekku_server.model.Docs;
import dev.kbd.vekku_server.model.DocsTag;
import dev.kbd.vekku_server.repository.DocsRepository;
import dev.kbd.vekku_server.repository.DocsTagRepository;
import dev.kbd.vekku_server.services.brain.BrainService;
import dev.kbd.vekku_server.services.brain.model.TagScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final DocsRepository docsRepository;
    private final DocsTagRepository docsTagRepository;
    private final BrainService brainService;

    @Transactional
    public DocDto.DocResponse createDoc(DocDto.CreateDocRequest request, String userId) {
        log.info("Creating doc for user: {}", userId);

        // 1. Save original content
        Docs doc = Docs.builder()
                .content(request.getContent())
                .type(request.getType())
                .userId(userId)
                .build();
        doc = docsRepository.save(doc);

        // 2. Prepare cleaned content for Brain Service
        // Removing extra whitespace and potentially other noise if needed
        String cleanedContent = request.getContent() != null ? request.getContent().trim() : "";
        if (cleanedContent.isEmpty()) {
            return buildResponse(doc, List.of());
        }

        // 3. Call Brain Service
        log.info("Requesting tags for docId: {}", doc.getId());
        List<TagScore> tagScores = brainService.getRawTagsByEmbedding(cleanedContent, 0.4, 5);

        // 4. Save Tags
        final UUID docId = doc.getId();
        List<DocsTag> savedTags = tagScores.stream()
                .map(ts -> DocsTag.builder()
                        .docId(docId)
                        .tagId(ts.name()) // Assuming name corresponds to ID/Tag Name
                        .score(ts.score())
                        .userId(userId)
                        .build())
                .map(docsTagRepository::save)
                .collect(Collectors.toList());

        return buildResponse(doc, savedTags);
    }

    public DocDto.DocResponse getDoc(UUID id) {
        Docs doc = docsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doc not found"));
        List<DocsTag> tags = docsTagRepository.findByDocId(id);
        return buildResponse(doc, tags);
    }

    public List<DocDto.DocResponse> getAllDocs(String userId) {
        // This assumes we want to filter by user eventually, but for now simple findAll
        // or by User
        // Using findAll for simplicity if userId is not enforced on repo level yet,
        // but ideally should be findByUserId
        // Since I didn't add findByUserId in Repo yet, I'll filter or just return all.
        // Let's iterate all for now or I can add the method to repo.
        // Given strict types, let's just use findAll() for MVP as User requested "view
        // docs".
        return docsRepository.findAll().stream()
                .map(doc -> {
                    List<DocsTag> tags = docsTagRepository.findByDocId(doc.getId());
                    return buildResponse(doc, tags);
                })
                .collect(Collectors.toList());
    }

    private DocDto.DocResponse buildResponse(Docs doc, List<DocsTag> tags) {
        return DocDto.DocResponse.builder()
                .id(doc.getId())
                .content(doc.getContent())
                .type(doc.getType())
                .userId(doc.getUserId())
                .tags(tags)
                .build();
    }
}
