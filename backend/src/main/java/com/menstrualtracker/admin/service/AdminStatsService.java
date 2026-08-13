package com.menstrualtracker.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstrualtracker.admin.dto.AdminStatsDTO;
import com.menstrualtracker.admin.dto.SymptomStatDTO;
import com.menstrualtracker.admin.dto.UserGrowthPoint;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final MenstrualRecordRepository recordRepository;
    private final ObjectMapper objectMapper;

    public AdminStatsDTO getOverview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = YearMonth.from(now.toLocalDate()).atDay(1).atStartOfDay();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long totalUsers = userRepository.count();
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(monthStart);
        long totalRecords = recordRepository.count();
        long todayRecords = recordRepository.countByCreatedAtAfter(todayStart);
        long activeUsers7d = recordRepository.countActiveUsersSince(weekAgo);

        List<UserGrowthPoint> userGrowth = buildUserGrowth(thirtyDaysAgo);
        List<SymptomStatDTO> topSymptoms = buildTopSymptoms(recordRepository.findTopSymptoms(10));

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalRecords(totalRecords)
                .todayRecords(todayRecords)
                .activeUsers7d(activeUsers7d)
                .userGrowth(userGrowth)
                .topSymptoms(topSymptoms)
                .build();
    }

    private List<UserGrowthPoint> buildUserGrowth(LocalDateTime since) {
        List<Object[]> rows = userRepository.countUsersByDay(since);
        Map<LocalDate, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] instanceof java.sql.Date d) {
                map.put(d.toLocalDate(), ((Number) row[1]).longValue());
            }
        }
        List<UserGrowthPoint> result = new ArrayList<>();
        LocalDate start = since.toLocalDate();
        LocalDate today = LocalDate.now();
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            result.add(new UserGrowthPoint(d, map.getOrDefault(d, 0L)));
        }
        return result;
    }

    private List<SymptomStatDTO> buildTopSymptoms(List<Object[]> rows) {
        Map<String, Long> counter = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null) continue;
            String symptomsJson = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            try {
                List<String> symptoms = objectMapper.readValue(symptomsJson,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                for (String s : symptoms) {
                    counter.merge(s, count, Long::sum);
                }
            } catch (Exception ignored) {
                // 非 JSON 格式，忽略
            }
        }
        return counter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new SymptomStatDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
