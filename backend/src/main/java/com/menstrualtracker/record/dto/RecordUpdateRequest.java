package com.menstrualtracker.record.dto;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
@Data
public class RecordUpdateRequest {
    private LocalDate startDate; private LocalDate endDate;
    private String flow; private String painLevel; private String color;
    private Boolean clots; private List<String> symptoms; private List<String> moodTags;
    private String notes;

    @AssertTrue(message = "End date cannot be before start date")
    public boolean isEndDateValid() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }
}
