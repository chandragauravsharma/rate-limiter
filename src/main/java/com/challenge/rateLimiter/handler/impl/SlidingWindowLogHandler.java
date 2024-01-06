package com.challenge.rateLimiter.handler.impl;

import com.challenge.rateLimiter.handler.RateLimitConfigData;
import com.challenge.rateLimiter.handler.RateLimitHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
public class SlidingWindowLogHandler implements RateLimitHandler {
    private static Map<String, Queue<Long>> slidingWindowCache = new HashMap<>();
    private int windowSizeInSec;
    private int maxAllowedRequestsPerWindow;

    public SlidingWindowLogHandler(RateLimitConfigData configData) {
        this.windowSizeInSec = configData.getWindowSizeInSecSW();
        this.maxAllowedRequestsPerWindow = configData.getMaxAllowedRequestsPerWindowSW();
    }
    public synchronized Mono<Boolean> rateLimit(String clientId) {
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
