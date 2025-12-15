package dev.kbd.vekku_server.services.independent.brainService.remote;

import dev.kbd.vekku_server.brain.BrainServiceGrpc;
import dev.kbd.vekku_server.brain.LearnRequest;
import dev.kbd.vekku_server.brain.SuggestTagsRequest;
import dev.kbd.vekku_server.brain.SuggestTagsResponse;
import dev.kbd.vekku_server.services.independent.brainService.BrainService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RemoteBrainService implements BrainService {

    @GrpcClient("brainService")
    private BrainServiceGrpc.BrainServiceBlockingStub brainClient;

    @Override
    public void learnTag(String tagName) {
        LearnRequest request = LearnRequest.newBuilder()
                .setTagName(tagName)
                .build();
        brainClient.learn(request);
    }

    @Override
    public Set<String> suggestTags(String content) {
        SuggestTagsRequest request = SuggestTagsRequest.newBuilder()
                .setContent(content)
                .build();
        SuggestTagsResponse response = brainClient.suggestTags(request);
        return new HashSet<>(response.getTagsList());
    }
}
