package com.challenge.rateLimiter.handler;

import com.challenge.rateLimiter.model.SlidingWindowCounterData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("app")
@Component
public class SlidingWindowCounterHandler {
    private static Map<String, SlidingWindowCounterData> slidingWindowCache;
    @Value("${app.sliding-window.window-size-in-secs}")
    private int windowSizeInSec;
    @Value("${app.sliding-window.max-allowed-requests-per-window}")
    private int maxAllowedRequestsPerWindow;

    public SlidingWindowCounterHandler() {
        slidingWindowCache = new HashMap<>();
    }

    public Mono<Boolean> rateLimit(String clientId) {
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
