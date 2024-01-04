package com.challenge.rateLimiter.model;

import lombok.Data;

@Data
public class TokenBucket {
    int tokens;
    long lastUpdateTimeStamp;
}
