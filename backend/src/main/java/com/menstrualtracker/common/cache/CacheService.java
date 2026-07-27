package com.menstrualtracker.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类 — 封装常用缓存操作，所有方法均带降级逻辑。
 * Redis 不可用时打印日志并返回默认值，不影响核心业务流程。
 * 所有缓存键统一使用前缀 "menstrual:"，便于管理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 基础操作 ====================

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Cache SET: key={}, timeout={} {}", key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis SET failed (degraded): key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Cache GET: key={}, hit={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.warn("Redis GET failed (degraded): key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除单个缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Cache DELETE: key={}", key);
        } catch (Exception e) {
            log.warn("Redis DELETE failed (degraded): key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 批量删除 — 使用 SCAN 命令（生产环境避免 KEYS 阻塞）
     */
    public void deleteByPattern(String pattern) {
        try {
            List<String> keysToDelete = new ArrayList<>();
            redisTemplate.executeWithStickyConnection(connection -> {
                try (Cursor<byte[]> cursor = connection.scan(
                        ScanOptions.scanOptions().match(pattern).count(100).build())) {
                    while (cursor.hasNext()) {
                        keysToDelete.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                }
                return null;
            });
            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("Cache deleteByPattern: pattern={}, deleted {} keys", pattern, keysToDelete.size());
            }
        } catch (Exception e) {
            log.warn("Redis deleteByPattern failed (degraded): pattern={}, error={}", pattern, e.getMessage());
        }
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Redis EXISTS failed (degraded): key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有匹配模式的 key 集合（用于小范围查询）
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.warn("Redis KEYS failed (degraded): pattern={}, error={}", pattern, e.getMessage());
            return Set.of();
        }
    }

    // ==================== 分布式计数器（用于限流） ====================

    /**
     * 原子递增计数器
     */
    public Long increment(String key) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            log.debug("Cache INCR: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.warn("Redis INCR failed (degraded): key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 设置过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis EXPIRE failed (degraded): key={}, error={}", key, e.getMessage());
        }
    }
}

