package com.menstrualtracker.record.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RecordCreateRequest {
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    private LocalDate endDate;

    @Pattern(regexp = "LIGHT|MEDIUM|HEAVY|", message = "Flow must be LIGHT, MEDIUM, or HEAVY")
    private String flow;

    @Pattern(regexp = "NONE|MILD|MODERATE|SEVERE|", message = "Pain level must be NONE, MILD, MODERATE, or SEVERE")
    private String painLevel;

    @Size(max = 20, message = "Color is too long")
    private String color;
    private Boolean clots;

    @Size(max = 20, message = "At most 20 symptom tags")
    private List<@Size(max = 30, message = "Symptom tag is too long") String> symptoms;
    @Size(max = 20, message = "At most 20 mood tags")
    private List<@Size(max = 30, message = "Mood tag is too long") String> moodTags;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;

    @AssertTrue(message = "End date cannot be before start date")
    public boolean isEndDateValid() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }
}
