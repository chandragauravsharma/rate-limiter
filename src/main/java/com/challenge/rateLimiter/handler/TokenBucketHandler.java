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
    @Value("${app.token-bucket.max-default-tokens}")
    private int maxDefaultTokens;
    @Value("${app.token-bucket.refresh-interval}")
    private int refreshInterval; // in secs
    @Value("${app.token-bucket.new-tokens-every-refresh-interval}")
    private int newTokensEveryRefreshInterval;

    public TokenBucketHandler() {
        tokenBucketMap = new HashMap<>();
    }
    public Mono<Boolean> rateLimit(String clientId) {
        if (tokenBucketMap.containsKey(clientId)) {
            // not first time user
            TokenBucket tokenBucket = tokenBucketMap.get(clientId);
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
                tokenBucketMap.put(clientId, tokenBucket);
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
            tokenBucketMap.put(clientId, tokenBucket);
            return Mono.just(true);
        }
    }
}
