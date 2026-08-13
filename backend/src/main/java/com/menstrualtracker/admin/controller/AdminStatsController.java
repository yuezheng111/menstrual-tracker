package com.menstrualtracker.admin.controller;

import com.menstrualtracker.admin.dto.AdminStatsDTO;
import com.menstrualtracker.admin.service.AdminStatsService;
import com.menstrualtracker.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    @Operation(summary = "Platform-wide statistics overview")
    public ApiResponse<AdminStatsDTO> overview() {
        return ApiResponse.success(adminStatsService.getOverview());
    }
}
