package com.menstrualtracker.prediction.dto;
import java.time.LocalDate;
import lombok.AllArgsConstructor; import lombok.Builder; import lombok.Data; import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReminderDTO {
    private LocalDate date; private String type; private String title;
    private String description; private Integer daysUntil;
}