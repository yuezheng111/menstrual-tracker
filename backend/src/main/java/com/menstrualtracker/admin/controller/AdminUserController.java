package com.menstrualtracker.admin.controller;

import com.menstrualtracker.admin.dto.AdminUserDTO;
import com.menstrualtracker.admin.dto.AdminUserStatusRequest;
import com.menstrualtracker.admin.dto.AdminPasswordResetRequest;
import com.menstrualtracker.admin.service.AdminUserService;
import com.menstrualtracker.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List users (paginated, optional keyword search)")
    public ApiResponse<Page<AdminUserDTO>> listUsers(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminUserService.listUsers(page, size, keyword));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user")
    public ApiResponse<AdminUserDTO> setStatus(@PathVariable Long id,
                                               @Valid @RequestBody AdminUserStatusRequest request) {
        return ApiResponse.success(adminUserService.setEnabled(id, request.getEnabled()));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Reset a user password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody AdminPasswordResetRequest request) {
        adminUserService.resetPassword(id, request.getNewPassword());
        return ApiResponse.success("Password reset", null);
    }
}
