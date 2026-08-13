package com.menstrualtracker.admin.controller;

import com.menstrualtracker.admin.service.AdminExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
@Tag(name = "Admin Export")
public class AdminExportController {

    private final AdminExportService adminExportService;

    @GetMapping(value = "/users", produces = "text/csv")
    @Operation(summary = "Export all users as CSV")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] data = adminExportService.exportUsersCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @GetMapping(value = "/records", produces = "text/csv")
    @Operation(summary = "Export all menstrual records as CSV")
    public ResponseEntity<byte[]> exportRecords() {
        byte[] data = adminExportService.exportRecordsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=records.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
