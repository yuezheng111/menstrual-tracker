package com.menstrualtracker.admin.service;

import com.menstrualtracker.admin.dto.AdminChangePasswordRequest;
import com.menstrualtracker.admin.dto.AdminLoginRequest;
import com.menstrualtracker.admin.dto.AdminLoginResponse;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.cache.LoginRateLimiter;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.common.util.JwtUtil;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CacheService cacheService;
    private final LoginRateLimiter loginRateLimiter;

    private static final String TOKEN_CACHE_KEY = "menstrual:token:%s";
    private static final long TOKEN_CACHE_TTL = 7;

    public AdminLoginResponse login(AdminLoginRequest request, String clientIp) {
        String username = request.getUsername();
        if (loginRateLimiter.isBlocked(clientIp, username)) {
            throw BusinessException.tooManyRequests("Too many login attempts, please try again later");
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginRateLimiter.onFailure(clientIp, username);
            throw BusinessException.unauthorized("Invalid username or password");
        }

        if (!"ADMIN".equals(user.getRole()) || !Boolean.TRUE.equals(user.getEnabled())) {
            loginRateLimiter.onFailure(clientIp, username);
            log.warn("Admin login rejected for username={}, reason={}",
                    username, !Boolean.TRUE.equals(user.getEnabled()) ? "disabled" : "not-admin");
            throw BusinessException.unauthorized("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "ADMIN");
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(), TOKEN_CACHE_TTL, TimeUnit.DAYS);
        loginRateLimiter.onSuccess(clientIp, username);

        log.info("Admin login successful: username={}", user.getUsername());
        return AdminLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .username(user.getUsername())
                .role(user.getRole())
                .mustChangePassword(Boolean.TRUE.equals(user.getPasswordChangeRequired()))
                .build();
    }

    @Transactional
    public AdminLoginResponse changePassword(Long userId, AdminChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("Invalid username or password");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw BusinessException.badRequest("New password must be different from the old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
        cacheService.deleteUserSessions(userId);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "ADMIN");
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(), TOKEN_CACHE_TTL, TimeUnit.DAYS);
        return AdminLoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .username(user.getUsername())
                .role(user.getRole())
                .mustChangePassword(false)
                .build();
    }
}
