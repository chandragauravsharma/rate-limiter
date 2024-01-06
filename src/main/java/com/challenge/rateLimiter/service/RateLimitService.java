package com.challenge.rateLimiter.service;

import com.challenge.rateLimiter.handler.RateLimitConfigData;
import com.challenge.rateLimiter.handler.RateLimitHandler;
import com.challenge.rateLimiter.handler.RateLimitHandlerFactory;
import com.challenge.rateLimiter.handler.impl.FixedWindowHandler;
import com.challenge.rateLimiter.handler.impl.SlidingWindowCounterHandler;
import com.challenge.rateLimiter.handler.impl.SlidingWindowLogHandler;
import com.challenge.rateLimiter.handler.impl.TokenBucketHandler;
import com.challenge.rateLimiter.model.response.RateLimitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RateLimitService {
    @Autowired
    RateLimitHandlerFactory handlerFactory;
    @Autowired
    RateLimitConfigData configData;

    public Mono<RateLimitResponse> rateLimit(String clientId) {
        return handlerFactory.createHandler(configData)
                .rateLimit(clientId)
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
