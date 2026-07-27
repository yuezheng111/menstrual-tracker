package com.menstrualtracker.record.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "menstrual_records")
@SQLDelete(sql = "UPDATE menstrual_records SET deleted = 1 WHERE id = ?")
@SQLRestriction("deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenstrualRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 10)
    private String flow;

    @Column(name = "pain_level", length = 10)
    private String painLevel;

    @Column(length = 20)
    private String color;

    @Builder.Default
    private Boolean clots = false;

    @Column(columnDefinition = "JSON")
    private String symptoms;

    @Column(name = "mood_tags", columnDefinition = "JSON")
    private String moodTags;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cycle_day")
    private Integer cycleDay;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
