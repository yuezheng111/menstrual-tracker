package com.menstrualtracker.admin.controller;

import com.menstrualtracker.admin.dto.AdminLoginRequest;
import com.menstrualtracker.admin.dto.AdminLoginResponse;
import com.menstrualtracker.admin.dto.AdminChangePasswordRequest;
import com.menstrualtracker.admin.service.AdminAuthService;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.util.ClientIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Auth", description = "Admin console login")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final ClientIpResolver clientIpResolver;

    @PostMapping("/login")
    @Operation(summary = "Admin login (only ADMIN role accounts can log in)")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                                 HttpServletRequest servletRequest) {
        String clientIp = clientIpResolver.resolve(servletRequest);
        return ApiResponse.success("Admin login successful", adminAuthService.login(request, clientIp));
    }

    @PutMapping("/password/change")
    @Operation(summary = "Change current admin password")
    public ApiResponse<AdminLoginResponse> changePassword(@AuthenticationPrincipal Long userId,
                                                          @Valid @RequestBody AdminChangePasswordRequest request) {
        return ApiResponse.success("Password changed", adminAuthService.changePassword(userId, request));
    }
}
