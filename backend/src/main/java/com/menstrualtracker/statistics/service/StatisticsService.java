package com.menstrualtracker.statistics.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.common.util.CycleCalculator;
import com.menstrualtracker.record.entity.MenstrualRecord;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import com.menstrualtracker.statistics.dto.CycleHistoryDTO;
import com.menstrualtracker.statistics.dto.StatisticsOverviewDTO;
import com.menstrualtracker.statistics.dto.SymptomFrequencyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final MenstrualRecordRepository recordRepository;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    private static final String OVERVIEW_CACHE_KEY = "menstrual:user:%d:overview";
    private static final long OVERVIEW_CACHE_TTL = 30; // 分钟

    /**
     * 获取统计概览 — 优先从 Redis 缓存读取，未命中则计算并写入缓存（30分钟过期）。
     */
    public ApiResponse<StatisticsOverviewDTO> getOverview(Long userId) {
        String cacheKey = String.format(OVERVIEW_CACHE_KEY, userId);

        // 1. 查 Redis 缓存
        Object cached = cacheService.get(cacheKey);
        if (cached instanceof StatisticsOverviewDTO) {
            return ApiResponse.success((StatisticsOverviewDTO) cached);
        }

        // 2. 缓存未命中，计算统计
        List<MenstrualRecord> records = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId);
        if (records.isEmpty()) return ApiResponse.success(StatisticsOverviewDTO.builder().totalCycles(0).totalRecords(0).build());
        List<MenstrualRecord> chronological = new ArrayList<>(records);
        chronological.sort(Comparator.comparing(MenstrualRecord::getStartDate));
        List<Integer> cycleLengths = new ArrayList<>();
        List<Integer> periodLengths = new ArrayList<>();
        Map<String, Long> symptomCount = new HashMap<>();
        for (int i = 1; i < chronological.size(); i++) {
            cycleLengths.add(CycleCalculator.calculateCycleLength(chronological.get(i-1).getStartDate(), chronological.get(i).getStartDate()));
        }
        for (MenstrualRecord r : chronological) {
            if (r.getStartDate() != null && r.getEndDate() != null)
                periodLengths.add(CycleCalculator.calculatePeriodLength(r.getStartDate(), r.getEndDate()));
            countSymptoms(symptomCount, r.getSymptoms());
        }
        MenstrualRecord last = chronological.get(chronological.size() - 1);
        int avgCycle = 28;
        int avgPeriod = 5;
        CycleCalculator.PredictionResult prediction = CycleCalculator.predict(last.getStartDate(), avgCycle, avgPeriod);
        List<CycleHistoryDTO> recentCycles = new ArrayList<>();
        int startIdx = Math.max(0, chronological.size() - 6);
        for (int i = startIdx; i < chronological.size(); i++) {
            MenstrualRecord r = chronological.get(i);
            Integer cycleLen = null;
            for (int j = i; j >= 1; j--) {
                if (chronological.get(j).getId().equals(r.getId())) {
                    cycleLen = CycleCalculator.calculateCycleLength(chronological.get(j-1).getStartDate(), r.getStartDate());
                    break;
                }
            }
            int periodLen = (r.getEndDate() != null) ? CycleCalculator.calculatePeriodLength(r.getStartDate(), r.getEndDate()) : 0;
            recentCycles.add(CycleHistoryDTO.builder()
                    .startDate(r.getStartDate()).endDate(r.getEndDate()).cycleLength(cycleLen).periodLength(periodLen)
                    .flow(r.getFlow()).painLevel(r.getPainLevel()).build());
        }
        Collections.reverse(recentCycles);
        List<SymptomFrequencyDTO> topSymptoms = symptomCount.entrySet().stream()
                .map(e -> SymptomFrequencyDTO.builder().name(e.getKey()).count(e.getValue())
                        .percentage(Math.round((double)e.getValue()/records.size()*10000)/100.0).build())
                .sorted((a,b) -> Long.compare(b.getCount(), a.getCount())).limit(10).collect(Collectors.toList());
        StatisticsOverviewDTO overview = StatisticsOverviewDTO.builder()
                .totalCycles(cycleLengths.size()).totalRecords(records.size())
                .avgCycleLength(cycleLengths.stream().mapToInt(Integer::intValue).average().orElse(0))
                .avgPeriodLength(periodLengths.stream().mapToInt(Integer::intValue).average().orElse(0))
                .minCycleLength(cycleLengths.stream().mapToInt(Integer::intValue).min().orElse(0))
                .maxCycleLength(cycleLengths.stream().mapToInt(Integer::intValue).max().orElse(0))
                .minPeriodLength(periodLengths.stream().mapToInt(Integer::intValue).min().orElse(0))
                .maxPeriodLength(periodLengths.stream().mapToInt(Integer::intValue).max().orElse(0))
                .lastPeriodStart(last.getStartDate())
                .lastPeriodEnd(last.getEndDate())
                .nextPredictedStart(prediction != null ? prediction.getNextPeriodStart() : null)
                .nextPredictedEnd(prediction != null ? prediction.getNextPeriodEnd() : null)
                .ovulationStart(prediction != null ? prediction.getOvulationStart() : null)
                .ovulationEnd(prediction != null ? prediction.getOvulationEnd() : null)
                .fertileWindowStart(prediction != null ? prediction.getFertileWindowStart() : null)
                .fertileWindowEnd(prediction != null ? prediction.getFertileWindowEnd() : null)
                .safePeriodStart(prediction != null ? prediction.getSafePeriodStart() : null)
                .safePeriodEnd(prediction != null ? prediction.getSafePeriodEnd() : null)
                .recentCycles(recentCycles).topSymptoms(topSymptoms).build();

        // 3. 写入 Redis 缓存（30分钟过期）
        cacheService.set(cacheKey, overview, OVERVIEW_CACHE_TTL, TimeUnit.MINUTES);

        return ApiResponse.success(overview);
    }
    private void countSymptoms(Map<String, Long> map, String symptomsJson) {
        if (symptomsJson == null || symptomsJson.isEmpty()) return;
        try {
            List<String> symptoms = objectMapper.readValue(symptomsJson, new TypeReference<List<String>>() {});
            for (String s : symptoms) map.merge(s, 1L, Long::sum);
        } catch (Exception ignored) {}
    }
}
