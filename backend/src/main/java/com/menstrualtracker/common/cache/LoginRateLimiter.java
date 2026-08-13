package com.menstrualtracker.common.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis-backed login limiter keyed by IP and username. Failures accumulate,
 * success clears the counters, and repeated failures create a lockout.
 * When Redis is unavailable the limiter falls back to a local cache instead
 * of allowing requests through without any limit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    private final CacheService cacheService;

    private static final String FAIL_IP_PREFIX = "menstrual:login:fail:ip:";
    private static final String FAIL_USER_PREFIX = "menstrual:login:fail:user:";
    private static final String LOCK_PREFIX = "menstrual:login:lock:";
    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_SECONDS = 900;
    private static final long LOCK_SECONDS = 900;

    private final Cache<String, Integer> localFailures = CacheBuilder.newBuilder()
            .expireAfterWrite(WINDOW_SECONDS, TimeUnit.SECONDS)
            .maximumSize(10000)
            .build();
    private final Cache<String, Long> localLocks = CacheBuilder.newBuilder()
            .expireAfterWrite(LOCK_SECONDS, TimeUnit.SECONDS)
            .maximumSize(10000)
            .build();

    public boolean isBlocked(String ip, String username) {
        String lockKey = lockKey(ip, username);
        Boolean redisLock = cacheService.exists(lockKey);
        if (redisLock != null) {
            if (redisLock) {
                return true;
            }
            return countReached(ip, username);
        }
        if (localLocks.getIfPresent(lockKey) != null) {
            return true;
        }
        return localCountReached(ip, username);
    }

    public void onFailure(String ip, String username) {
        String ipKey = failIpKey(ip);
        String userKey = failUserKey(username);
        Long ipCount = cacheService.increment(ipKey);
        Long userCount = cacheService.increment(userKey);
        if (ipCount != null && userCount != null) {
            if (ipCount == 1) {
                cacheService.expire(ipKey, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (userCount == 1) {
                cacheService.expire(userKey, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (ipCount >= MAX_FAILURES || userCount >= MAX_FAILURES) {
                cacheService.set(lockKey(ip, username), "1", LOCK_SECONDS, TimeUnit.SECONDS);
            }
        } else {
            int localIpCount = incrementLocal(localFailures, ipKey);
            int localUserCount = incrementLocal(localFailures, userKey);
            if (localIpCount >= MAX_FAILURES || localUserCount >= MAX_FAILURES) {
                localLocks.put(lockKey(ip, username), System.currentTimeMillis() + LOCK_SECONDS * 1000);
            }
        }
    }

    public void onSuccess(String ip, String username) {
        cacheService.delete(failIpKey(ip));
        cacheService.delete(failUserKey(username));
        cacheService.delete(lockKey(ip, username));
        localFailures.invalidate(failIpKey(ip));
        localFailures.invalidate(failUserKey(username));
        localLocks.invalidate(lockKey(ip, username));
    }

    private boolean countReached(String ip, String username) {
        Integer ipCount = asInt(cacheService.get(failIpKey(ip)));
        Integer userCount = asInt(cacheService.get(failUserKey(username)));
        return (ipCount != null && ipCount >= MAX_FAILURES)
                || (userCount != null && userCount >= MAX_FAILURES);
    }

    private boolean localCountReached(String ip, String username) {
        Integer ipCount = localFailures.getIfPresent(failIpKey(ip));
        Integer userCount = localFailures.getIfPresent(failUserKey(username));
        return (ipCount != null && ipCount >= MAX_FAILURES)
                || (userCount != null && userCount >= MAX_FAILURES);
    }

    private int incrementLocal(Cache<String, Integer> cache, String key) {
        Integer current = cache.getIfPresent(key);
        int next = current == null ? 1 : current + 1;
        cache.put(key, next);
        return next;
    }

    private Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private String failIpKey(String ip) {
        return FAIL_IP_PREFIX + ip;
    }

    private String failUserKey(String username) {
        return FAIL_USER_PREFIX + username;
    }

    private String lockKey(String ip, String username) {
        return LOCK_PREFIX + ip + ":" + username;
    }
}
