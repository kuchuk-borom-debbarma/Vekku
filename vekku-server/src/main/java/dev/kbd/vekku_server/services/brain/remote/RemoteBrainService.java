package dev.kbd.vekku_server.services.brain.remote;

import dev.kbd.vekku_server.services.brain.BrainService;
import dev.kbd.vekku_server.services.brain.dto.*;
import dev.kbd.vekku_server.services.brain.model.ContentRegionTags;
import dev.kbd.vekku_server.services.brain.model.TagScore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * 🌐 REMOTE BRAIN SERVICE: Connects to the external Node.js Brain Service.
 * <p>
 * Acts as a client (Bridge) to the Python/Node.js microservice
 * (`vekku-brain-service`).
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
         */
        @Override
        public void learnTag(String tagName) {
                restClient.post()
                                .uri("/learn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(new LearnRequest(tagName))
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
}
