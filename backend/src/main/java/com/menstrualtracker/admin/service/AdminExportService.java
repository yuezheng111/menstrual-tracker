package com.menstrualtracker.admin.service;

import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.record.entity.MenstrualRecord;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminExportService {

    private final UserRepository userRepository;
    private final MenstrualRecordRepository recordRepository;

    public byte[] exportUsersCsv() {
        List<User> users = userRepository.findAll();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Username", "Role", "Enabled", "Created At"))) {
            for (User u : users) {
                printer.printRecord(u.getId(), u.getUsername(),
                        u.getRole(), u.getEnabled(), u.getCreatedAt());
            }
            printer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw BusinessException.badRequest("Failed to export users: " + e.getMessage());
        }
    }

    public byte[] exportRecordsCsv() {
        List<MenstrualRecord> records = recordRepository.findAll();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "User ID", "Start Date", "End Date", "Flow", "Pain Level",
                             "Color", "Clots", "Symptoms", "Mood Tags", "Cycle Day", "Notes"))) {
            for (MenstrualRecord r : records) {
                printer.printRecord(r.getId(), r.getUserId(), r.getStartDate(), r.getEndDate(), r.getFlow(),
                        r.getPainLevel(), r.getColor(), r.getClots(), r.getSymptoms(), r.getMoodTags(),
                        r.getCycleDay(), r.getNotes());
            }
            printer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw BusinessException.badRequest("Failed to export records: " + e.getMessage());
        }
    }
}
