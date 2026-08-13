package com.menstrualtracker.admin.controller;

import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.admin.service.AdminTagService;
import com.menstrualtracker.symptom.entity.SymptomTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@Tag(name = "Admin Tags")
public class AdminTagController {

    private final AdminTagService adminTagService;

    @GetMapping
    @Operation(summary = "List all tags platform-wide (optional type filter)")
    public ApiResponse<List<SymptomTag>> listTags(@RequestParam(required = false) String type) {
        return ApiResponse.success(adminTagService.listAll(type));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        adminTagService.delete(id);
        return ApiResponse.success("Tag deleted", null);
    }
}
