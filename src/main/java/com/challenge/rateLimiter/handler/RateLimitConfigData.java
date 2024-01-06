package com.challenge.rateLimiter.handler;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app")
@Data
public class RateLimitConfigData {
    @Value("${app.algorithm}")
    private String algorithm;

    // token-bucket
    @Value("${app.config.token-bucket.max-default-tokens}")
    private int maxDefaultTokens;

    @Value("${app.config.token-bucket.refresh-interval}")
    private int refreshInterval; // in secs

    @Value("${app.config.token-bucket.new-tokens-every-refresh-interval}")
    private int newTokensEveryRefreshInterval;

    // fixed-window
    @Value("${app.config.fixed-window.window-size-in-secs}")
    private int windowSizeInSecFW;

    @Value("${app.config.fixed-window.max-allowed-requests-per-window}")
    private int maxAllowedRequestsPerWindowFW;

    // sliding window
    @Value("${app.config.sliding-window.window-size-in-secs}")
    private int windowSizeInSecSW;

    @Value("${app.config.sliding-window.max-allowed-requests-per-window}")
    private int maxAllowedRequestsPerWindowSW;
}
