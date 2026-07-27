package com.menstrualtracker.statistics.dto;
import java.time.LocalDate;
import lombok.AllArgsConstructor; import lombok.Builder; import lombok.Data; import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CycleHistoryDTO {
    private LocalDate startDate; private LocalDate endDate;
    private Integer cycleLength; private Integer periodLength;
    private String flow; private String painLevel;
}