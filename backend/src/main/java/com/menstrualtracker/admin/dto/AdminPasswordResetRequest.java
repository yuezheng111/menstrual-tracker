package com.menstrualtracker.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPasswordResetRequest {
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64, message = "Password must be 8-64 characters")
    private String newPassword;
}
