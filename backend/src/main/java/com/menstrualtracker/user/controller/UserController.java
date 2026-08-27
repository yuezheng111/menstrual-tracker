package com.menstrualtracker.user.controller;

import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.cache.RegisterRateLimiter;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.common.util.ClientIpResolver;
import com.menstrualtracker.user.dto.*;
import com.menstrualtracker.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class UserController {

    private final UserService userService;
    private final RegisterRateLimiter registerRateLimiter;
    private final ClientIpResolver clientIpResolver;

    @PostMapping("/register")
    @Operation(summary = "Register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                               HttpServletRequest servletRequest) {
        String clientIp = clientIpResolver.resolve(servletRequest);
        if (!registerRateLimiter.isAllowed(clientIp)) {
            throw BusinessException.tooManyRequests("Too many registration attempts, please try again later");
        }
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest servletRequest) {
        String clientIp = clientIpResolver.resolve(servletRequest);
        return userService.login(request, clientIp);
    }

    @PostMapping("/wx-login")
    @Operation(summary = "WeChat mini program login")
    public ApiResponse<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request,
                                              HttpServletRequest servletRequest) {
        String clientIp = clientIpResolver.resolve(servletRequest);
        return userService.wxLogin(request.getCode(), clientIp);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using WeChat code")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody WxLoginRequest request,
                                                   @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                   HttpServletRequest servletRequest) {
        String clientIp = clientIpResolver.resolve(servletRequest);
        // 支持带旧token的刷新请求
        return userService.refreshToken(request.getCode(), authHeader, clientIp);
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

}
