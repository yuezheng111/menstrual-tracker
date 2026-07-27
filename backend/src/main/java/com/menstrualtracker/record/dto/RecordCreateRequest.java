package com.menstrualtracker.record.dto;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class RecordCreateRequest {
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    private LocalDate endDate;
    private String flow; private String painLevel; private String color;
    private Boolean clots; private List<String> symptoms; private List<String> moodTags;
    private String notes;
}