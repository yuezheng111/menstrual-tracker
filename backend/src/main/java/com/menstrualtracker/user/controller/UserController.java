package com.menstrualtracker.user.controller;

import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.user.dto.*;
import com.menstrualtracker.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
                                            @RequestHeader(value = "X-Real-IP", required = false) String xRealIp,
                                            HttpServletRequest servletRequest) {
        String clientIp = resolveClientIp(xForwardedFor, xRealIp, servletRequest);
        return userService.login(request, clientIp);
    }

    @PostMapping("/wx-login")
    @Operation(summary = "WeChat mini program login")
    public ApiResponse<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request,
                                              @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
                                              @RequestHeader(value = "X-Real-IP", required = false) String xRealIp,
                                              HttpServletRequest servletRequest) {
        String clientIp = resolveClientIp(xForwardedFor, xRealIp, servletRequest);
        return userService.wxLogin(request.getCode(), clientIp);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get profile")
    public ApiResponse<UserProfileDTO> getProfile(@AuthenticationPrincipal Long userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update profile")
    public ApiResponse<UserProfileDTO> updateProfile(@AuthenticationPrincipal Long userId,
                                                     @Valid @RequestBody UserProfileDTO request) {
        return userService.updateProfile(userId, request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return userService.logout(token);
    }

    private String resolveClientIp(String xForwardedFor, String xRealIp, HttpServletRequest request) {
        String ip = xForwardedFor;
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = xRealIp;
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}