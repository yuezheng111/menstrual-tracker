package com.menstrualtracker.prediction.controller;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.prediction.dto.PredictionDTO;
import com.menstrualtracker.prediction.dto.ReminderDTO;
import com.menstrualtracker.prediction.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Tag(name = "Cycle Predictions")
public class PredictionController {
    private final PredictionService predictionService;
    @GetMapping
    @Operation(summary = "Get cycle prediction")
    public ApiResponse<PredictionDTO> getPrediction(@AuthenticationPrincipal Long userId) { return predictionService.getPrediction(userId); }
    @GetMapping("/reminders")
    @Operation(summary = "Get upcoming reminders")
    public ApiResponse<List<ReminderDTO>> getReminders(@AuthenticationPrincipal Long userId, @RequestParam(defaultValue = "7") int advanceDays) {
        return predictionService.getReminders(userId, advanceDays);
    }
}