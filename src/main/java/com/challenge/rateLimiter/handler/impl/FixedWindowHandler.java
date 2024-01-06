package com.challenge.rateLimiter.handler.impl;

import com.challenge.rateLimiter.handler.RateLimitConfigData;
import com.challenge.rateLimiter.handler.RateLimitHandler;
import com.challenge.rateLimiter.model.FixedWindowData;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class FixedWindowHandler implements RateLimitHandler {
    private static Map<String, FixedWindowData> fixedWindowCache = new HashMap<>();
    private int windowSizeInSec;
    private int maxAllowedRequestsPerWindow;

    public FixedWindowHandler(RateLimitConfigData configData) {
        this.windowSizeInSec = configData.getWindowSizeInSecFW();
        this.maxAllowedRequestsPerWindow = configData.getMaxAllowedRequestsPerWindowFW();
    }

    public synchronized Mono<Boolean> rateLimit(String clientId) {
        if (fixedWindowCache.containsKey(clientId)) {
            int elapsedTimeInSec = (int)((System.currentTimeMillis() / 1000) - (fixedWindowCache.get(clientId).getLastWindowStartTime() / 1000));
            // check for a new window or not
            if (elapsedTimeInSec >= windowSizeInSec) {
                // new window starts here
                FixedWindowData data = FixedWindowData.builder().allowedRequestCount(1).lastWindowStartTime(System.currentTimeMillis()).build();
                fixedWindowCache.put(clientId, data);
                return Mono.just(true);
            } else {
                if (fixedWindowCache.get(clientId).getAllowedRequestCount() < maxAllowedRequestsPerWindow) {
                    FixedWindowData data = fixedWindowCache.get(clientId);
                    data.setAllowedRequestCount(data.getAllowedRequestCount() + 1);
                    fixedWindowCache.put(clientId, data);
                    return Mono.just(true);
                } else {
                    // fail
                    return Mono.just(false);
                }
            }
        } else {
            // first time
            FixedWindowData data = FixedWindowData.builder().allowedRequestCount(1).lastWindowStartTime(System.currentTimeMillis()).build();
            fixedWindowCache.put(clientId, data);
            return Mono.just(true);
        }
    }
}
