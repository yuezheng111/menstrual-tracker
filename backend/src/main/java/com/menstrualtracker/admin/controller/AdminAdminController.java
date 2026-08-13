package com.menstrualtracker.admin.controller;

import com.menstrualtracker.admin.service.AdminAdminService;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
@Tag(name = "Admin Management")
public class AdminAdminController {

    private final AdminAdminService adminAdminService;

    @GetMapping
    @Operation(summary = "List all admin accounts")
    public ApiResponse<List<User>> listAdmins() {
        return ApiResponse.success(adminAdminService.listAdmins());
    }

    @PutMapping("/{id}/grant")
    @Operation(summary = "Grant admin role to a user")
    public ApiResponse<User> grantAdmin(@PathVariable Long id) {
        return ApiResponse.success("Admin role granted", adminAdminService.grantAdmin(id));
    }

    @PutMapping("/{id}/revoke")
    @Operation(summary = "Revoke admin role from a user")
    public ApiResponse<User> revokeAdmin(@PathVariable Long id,
                                         @AuthenticationPrincipal Long operatorId) {
        return ApiResponse.success("Admin role revoked", adminAdminService.revokeAdmin(id, operatorId));
    }
}
