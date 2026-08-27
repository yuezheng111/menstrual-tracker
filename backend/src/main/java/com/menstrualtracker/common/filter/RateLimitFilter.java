package com.menstrualtracker.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.util.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Redis distributed rate limiter for general API requests. Client IP is
 * resolved through ClientIpResolver so forwarding headers are not trusted
 * unless they come from a configured proxy.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final ClientIpResolver clientIpResolver;

    private static final String RATE_KEY_PREFIX = "menstrual:rate:ip:";
    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private static final long WINDOW_SECONDS = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = clientIpResolver.resolve(request);
        String rateKey = RATE_KEY_PREFIX + ip;

        Long count = cacheService.increment(rateKey);
        if (count != null) {
            if (count == 1) {
                cacheService.expire(rateKey, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (count > MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {}, count: {}, URI: {}", ip, count, request.getRequestURI());
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(429);
                ApiResponse<Void> apiResponse = ApiResponse.error(429, "Too many requests, please try again later");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
