package com.menstrualtracker.common.filter;

import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.cache.TokenBlacklistCache;
import com.menstrualtracker.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器 — 验证 Token 有效性，同时检查 Redis 中是否存在该 Token 对应的会话映射。
 * 若 Redis 中无映射（用户已主动注销），则拒绝请求。
 * Redis 不可用时降级 —— 先查本地黑名单兜底，再仅验证 JWT 本身。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CacheService cacheService;
    private final TokenBlacklistCache tokenBlacklistCache;

    private static final String TOKEN_CACHE_KEY = "menstrual:token:%s";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String tokenKey = String.format(TOKEN_CACHE_KEY, token);

            // 1. 先查本地黑名单（Redis 宕机时仍能识别已注销的 Token）
            if (tokenBlacklistCache.contains(token)) {
                log.info("Token found in local blacklist (user logged out, Redis degraded): userId={}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 再查 Redis 会话映射
            Boolean sessionExists = cacheService.exists(tokenKey);

            if (sessionExists != null && !sessionExists) {
                // Redis 明确返回 false，表示 Token 已被注销
                log.info("Token not found in Redis session (user logged out): userId={}", userId);
                filterChain.doFilter(request, response);
                return;
            }
            // sessionExists == null 表示 Redis 不可用 → 降级，本地黑名单已在步骤 1 兜底，信任 JWT

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}