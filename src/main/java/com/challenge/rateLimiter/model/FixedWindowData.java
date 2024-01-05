package com.challenge.rateLimiter.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FixedWindowData {
    int allowedRequestCount;
    long lastWindowStartTime;
}
