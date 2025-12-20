package dev.kbd.vekku_server.infrastructure.ratelimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, int limit, int durationInSeconds) {
        return cache.computeIfAbsent(key, k -> createNewBucket(limit, durationInSeconds));
    }

    private Bucket createNewBucket(int limit, int durationInSeconds) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillGreedy(limit, Duration.ofSeconds(durationInSeconds))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
