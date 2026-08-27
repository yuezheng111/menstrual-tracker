package com.menstrualtracker.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.cache.LoginRateLimiter;
import com.menstrualtracker.common.cache.TokenBlacklistCache;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.common.util.JwtUtil;
import com.menstrualtracker.user.dto.*;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CacheService cacheService;
    private final LoginRateLimiter loginRateLimiter;
    private final TokenBlacklistCache tokenBlacklistCache;
    private final RestTemplate restTemplate;

    @Value("${wechat.app-id}")
    private String wechatAppId;

    @Value("${wechat.app-secret}")
    private String wechatAppSecret;

    // Redis-backed session map makes logout effective before the JWT expires.
    private static final String TOKEN_CACHE_KEY = "menstrual:token:%s";
    private static final long TOKEN_CACHE_TTL = 7;

    private static final String WECHAT_TOKEN_KEY = "menstrual:wechat:access_token";
    private static final long WECHAT_TOKEN_TTL = 7_000;

    @Transactional
    public ApiResponse<LoginResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw BusinessException.conflict("Username already exists");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .loginType("PASSWORD")
                .build();
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(),
                TOKEN_CACHE_TTL, TimeUnit.DAYS);
        return ApiResponse.success("Registration successful", buildLoginResponse(user, token));
    }

    public ApiResponse<LoginResponse> login(LoginRequest request, String clientIp) {
        String username = request.getUsername();
        if (loginRateLimiter.isBlocked(clientIp, username)) {
            throw BusinessException.tooManyRequests("Too many login attempts, please try again later");
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginRateLimiter.onFailure(clientIp, username);
            throw BusinessException.unauthorized("Invalid username or password");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            loginRateLimiter.onFailure(clientIp, username);
            throw BusinessException.forbidden("Account has been disabled");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(),
                TOKEN_CACHE_TTL, TimeUnit.DAYS);
        loginRateLimiter.onSuccess(clientIp, username);
        return ApiResponse.success("Login successful", buildLoginResponse(user, token));
    }

    public ApiResponse<Void> logout(String token) {
        if (token != null && !token.isEmpty()) {
            cacheService.delete(String.format(TOKEN_CACHE_KEY, token));
            tokenBlacklistCache.add(token);
            log.info("User logged out; session removed from Redis and blacklisted locally");
        }
        return ApiResponse.success("Logged out", null);
    }

    public ApiResponse<LoginResponse> wxLogin(String code, String clientIp) {
        if (loginRateLimiter.isBlocked(clientIp, "wx-login")) {
            throw BusinessException.tooManyRequests("Too many login attempts, please try again later");
        }

        String openId;
        if (wechatAppId.startsWith("test_")) {
            openId = "mock_fixed_user";
        } else {
            openId = exchangeCodeForOpenId(code);
            if (openId == null) {
                loginRateLimiter.onFailure(clientIp, "wx-login");
                throw BusinessException.badRequest("WeChat login failed");
            }
        }

        User user = userRepository.findByOpenId(openId).orElse(null);
        if (user == null) {
            user = User.builder()
                    .username(generateWxUsername(openId))
                    .nickname("微信用户")
                    .openId(openId)
                    .password(passwordEncoder.encode(openId))
                    .loginType("WECHAT")
                    .build();
            user = userRepository.save(user);
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            loginRateLimiter.onFailure(clientIp, "wx-login");
            throw BusinessException.forbidden("Account has been disabled");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        cacheService.set(String.format(TOKEN_CACHE_KEY, token), user.getId(),
                TOKEN_CACHE_TTL, TimeUnit.DAYS);
        loginRateLimiter.onSuccess(clientIp, "wx-login");
        return ApiResponse.success("Login successful", buildLoginResponse(user, token));
    }

    public ApiResponse<UserProfileDTO> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        return ApiResponse.success(toProfileDTO(user));
    }

    @Transactional
    public ApiResponse<UserProfileDTO> updateProfile(Long userId, UserProfileDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getNickname() != null) {
            String nickname = request.getNickname().trim();
            if (nickname.isEmpty()) {
                throw BusinessException.badRequest("Nickname cannot be empty");
            }
            if (nickname.length() > 30) {
                throw BusinessException.badRequest("Nickname must be 1-30 characters");
            }
            if (nickname.matches(".*[\\x00-\\x1F\\x7F].*") || nickname.contains("<") || nickname.contains(">")) {
                throw BusinessException.badRequest("Nickname contains invalid characters");
            }
            user.setNickname(nickname);
        }
        user = userRepository.save(user);
        return ApiResponse.success("Profile updated", toProfileDTO(user));
    }

    public String getWechatAccessToken() {
        Object cached = cacheService.get(WECHAT_TOKEN_KEY);
        if (cached instanceof String accessToken && !accessToken.isEmpty()) {
            return accessToken;
        }

        URI uri = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/cgi-bin/token")
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", wechatAppId)
                .queryParam("secret", wechatAppSecret)
                .build()
                .encode()
                .toUri();

        try {
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            if (response != null && response.hasNonNull("access_token")) {
                String accessToken = response.get("access_token").asText();
                cacheService.set(WECHAT_TOKEN_KEY, accessToken, WECHAT_TOKEN_TTL, TimeUnit.SECONDS);
                log.info("WeChat access_token refreshed and cached");
                return accessToken;
            }
            logWechatError("access_token", response);
        } catch (Exception e) {
            log.error("WeChat access_token request failed", e);
        }
        return null;
    }

    private String exchangeCodeForOpenId(String code) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", wechatAppId)
                .queryParam("secret", wechatAppSecret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .encode()
                .toUri();

        try {
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            if (response != null && response.hasNonNull("openid")) {
                return response.get("openid").asText();
            }
            logWechatError("jscode2session", response);
        } catch (Exception e) {
            log.error("WeChat jscode2session request failed", e);
        }
        return null;
    }

    private void logWechatError(String operation, JsonNode response) {
        int errorCode = response != null && response.hasNonNull("errcode")
                ? response.get("errcode").asInt() : -1;
        String errorMessage = response != null && response.hasNonNull("errmsg")
                ? response.get("errmsg").asText() : "unknown response";
        log.error("WeChat {} failed: code={}, message={}", operation, errorCode, errorMessage);
    }

    private String generateWxUsername(String openId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(openId.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("wx_");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to hash openId; falling back to hashCode", e);
            return "wx_" + Integer.toHexString(openId.hashCode());
        }
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token).tokenType("Bearer").expiresIn(86_400_000L)
                .user(toProfileDTO(user))
                .build();
    }

    private UserProfileDTO toProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId()).username(user.getUsername())
                .nickname(user.getNickname()).avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }
}
