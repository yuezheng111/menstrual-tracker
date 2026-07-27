package com.menstrualtracker.common.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 本地 Token 黑名单缓存 — 存储已注销但还未过期的 JWT。
 * 作为 Redis 会话管理的兜底方案：当 Redis 不可用时，已注销的 Token 仍会被拒绝。
 * TTL 与 JWT 过期时间一致（24 小时），自动过期清理。
 */
@Slf4j
public class TokenBlacklistCache {

    private final Cache<String, String> blacklist;

    public TokenBlacklistCache(Cache<String, String> blacklist) {
        this.blacklist = blacklist;
    }

    /** 将 Token 加入黑名单（用户登出时调用） */
    public void add(String token) {
        blacklist.put(token, token);
        log.debug("Token added to local blacklist");
    }

    /** 检查 Token 是否在黑名单中（已被注销） */
    public boolean contains(String token) {
        return blacklist.getIfPresent(token) != null;
    }
}