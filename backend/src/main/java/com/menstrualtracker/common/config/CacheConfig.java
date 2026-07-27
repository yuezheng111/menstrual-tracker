package com.menstrualtracker.common.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.menstrualtracker.common.cache.TokenBlacklistCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, String> tokenBlacklistCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    @Bean
    public TokenBlacklistCache tokenBlacklistCacheWrapper(Cache<String, String> tokenBlacklistCache) {
        return new TokenBlacklistCache(tokenBlacklistCache);
    }

    @Bean
    public Cache<String, String> verificationCodeCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
