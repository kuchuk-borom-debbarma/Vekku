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
         * <p>
         * Endpoint: POST /learn
         * Payload: { id, alias, synonyms }
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

        /**
         * Retrieves raw semantic tags for a given content string.
         * <p>
         * Endpoint: POST /raw-tags
         */
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

        /**
         * Analyzes content and returns tags mapped to specific regions (chunks) of
         * text.
         * Useful for long-form content.
         * <p>
         * Endpoint: POST /region-tags
         */
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
        public void deleteTag(String tagName) {
                restClient.delete()
                                .uri("/tags/" + tagName)
                                .retrieve()
                                .toBodilessEntity();
        }
}
