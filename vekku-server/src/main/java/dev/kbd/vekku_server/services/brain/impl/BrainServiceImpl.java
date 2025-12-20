package dev.kbd.vekku_server.services.brain.impl;

import dev.kbd.vekku_server.services.brain.dto.*;
import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
import dev.kbd.vekku_server.services.brain.model.TagScore;
import dev.kbd.vekku_server.services.brain.interfaces.IBrainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

/**
 * 🧠 BRAIN SERVICE IMPLEMENTATION
 */
@Service
public class BrainServiceImpl implements IBrainService {

    private final RestClient restClient;

    public BrainServiceImpl(@Value("${brain-service.url}") String brainServiceUrl,
            RestClient.Builder restClientBuilder) {
        if (brainServiceUrl == null || brainServiceUrl.isEmpty()) {
            throw new IllegalArgumentException("Brain Service URL is required");
        }
        this.restClient = restClientBuilder.baseUrl(brainServiceUrl).build();
    }

    /**
     * Sends a "Learn" request to the Brain Service.
     * Endpoint: POST /learn
     */
    @Override
    public void learnTag(java.util.UUID id, String alias, List<String> synonyms) {
        restClient.post()
                .uri("/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LearnTagRequest(id.toString(), alias, synonyms))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteTag(UUID id) {
        restClient.delete()
                .uri("/tags/" + id)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<TagScore> getRawTagsByEmbedding(String content, Double threshold, Integer topK) {
        RawTagsResponse response = restClient.post()
                .uri("/raw-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RawTagsRequest(content, threshold, topK))
                .retrieve()
                .body(RawTagsResponse.class);

        return response != null ? response.tags() : List.of();
    }

    @Override
    public List<ContentRegionTags> getRegionTags(String content, Double threshold, Integer topK) {
        RegionTagsResponse response = restClient.post()
                .uri("/region-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegionTagsRequest(content, threshold, topK))
                .retrieve()
                .body(RegionTagsResponse.class);

        return response != null ? response.regions() : List.of();
    }

    @Override
    public List<TagScore> scoreTags(List<String> tags, String content) {
        ScoreTagsResponse response = restClient.post()
                .uri("/score-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ScoreTagsRequest(tags, content))
                .retrieve()
                .body(ScoreTagsResponse.class);

        return response != null ? response.scores() : List.of();
    }

    @Override
    public TagListDto getAllTags(Integer limit, String offset) {
        String uri = "/tags?limit=" + limit;
        if (offset != null) {
            uri += "&offset=" + offset;
        }

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(TagListDto.class);
    }

    @Override
    public List<TagScore> extractKeywords(String content, Integer topK, Double diversity) {
        ExtractKeywordsResponse response = restClient.post()
                .uri("/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ExtractKeywordsRequest(content, topK, diversity))
                .retrieve()
                .body(ExtractKeywordsResponse.class);

        return response != null ? response.keywords() : List.of();
    }
}
