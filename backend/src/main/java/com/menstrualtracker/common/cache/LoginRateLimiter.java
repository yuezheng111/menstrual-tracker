package com.menstrualtracker.common.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 本地登录限流器 — 基于 Guava Cache，每 IP 每分钟最多 10 次登录尝试。
 * 纯内存实现，不依赖 Redis，Redis 宕机时仍生效。
 * 登录成功后调用 {@link #onSuccess(String)} 清除该 IP 计数。
 */
@Slf4j
@Component
public class LoginRateLimiter {

    private final Cache<String, Integer> attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    private static final int MAX_ATTEMPTS_PER_MINUTE = 10;

    /**
     * 检查是否允许继续（未超限）
     *
     * @return true = 允许，false = 已限流
     */
    public boolean isAllowed(String clientIp) {
        Integer count = attemptsCache.getIfPresent(clientIp);
        int newCount = (count == null) ? 1 : count + 1;
        attemptsCache.put(clientIp, newCount);

        if (newCount > MAX_ATTEMPTS_PER_MINUTE) {
            log.warn("Login rate limited for IP: {}, attempts: {}", clientIp, newCount);
            return false;
        }
        return true;
    }

    /**
     * 登录成功后清除该 IP 的失败计数
     */
    public void onSuccess(String clientIp) {
        attemptsCache.invalidate(clientIp);
    }
}