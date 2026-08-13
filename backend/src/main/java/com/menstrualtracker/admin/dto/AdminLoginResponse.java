package com.menstrualtracker.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String role;
    private Boolean mustChangePassword;
}
