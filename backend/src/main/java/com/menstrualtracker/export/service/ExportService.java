package com.menstrualtracker.export.service;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.record.entity.MenstrualRecord;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ExportService {
    private final MenstrualRecordRepository recordRepository;
    public byte[] exportToCsv(Long userId) {
        List<MenstrualRecord> records = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID","Start Date","End Date","Flow","Pain Level","Color","Clots","Symptoms","Mood Tags","Cycle Day","Notes"))) {
            for (MenstrualRecord r : records) {
                printer.printRecord(r.getId(),r.getStartDate(),r.getEndDate(),r.getFlow(),r.getPainLevel(),r.getColor(),
                        r.getClots(),r.getSymptoms(),r.getMoodTags(),r.getCycleDay(),r.getNotes());
            }
            printer.flush();
            return baos.toByteArray();
        } catch (Exception e) { throw BusinessException.badRequest("Failed to export data: " + e.getMessage()); }
    }
}