package com.challenge.rateLimiter.service;

import com.challenge.rateLimiter.handler.FixedWindowHandler;
import com.challenge.rateLimiter.handler.TokenBucketHandler;
import com.challenge.rateLimiter.model.response.RateLimitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RateLimitService {
    @Autowired
    TokenBucketHandler tokenBucketHandler;
    @Autowired
    FixedWindowHandler fixedWindowHandler;

    public Mono<RateLimitResponse> rateLimit(String clientId) {
        return fixedWindowHandler.rateLimit(clientId)
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
