package com.challenge.rateLimiter.handler;

import com.challenge.rateLimiter.model.TokenBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("app")
@Component
public class TokenBucketHandler {
    private static Map<String, TokenBucket> tokenBucketMap;
    @Value("${app.token-bucket.default-tokens}")
    private int defaultTokens = 10;
    @Value("${app.token-bucket.refresh-interval}")
    private int refreshInterval = 1; // in secs

    public TokenBucketHandler() {
        tokenBucketMap = new HashMap<>();
    }
    public Mono<Boolean> rateLimit(String clientId) {
        if (tokenBucketMap.containsKey(clientId)) {
            // not first time user
            TokenBucket tokenBucket = tokenBucketMap.get(clientId);
            int currentTokens = tokenBucket.getTokens();
            if (currentTokens <= 0) {
                long lastUpdatedTime = tokenBucket.getLastUpdateTimeStamp();
                long elapsedTimeInSecs = (System.currentTimeMillis() - lastUpdatedTime) / (refreshInterval * 1000);
                int newTokens = (int)elapsedTimeInSecs; // 1 token every sec
                currentTokens = newTokens;
                if (currentTokens > defaultTokens) {
                    currentTokens = defaultTokens;
                }
                if (currentTokens > 0) {
                    currentTokens = currentTokens - 1;
                    tokenBucket.setTokens(currentTokens);
                    tokenBucket.setLastUpdateTimeStamp(System.currentTimeMillis());
                    tokenBucketMap.put(clientId, tokenBucket);
                    return Mono.just(true);
                } else {
                    return Mono.just(false);
                }
            } else {
                currentTokens = currentTokens - 1;
                tokenBucket.setTokens(currentTokens);
                tokenBucketMap.put(clientId, tokenBucket);
                return Mono.just(true);
            }
        } else {
            // first time user
            // set default_tokens - 1 and pass rate limit
            TokenBucket tokenBucket = new TokenBucket();
            tokenBucket.setTokens(defaultTokens - 1);
            tokenBucket.setLastUpdateTimeStamp(System.currentTimeMillis());
            tokenBucketMap.put(clientId, tokenBucket);
            return Mono.just(true);
        }
    }
}
