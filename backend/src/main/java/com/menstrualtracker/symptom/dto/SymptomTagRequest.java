package com.menstrualtracker.symptom.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class SymptomTagRequest {
    @NotBlank(message = "Tag name is required")
    private String name;
    @NotBlank(message = "Tag type is required")
    private String type;
    private String color; private String icon; private Integer sortOrder;
}