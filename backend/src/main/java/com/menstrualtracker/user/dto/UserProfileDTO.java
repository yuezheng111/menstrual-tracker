package com.menstrualtracker.user.dto;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserProfileDTO {
    private Long id; private String username; private String phone; private String email;
    private String avatar; private LocalDate birthDate; private Integer menarcheAge;
    private Integer avgCycleDays; private Integer avgPeriodDays;
}