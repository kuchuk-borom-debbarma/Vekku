package dev.kbd.vekku_server.service;

import dev.kbd.vekku_server.model.Tag;
import dev.kbd.vekku_server.model.content.Content;
import dev.kbd.vekku_server.model.content.ContentTagSuggestion;
import dev.kbd.vekku_server.repository.ContentTagRepository;
import dev.kbd.vekku_server.repository.TagRepository;
import dev.kbd.vekku_server.services.brain.model.TagScore;
import dev.kbd.vekku_server.services.core.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentProcessor {

    private final EmbeddingService embeddingService;
    private final TagRepository tagRepository;
    private final ContentTagRepository contentTagRepository;

    @RabbitListener(queues = "${vekku.rabbitmq.queue}")
    @Transactional
    public void processContent(Content content) {
        log.info("Processing content: {}", content.getId());

        try {
            // 1. Brain needs to return the tags
            List<TagScore> tagScores = embeddingService.getRawTagsByEmbedding(content.getText(), 0.45, 10);

            for (TagScore tagScore : tagScores) {
                String tagName = tagScore.name();
                Double score = tagScore.score();
                String userId = content.getUserId();

                // Get the id of the tag
                Tag tag = tagRepository.findByNameAndUserId(tagName, userId)
                        .orElseThrow(() -> new RuntimeException("Tag not found for name: " + tagName));

                // 3. Save ContentTagSuggestion
                ContentTagSuggestion contentTag = ContentTagSuggestion.builder()
                        .content(content)
                        .tag(tag)
                        .score(score)
                        .userId(userId)
                        .build();
                if (contentTag == null) {
                    throw new RuntimeException("ContentTag cannot be null");
                }

                contentTagRepository.save(contentTag);
            }
            log.info("Successfully processed content: {} with {} tags", content.getId(), tagScores.size());

        } catch (Exception e) {
            log.error("Error processing content: {}", content.getId(), e);
            // Optionally throw to retry (DLQ strategy needed potentially)
        }
    }
}
