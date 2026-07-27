package com.menstrualtracker.symptom.dto;
import lombok.AllArgsConstructor; import lombok.Builder; import lombok.Data; import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SymptomTagDTO {
    private Long id; private Long userId; private String name; private String type;
    private String color; private String icon; private Integer sortOrder;
}