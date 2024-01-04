package com.challenge.rateLimiter.service;

import com.challenge.rateLimiter.handler.TokenBucketHandler;
import com.challenge.rateLimiter.model.request.RateLimitRequest;
import com.challenge.rateLimiter.model.response.RateLimitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RateLimitService {
    @Autowired
    TokenBucketHandler tokenBucketHandler;

    public Mono<RateLimitResponse> rateLimit(String clientId) {
        return tokenBucketHandler.rateLimit(clientId)
                .map(rateLimited -> {
                    RateLimitResponse response = new RateLimitResponse();
                    if (rateLimited) {
                        response.setCode(0);
                        response.setMessage("Pass");
                    } else {
                        response.setCode(-1);
                        response.setMessage("Fail");
                    }
                    return response;
                });
    }
}
