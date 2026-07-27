package com.menstrualtracker.symptom.controller;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.symptom.dto.SymptomTagDTO;
import com.menstrualtracker.symptom.dto.SymptomTagRequest;
import com.menstrualtracker.symptom.service.SymptomTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Symptom Tags", description = "Custom symptom and emotion tag management")
public class SymptomTagController {
    private final SymptomTagService symptomTagService;
    @GetMapping
    @Operation(summary = "Get all tags for current user")
    public ApiResponse<List<SymptomTagDTO>> getTags(@AuthenticationPrincipal Long userId) { return symptomTagService.getTags(userId); }
    @GetMapping("/type/{type}")
    @Operation(summary = "Get tags by type (SYMPTOM or EMOTION)")
    public ApiResponse<List<SymptomTagDTO>> getTagsByType(@AuthenticationPrincipal Long userId, @PathVariable String type) { return symptomTagService.getTagsByType(userId, type); }
    @PostMapping
    @Operation(summary = "Create a new tag")
    public ApiResponse<SymptomTagDTO> createTag(@AuthenticationPrincipal Long userId, @Valid @RequestBody SymptomTagRequest request) { return symptomTagService.createTag(userId, request); }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag")
    public ApiResponse<Void> deleteTag(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return symptomTagService.deleteTag(userId, id); }
}