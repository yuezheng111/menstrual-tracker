package com.menstrualtracker.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.cache.TokenBlacklistCache;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.util.JwtUtil;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
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
import java.util.List;

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
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_CACHE_KEY = "menstrual:token:%s";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtUtil.isTokenExpired(token)) {
            log.info("Token expired, rejecting request: uri={}", request.getRequestURI());
            writeUnauthorized(response);
            return;
        }

        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String tokenKey = String.format(TOKEN_CACHE_KEY, token);

            // 1. 先查本地黑名单（Redis 宕机时仍能识别已注销的 Token）
            if (tokenBlacklistCache.contains(token)) {
                log.info("Token found in local blacklist (user logged out): userId={}", userId);
                writeUnauthorized(response);
                return;
            }

            // 2. 再查 Redis 会话映射
            Boolean sessionExists = cacheService.exists(tokenKey);
            if (sessionExists == null) {
                log.warn("Redis unavailable; falling back to JWT validation only: userId={}", userId);
            } else if (!sessionExists) {
                log.info("Token not found in Redis session (user logged out): userId={}", userId);
                writeUnauthorized(response);
                return;
            }

            String role = jwtUtil.getRoleFromToken(token);
            if ("ADMIN".equals(role) && !"/api/admin/password/change".equals(request.getRequestURI())) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && Boolean.TRUE.equals(user.getPasswordChangeRequired())) {
                    log.info("Admin must change password before using admin APIs: userId={}", userId);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write(objectMapper.writeValueAsString(
                            ApiResponse.error(403, "Please change your password first")));
                    return;
                }
            }
            List<org.springframework.security.core.GrantedAuthority> authorities =
                    "ADMIN".equals(role)
                            ? List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
                            : Collections.emptyList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/api/auth/refresh-token".equals(request.getRequestURI())
                || "/api/auth/refresh-token".equals(request.getServletPath());
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(401, "Unauthorized")));
    }
}
