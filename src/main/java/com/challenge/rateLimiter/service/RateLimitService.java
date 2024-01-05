package com.challenge.rateLimiter.service;

import com.challenge.rateLimiter.handler.FixedWindowHandler;
import com.challenge.rateLimiter.handler.SlidingWindowCounterHandler;
import com.challenge.rateLimiter.handler.SlidingWindowLogHandler;
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
    @Autowired
    SlidingWindowLogHandler slidingWindowLogHandler;
    @Autowired
    SlidingWindowCounterHandler slidingWindowCounterHandler;

    public Mono<RateLimitResponse> rateLimit(String clientId) {
        return slidingWindowCounterHandler.rateLimit(clientId)
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
