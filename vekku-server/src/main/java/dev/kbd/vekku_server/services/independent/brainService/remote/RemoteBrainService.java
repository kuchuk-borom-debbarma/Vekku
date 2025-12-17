package dev.kbd.vekku_server.services.independent.brainService.remote;

import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import dev.kbd.vekku_server.services.independent.brainService.model.ContentRegionTags;
import java.util.List;

/**
 * 🌐 REMOTE BRAIN SERVICE: Connects to the external Node.js Brain Service.
 * <p>
 * Instead of running the heavy AI models inside the JVM, this service acts as a
 * client
 * (Bridge) to the Python/Node.js microservice (`vekku-brain-service`) which
 * hosts
 * the Embedding Model and Vector Database connection.
 * <p>
 * Design Pattern:
 * <a href="https://microservices.io/patterns/apigateway.html">API Gateway /
 * Adapter</a>
 */
@Service
public class RemoteBrainService implements BrainService {

    private final RestClient restClient;

    public RemoteBrainService(@Value("${brain-service.url}") String brainServiceUrl,
            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(brainServiceUrl).build();
    }

    /**
     * Sends a "Learn" request to the Brain Service.
     * The service will embed this tag and store it in Qdrant.
     */
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

    /**
     * Sends content to the Brain Service to get semantic tag suggestions.
     * The response contains a list of regions (chunks) with their respective tag
     * scores.
     */
    @Override
    public List<ContentRegionTags> suggestTags(String content, Double threshold, Integer topK) {
        record SuggestTagsRequest(String content, Double threshold, Integer topK) {
        }
        record SuggestTagsResponse(List<ContentRegionTags> regions) {
        }

        SuggestTagsResponse response = restClient.post()
                .uri("/suggest-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SuggestTagsRequest(content, threshold, topK))
                .retrieve()
                .body(SuggestTagsResponse.class);

        return response != null ? response.regions() : List.of();
    }

    @Override
    public List<dev.kbd.vekku_server.services.independent.brainService.model.TagScore> scoreTags(List<String> tags,
            String content) {
        record ScoreTagsRequest(List<String> tags, String content) {
        }
        record ScoreTagsResponse(List<dev.kbd.vekku_server.services.independent.brainService.model.TagScore> scores) {
        }

        ScoreTagsResponse response = restClient.post()
                .uri("/score-tags")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ScoreTagsRequest(tags, content))
                .retrieve()
                .body(ScoreTagsResponse.class);

        return response != null ? response.scores() : List.of();
    }
}
