package com.menstrualtracker.record.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RecordDTO {
    private Long id; private Long userId; private LocalDate startDate; private LocalDate endDate;
    private Integer cycleDay; private String flow; private String painLevel; private String color;
    private Boolean clots; private List<String> symptoms; private List<String> moodTags;
    private String notes; private LocalDateTime createdAt;
}