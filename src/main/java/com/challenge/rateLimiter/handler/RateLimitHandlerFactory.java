package com.challenge.rateLimiter.handler;


import com.challenge.rateLimiter.handler.impl.FixedWindowHandler;
import com.challenge.rateLimiter.handler.impl.SlidingWindowCounterHandler;
import com.challenge.rateLimiter.handler.impl.SlidingWindowLogHandler;
import com.challenge.rateLimiter.handler.impl.TokenBucketHandler;
import org.springframework.stereotype.Component;

@Component
public class RateLimitHandlerFactory {
    public RateLimitHandler createHandler(RateLimitConfigData configData) {
        RateLimitHandler handler;
        switch (configData.getAlgorithm()) {
            case "fixed-window":
                handler = new FixedWindowHandler(configData);
                break;
            case "sliding-window-log":
                handler = new SlidingWindowLogHandler(configData);
                break;
            case "sliding-window-counter":
                handler = new SlidingWindowCounterHandler(configData);
                break;
            case "token-bucket":
                handler = new TokenBucketHandler(configData);
                break;
            default:
                throw new RuntimeException("No Handler defined for type: " + configData.getAlgorithm());
        }

        return handler;
    }
}
