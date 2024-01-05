package com.challenge.rateLimiter.handler;

import com.challenge.rateLimiter.model.FixedWindowData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("app")
@Component
public class FixedWindowHandler {
    private static Map<String, FixedWindowData> fixedWindowDataMap;
    @Value("${app.fixed-window.window-size-in-secs}")
    private int windowSizeInSec;
    @Value("${app.fixed-window.max-allowed-requests-per-window}")
    private int maxAllowedRequestsPerWindow;

    public FixedWindowHandler() {
        this.fixedWindowDataMap = new HashMap<>();
    }

    public Mono<Boolean> rateLimit(String clientId) {
        if (fixedWindowDataMap.containsKey(clientId)) {
            int elapsedTimeInSec = (int)((System.currentTimeMillis() / 1000) - (fixedWindowDataMap.get(clientId).getLastWindowStartTime() / 1000));
            // check for a new window or not
            if (elapsedTimeInSec >= windowSizeInSec) {
                // new window starts here
                FixedWindowData data = FixedWindowData.builder().allowedRequestCount(1).lastWindowStartTime(System.currentTimeMillis()).build();
                fixedWindowDataMap.put(clientId, data);
                return Mono.just(true);
            } else {
                if (fixedWindowDataMap.get(clientId).getAllowedRequestCount() < maxAllowedRequestsPerWindow) {
                    FixedWindowData data = fixedWindowDataMap.get(clientId);
                    data.setAllowedRequestCount(data.getAllowedRequestCount() + 1);
                    fixedWindowDataMap.put(clientId, data);
                    return Mono.just(true);
                } else {
                    // fail
                    return Mono.just(false);
                }
            }
        } else {
            // first time
            FixedWindowData data = FixedWindowData.builder().allowedRequestCount(1).lastWindowStartTime(System.currentTimeMillis()).build();
            fixedWindowDataMap.put(clientId, data);
            return Mono.just(true);
        }
    }
}
