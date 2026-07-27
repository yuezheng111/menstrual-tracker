package com.menstrualtracker.record.dto;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
@Data
public class RecordPageQuery {
    @DateTimeFormat(pattern = "yyyy-MM")
    private String month;
    private Integer year;
    private int page = 1;
    private int size = 20;
}