package com.menstrualtracker.prediction.dto;
import java.time.LocalDate;
import lombok.AllArgsConstructor; import lombok.Builder; import lombok.Data; import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PredictionDTO {
    private LocalDate nextPeriodStart; private LocalDate nextPeriodEnd;
    private Integer predictedCycleLength; private Integer predictedPeriodLength;
    private LocalDate ovulationStart; private LocalDate ovulationEnd;
    private LocalDate fertileWindowStart; private LocalDate fertileWindowEnd;
    private LocalDate safePeriodStart; private LocalDate safePeriodEnd;
    private String confidence;
}