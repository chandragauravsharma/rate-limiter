package com.challenge.rateLimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class RateLimitController {
    @GetMapping("/unlimited")
    public String unlimited() {
        return "Unlimited! Let's Go!";
    }

    @GetMapping("/limited")
    public String limited() {
        return "Limited, don't over use me!";
    }
}
