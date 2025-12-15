package dev.kbd.vekku_server.services.independent.brainService.remote;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Set;

@Service
public class RemoteBrainService implements BrainService {

    private final RestClient restClient;

    public RemoteBrainService(@Value("${brain-service.url}") String brainServiceUrl,
            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(brainServiceUrl).build();
    }

    @Override
    public void learnTag(String tagName) {
        record LearnRequest(String tag_name) {
        }

        restClient.post()
                .uri("/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LearnRequest(tagName))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public Set<String> suggestTags(String content) {
        record SuggestTagsRequest(String content) {
        }
        record SuggestTagsResponse(Set<String> tags) {
        }

        SuggestTagsResponse response = restClient.post()
                .uri("/suggest-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SuggestTagsRequest(content))
                .retrieve()
                .body(SuggestTagsResponse.class);

        return response != null ? response.tags() : Set.of();
    }
}
