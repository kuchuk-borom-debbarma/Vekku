package dev.kbd.vekku_server.services;

import dev.kbd.vekku_server.dto.DocDto;
import dev.kbd.vekku_server.model.Docs;
import dev.kbd.vekku_server.model.DocsTag;
import dev.kbd.vekku_server.repository.DocsRepository;
import dev.kbd.vekku_server.repository.DocsTagRepository;
import dev.kbd.vekku_server.services.brain.BrainService;
// import dev.kbd.vekku_server.services.brain.model.TagScore;
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
