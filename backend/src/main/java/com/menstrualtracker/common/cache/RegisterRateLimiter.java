package com.menstrualtracker.common.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterRateLimiter {

    private final CacheService cacheService;

    private static final String PREFIX = "menstrual:register:ip:";
    private static final int MAX_PER_MINUTE = 10;
    private static final long WINDOW_SECONDS = 60;

    private final Cache<String, Integer> local = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    public boolean isAllowed(String ip) {
        String key = PREFIX + ip;
        Long count = cacheService.increment(key);
        if (count != null) {
            if (count == 1) {
                cacheService.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            return count <= MAX_PER_MINUTE;
        }
        Integer localCount = local.getIfPresent(key);
        int next = localCount == null ? 1 : localCount + 1;
        local.put(key, next);
        return next <= MAX_PER_MINUTE;
    }
}
