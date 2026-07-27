package com.menstrualtracker.statistics.dto;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor; import lombok.Builder; import lombok.Data; import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StatisticsOverviewDTO {
    private Integer totalCycles; private Integer totalRecords;
    private Double avgCycleLength; private Double avgPeriodLength;
    private Integer minCycleLength; private Integer maxCycleLength;
    private Integer minPeriodLength; private Integer maxPeriodLength;
    private LocalDate lastPeriodStart; private LocalDate lastPeriodEnd;
    private LocalDate nextPredictedStart; private LocalDate nextPredictedEnd;
    private LocalDate ovulationStart; private LocalDate ovulationEnd;
    private LocalDate fertileWindowStart; private LocalDate fertileWindowEnd;
    private LocalDate safePeriodStart; private LocalDate safePeriodEnd;
    private List<CycleHistoryDTO> recentCycles;
    private List<SymptomFrequencyDTO> topSymptoms;
}
