package dev.kbd.vekku_server.content;

import dev.kbd.vekku_server.content.api.ContentDTOs.ContentDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
class ContentServiceImpl implements IContentService {

    final ContentRepo contentRepo;
    final ContentMapper contentMapper;

    @Override
    public ContentDTO createContent(
        String userId,
        CreateContentRequest request
    ) {
        Content toSaveContent = Content.builder()
            .userId(userId)
            .content(request.content())
            .title(request.title())
            .contentType(request.contentType())
            .build();
        contentRepo.save(toSaveContent);
        return contentMapper.toDto(toSaveContent);
    }

    @Override
    public ContentDTO updateContent(
        String userId,
        UpdateContentRequest request
    ) {
        var existing = contentRepo
            .findById(UUID.fromString(request.id()))
            .orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException(
                String.format(
                    "Content with id {} not found for user {}",
                    request.id(),
                    userId
                )
            );
        }

        if (StringUtils.hasText(request.updatedTitle())) {
            existing.setTitle(request.updatedTitle());
        }
        if (StringUtils.hasText(request.updatedContent())) {
            existing.setContent(request.updatedContent());
        }
        if (request.updatedContentType() != null) {
            existing.setContentType(request.updatedContentType());
        }

        contentRepo.save(existing);

        return contentMapper.toDto(existing);
    }

    @Override
    public void deleteContent(String id, String userId) {
        //validate userId
        Content existing = contentRepo
            .findById(UUID.fromString(id))
            .orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException(
                String.format(
                    "Content with id {} not found for user {}",
                    id,
                    userId
                )
            );
        }

        //delete
        contentRepo.delete(existing);
    }
}
