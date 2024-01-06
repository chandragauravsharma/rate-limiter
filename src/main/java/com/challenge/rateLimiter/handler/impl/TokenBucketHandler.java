package com.challenge.rateLimiter.handler.impl;

import com.challenge.rateLimiter.handler.RateLimitConfigData;
import com.challenge.rateLimiter.handler.RateLimitHandler;
import com.challenge.rateLimiter.model.TokenBucket;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenBucketHandler implements RateLimitHandler {
    private static Map<String, TokenBucket> tokenBucketCache = new HashMap<>();
    private int maxDefaultTokens;
    private int refreshInterval; // in secs
    private int newTokensEveryRefreshInterval;

    public TokenBucketHandler(RateLimitConfigData configData) {
        this.maxDefaultTokens = configData.getMaxDefaultTokens();
        this.refreshInterval = configData.getRefreshInterval();
        this.newTokensEveryRefreshInterval = configData.getNewTokensEveryRefreshInterval();
    }
    public synchronized Mono<Boolean> rateLimit(String clientId) {
        if (tokenBucketCache.containsKey(clientId)) {
            // not first time user
            TokenBucket tokenBucket = tokenBucketCache.get(clientId);
            long elapsedTimeInSecs = (System.currentTimeMillis() - tokenBucket.getLastUpdateTimeStamp()) / (refreshInterval * 1000);
            int newTokens = tokenBucket.getTokens() + ((int)elapsedTimeInSecs * newTokensEveryRefreshInterval); // 1 token every sec here
            if (newTokens > maxDefaultTokens) {
                newTokens = maxDefaultTokens;
            }
            long newTimeUpdate = tokenBucket.getLastUpdateTimeStamp();
            if (elapsedTimeInSecs >= refreshInterval) {
                newTimeUpdate = System.currentTimeMillis();
            }

            if (newTokens > 0) {
                newTokens--;
                tokenBucket.setTokens(newTokens);
                tokenBucket.setLastUpdateTimeStamp(newTimeUpdate);
                tokenBucketCache.put(clientId, tokenBucket);
                return Mono.just(true);
            } else {
                return Mono.just(false);
            }
        } else {
            // first time user
            // set default_tokens - 1 and pass rate limit
            TokenBucket tokenBucket = new TokenBucket();
            tokenBucket.setTokens(maxDefaultTokens - 1);
            tokenBucket.setLastUpdateTimeStamp(System.currentTimeMillis());
            tokenBucketCache.put(clientId, tokenBucket);
            return Mono.just(true);
        }
    }
}
