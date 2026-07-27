package com.menstrualtracker.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式限流过滤器 — 基于客户端 IP 对除登录外的所有 API 进行限流。
 * 使用 Redis INCR + EXPIRE 命令，每分钟限制 30 次请求。
 * 键格式：menstrual:rate:ip:{ip}
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    private static final String RATE_KEY_PREFIX = "menstrual:rate:ip:";
    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_SECONDS = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);
        String rateKey = RATE_KEY_PREFIX + ip;

        // Redis INCR 原子递增
        Long count = cacheService.increment(rateKey);
        if (count != null) {
            // 第一次请求时设置过期时间
            if (count == 1) {
                cacheService.expire(rateKey, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            // 超过阈值则拒绝
            if (count > MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {}, count: {}, URI: {}", ip, count, request.getRequestURI());
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(429);
                ApiResponse<Void> apiResponse = ApiResponse.error(429, "Too many requests, please try again later");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }
        }
        // Redis 不可用时降级 —— 直接放行

        filterChain.doFilter(request, response);
    }

    /**
     * 获取客户端真实 IP（考虑代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
