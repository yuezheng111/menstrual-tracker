package com.menstrualtracker.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private Long totalUsers;
    private Long newUsersThisMonth;
    private Long totalRecords;
    private Long todayRecords;
    private Long activeUsers7d;
    private List<UserGrowthPoint> userGrowth;
    private List<SymptomStatDTO> topSymptoms;
}
