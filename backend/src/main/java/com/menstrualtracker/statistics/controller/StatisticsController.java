package com.menstrualtracker.statistics.controller;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.statistics.dto.StatisticsOverviewDTO;
import com.menstrualtracker.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;
    @GetMapping("/overview")
    @Operation(summary = "Get statistics overview")
    public ApiResponse<StatisticsOverviewDTO> getOverview(@AuthenticationPrincipal Long userId) { return statisticsService.getOverview(userId); }
}