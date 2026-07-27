package com.menstrualtracker.record.controller;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.record.dto.*;
import com.menstrualtracker.record.service.MenstrualRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Menstrual Records", description = "Track menstrual cycle records")
public class MenstrualRecordController {
    private final MenstrualRecordService recordService;
    @GetMapping
    @Operation(summary = "Get menstrual records with pagination")
    public ApiResponse<Page<RecordDTO>> getRecords(@AuthenticationPrincipal Long userId, RecordPageQuery query) { return recordService.getRecords(userId, query); }
    @GetMapping("/{id}")
    @Operation(summary = "Get a single record by ID")
    public ApiResponse<RecordDTO> getRecord(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return recordService.getRecord(userId, id); }
    @PostMapping
    @Operation(summary = "Create a new menstrual record")
    public ApiResponse<RecordDTO> createRecord(@AuthenticationPrincipal Long userId, @Valid @RequestBody RecordCreateRequest request) { return recordService.createRecord(userId, request); }
    @PutMapping("/{id}")
    @Operation(summary = "Update a menstrual record")
    public ApiResponse<RecordDTO> updateRecord(@AuthenticationPrincipal Long userId, @PathVariable Long id, @Valid @RequestBody RecordUpdateRequest request) { return recordService.updateRecord(userId, id, request); }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a menstrual record")
    public ApiResponse<Void> deleteRecord(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return recordService.deleteRecord(userId, id); }
}