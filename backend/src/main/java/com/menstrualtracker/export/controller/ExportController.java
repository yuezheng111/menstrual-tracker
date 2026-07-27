package com.menstrualtracker.export.controller;
import com.menstrualtracker.export.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Data Export")
public class ExportController {
    private final ExportService exportService;
    @GetMapping("/csv")
    @Operation(summary = "Export records as CSV")
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal Long userId) {
        byte[] data = exportService.exportToCsv(userId);
        String filename = "menstrual_records_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}