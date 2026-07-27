package com.menstrualtracker.common.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RateLimiterConfig {

    @Value("${rate-limiter.permits-per-second:10.0}")
    private double permitsPerSecond;

    @Value("${rate-limiter.warmup-seconds:5}")
    private int warmupSeconds;

    @Bean
    public RateLimiter globalRateLimiter() {
        if (warmupSeconds > 0) {
            return RateLimiter.create(permitsPerSecond, warmupSeconds, TimeUnit.SECONDS);
        }
        return RateLimiter.create(permitsPerSecond);
    }
}
