package com.menstrualtracker.prediction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cycle_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CyclePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "predicted_start_date", nullable = false)
    private LocalDate predictedStartDate;

    @Column(name = "predicted_end_date")
    private LocalDate predictedEndDate;

    @Column(name = "ovulation_start")
    private LocalDate ovulationStart;

    @Column(name = "ovulation_end")
    private LocalDate ovulationEnd;

    @Column(name = "fertile_window_start")
    private LocalDate fertileWindowStart;

    @Column(name = "fertile_window_end")
    private LocalDate fertileWindowEnd;

    @Column(name = "safe_period_start")
    private LocalDate safePeriodStart;

    @Column(name = "safe_period_end")
    private LocalDate safePeriodEnd;

    @Column(length = 20)
    @Builder.Default
    private String confidence = "MEDIUM";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
