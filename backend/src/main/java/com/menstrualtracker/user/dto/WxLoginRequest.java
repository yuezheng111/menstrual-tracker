package com.menstrualtracker.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginRequest {
    @NotBlank(message = "Code is required")
    private String code;
}
