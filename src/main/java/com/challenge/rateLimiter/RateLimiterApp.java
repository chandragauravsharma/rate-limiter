package com.challenge.rateLimiter;

import com.challenge.rateLimiter.handler.RateLimitConfigData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class RateLimiterApp {
    public static void main(String[] args) {
        SpringApplication.run(RateLimiterApp.class, args);
    }
}
