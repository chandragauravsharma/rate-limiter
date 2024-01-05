package com.challenge.rateLimiter.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@ConfigurationProperties("app")
@Component
public class SlidingWindowLogHandler {
    private Map<String, Queue<Long>> slidingWindowCache;
    @Value("${app.sliding-window.window-size-in-secs}")
    private int windowSizeInSec;
    @Value("${app.sliding-window.max-allowed-requests-per-window}")
    private int maxAllowedRequestsPerWindow;

    public SlidingWindowLogHandler() {
        this.slidingWindowCache = new HashMap<>();
    }
    public Mono<Boolean> rateLimit(String clientId) {
        long currentTime = System.currentTimeMillis();
        if (slidingWindowCache.containsKey(clientId)) {
            Queue<Long> timestamps = slidingWindowCache.get(clientId);
            while (!timestamps.isEmpty() && (currentTime/1000 - timestamps.peek()/1000 >= windowSizeInSec)) {
                timestamps.poll();
            }

            if (timestamps.size() < maxAllowedRequestsPerWindow) {
                timestamps.add(currentTime);
                slidingWindowCache.put(clientId, timestamps);
                return Mono.just(true);
            } else {
                return Mono.just(false);
            }
        } else {
            // first time
            Queue<Long> timestamps = new LinkedList<>();
            timestamps.add(System.currentTimeMillis());
            slidingWindowCache.put(clientId, timestamps);
            return Mono.just(true);
        }
    }
}
