package dev.kbd.vekku_server.services.brain.remote;

import dev.kbd.vekku_server.services.brain.BrainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
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

        @Override
        public List<dev.kbd.vekku_server.services.brain.model.TagScore> getRawTagsByEmbedding(
                        String content, Double threshold, Integer topK) {
                record RawTagsRequest(String content, Double threshold, Integer topK) {
                }
                record RawTagsResponse(
                                List<dev.kbd.vekku_server.services.brain.model.TagScore> tags) {
                }

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
                record RegionTagsRequest(String content, Double threshold, Integer topK) {
                }
                record RegionTagsResponse(List<ContentRegionTags> regions) {
                }

                RegionTagsResponse response = restClient.post()
                                .uri("/region-tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(new RegionTagsRequest(content, threshold, topK))
                                .retrieve()
                                .body(RegionTagsResponse.class);

                return response != null ? response.regions() : List.of();
        }

        @Override
        public List<dev.kbd.vekku_server.services.brain.model.TagScore> scoreTags(List<String> tags,
                        String content) {
                record ScoreTagsRequest(List<String> tags, String content) {
                }
                record ScoreTagsResponse(
                                List<dev.kbd.vekku_server.services.brain.model.TagScore> scores) {
                }

                ScoreTagsResponse response = restClient.post()
                                .uri("/score-tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(new ScoreTagsRequest(tags, content))
                                .retrieve()
                                .body(ScoreTagsResponse.class);

                return response != null ? response.scores() : List.of();
        }

        @Override
        public List<dev.kbd.vekku_server.services.brain.model.TagScore> suggestCombinedTags(String content,
                        Double threshold, Integer topK) {
                record SuggestCombinedRequest(String content, Double threshold, Integer topK) {
                }
                // Reusing RawTagsResponse structure as it is { tags: [...] }
                record SuggestCombinedResponse(
                                List<dev.kbd.vekku_server.services.brain.model.TagScore> tags) {
                }

                SuggestCombinedResponse response = restClient.post()
                                .uri("/suggest-combined")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(new SuggestCombinedRequest(content, threshold, topK))
                                .retrieve()
                                .body(SuggestCombinedResponse.class);

                return response != null ? response.tags() : List.of();
        }
}
