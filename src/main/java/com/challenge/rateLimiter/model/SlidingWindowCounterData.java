package com.challenge.rateLimiter.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlidingWindowCounterData {
    int currentWindowRequestCount;
    int previousWindowRequestCount;
    long lastWindowStartTime;
}
