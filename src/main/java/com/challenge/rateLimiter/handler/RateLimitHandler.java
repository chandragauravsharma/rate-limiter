package com.challenge.rateLimiter.handler;

import reactor.core.publisher.Mono;

public interface RateLimitHandler {
    Mono<Boolean> rateLimit(String clientId);
}
