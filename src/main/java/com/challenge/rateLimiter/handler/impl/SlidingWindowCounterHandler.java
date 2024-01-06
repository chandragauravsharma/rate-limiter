package com.challenge.rateLimiter.handler.impl;

import com.challenge.rateLimiter.handler.RateLimitConfigData;
import com.challenge.rateLimiter.handler.RateLimitHandler;
import com.challenge.rateLimiter.model.SlidingWindowCounterData;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class SlidingWindowCounterHandler implements RateLimitHandler {
    private static Map<String, SlidingWindowCounterData> slidingWindowCache = new HashMap<>();
    private int windowSizeInSec;
    private int maxAllowedRequestsPerWindow;

    public SlidingWindowCounterHandler(RateLimitConfigData configData) {
        this.windowSizeInSec = configData.getWindowSizeInSecSW();
        this.maxAllowedRequestsPerWindow = configData.getMaxAllowedRequestsPerWindowSW();
    }

    public synchronized Mono<Boolean> rateLimit(String clientId) {
        long currentTime = System.currentTimeMillis();
        if (slidingWindowCache.containsKey(clientId)) {
            SlidingWindowCounterData data = slidingWindowCache.get(clientId);
            int elapsedTimeInSec = (int)(currentTime / 1000 - data.getLastWindowStartTime() / 1000);
            if (elapsedTimeInSec < windowSizeInSec) {
                if (data.getCurrentWindowRequestCount() < maxAllowedRequestsPerWindow) {
                    // allow
                    data.setCurrentWindowRequestCount(data.getCurrentWindowRequestCount() + 1);
                    slidingWindowCache.put(clientId, data);
                    return Mono.just(true);
                } else {
                    // reject
                    return Mono.just(false);
                }
            } else {
                // consider previous and current window weightage
                int previousWindowPercent = 100 - ((elapsedTimeInSec / windowSizeInSec) * 100);
                int totalRequests = data.getCurrentWindowRequestCount() + (data.getPreviousWindowRequestCount() * (previousWindowPercent / 100));
                if (totalRequests < maxAllowedRequestsPerWindow) {
                    int currentCount = totalRequests;
                    int previousCount = data.getPreviousWindowRequestCount() - (data.getPreviousWindowRequestCount() * (previousWindowPercent / 100));
                    data.setCurrentWindowRequestCount(currentCount);
                    data.setPreviousWindowRequestCount(previousCount);
                    data.setLastWindowStartTime(currentTime);
                    slidingWindowCache.put(clientId, data);
                    return Mono.just(true);
                } else {
                    // reject
                    return Mono.just(false);
                }
            }
        } else {
            // first time
            SlidingWindowCounterData data = SlidingWindowCounterData.builder()
                    .currentWindowRequestCount(1)
                    .previousWindowRequestCount(0)
                    .lastWindowStartTime(currentTime)
                    .build();
            slidingWindowCache.put(clientId, data);
            return Mono.just(true);
        }
    }
}
