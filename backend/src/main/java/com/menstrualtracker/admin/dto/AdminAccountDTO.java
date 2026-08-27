package com.menstrualtracker.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountDTO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private Boolean enabled;
    private Boolean passwordChangeRequired;
    private LocalDateTime createdAt;
}
