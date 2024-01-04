package com.challenge.rateLimiter.controller;

import com.challenge.rateLimiter.model.request.RateLimitRequest;
import com.challenge.rateLimiter.model.response.RateLimitResponse;
import com.challenge.rateLimiter.service.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.http.HttpResponse;

@RestController("/")
public class RateLimitController {
    @Autowired
    RateLimitService rateLimitService;

    @GetMapping("/unlimited")
    public String unlimited() {
        return "Unlimited! Let's Go!";
    }

    @GetMapping("/limited")
    public Mono<RateLimitResponse> limited(ServerWebExchange serverWebExchange) {
        String clientId = serverWebExchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        return rateLimitService.rateLimit(clientId)
                .map(response -> {
                    if (response.getCode() == 0) {
                        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
                    } else {
                        serverWebExchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    }
                    return response;
                });
    }
}
