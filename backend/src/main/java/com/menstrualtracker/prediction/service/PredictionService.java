package com.menstrualtracker.prediction.service;

import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.common.util.CycleCalculator;
import com.menstrualtracker.prediction.dto.PredictionDTO;
import com.menstrualtracker.prediction.dto.ReminderDTO;
import com.menstrualtracker.prediction.entity.CyclePrediction;
import com.menstrualtracker.prediction.repository.CyclePredictionRepository;
import com.menstrualtracker.record.entity.MenstrualRecord;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final MenstrualRecordRepository recordRepository;
    private final CyclePredictionRepository predictionRepository;
    private final CacheService cacheService;

    private static final String PREDICT_CACHE_KEY = "menstrual:user:%d:predict";
    private static final long PREDICT_CACHE_TTL = 30; // minutes

    public ApiResponse<PredictionDTO> getPrediction(Long userId) {
        String cacheKey = String.format(PREDICT_CACHE_KEY, userId);

        Object cached = cacheService.get(cacheKey);
        if (cached instanceof PredictionDTO) {
            return ApiResponse.success((PredictionDTO) cached);
        }

        List<MenstrualRecord> records = getSortedRecords(userId);
        if (records.isEmpty()) {
            throw BusinessException.badRequest("No records found for prediction");
        }
        LocalDate lastStart = records.get(records.size() - 1).getStartDate();

        int avgCycle = calculateAverageCycleLength(records);
        int avgPeriod = calculateAveragePeriodLength(records);

        CycleCalculator.PredictionResult result =
                CycleCalculator.predict(lastStart, avgCycle, avgPeriod);
        if (result == null) {
            throw BusinessException.badRequest("Unable to generate prediction");
        }
        savePrediction(userId, result);

        PredictionDTO dto = toPredictionDTO(result);
        cacheService.set(cacheKey, dto, PREDICT_CACHE_TTL, TimeUnit.MINUTES);

        return ApiResponse.success(dto);
    }

    @Transactional
    public void savePrediction(Long userId, CycleCalculator.PredictionResult result) {
        CyclePrediction prediction = CyclePrediction.builder()
                .userId(userId)
                .predictedStartDate(result.getNextPeriodStart())
                .predictedEndDate(result.getNextPeriodEnd())
                .ovulationStart(result.getOvulationStart())
                .ovulationEnd(result.getOvulationEnd())
                .fertileWindowStart(result.getFertileWindowStart())
                .fertileWindowEnd(result.getFertileWindowEnd())
                .safePeriodStart(result.getSafePeriodStart())
                .safePeriodEnd(result.getSafePeriodEnd())
                .build();
        predictionRepository.save(prediction);
    }

    public ApiResponse<List<ReminderDTO>> getReminders(Long userId, int advanceDays) {
        List<MenstrualRecord> records = getSortedRecords(userId);
        if (records.isEmpty()) {
            return ApiResponse.success(List.of());
        }

        int avgCycle = calculateAverageCycleLength(records);
        int avgPeriod = calculateAveragePeriodLength(records);
        LocalDate lastStart = records.get(records.size() - 1).getStartDate();
        CycleCalculator.PredictionResult prediction =
                CycleCalculator.predict(lastStart, avgCycle, avgPeriod);
        LocalDate nextPeriod = prediction != null
                ? prediction.getNextPeriodStart()
                : lastStart.plusDays(avgCycle);

        List<CycleCalculator.Reminder> reminders =
                CycleCalculator.generateReminders(nextPeriod, advanceDays);
        List<ReminderDTO> dtos = reminders.stream()
                .map(r -> ReminderDTO.builder()
                        .date(r.getDate()).type(r.getType()).title(r.getTitle())
                        .description(r.getDescription()).daysUntil(r.getDaysUntil())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    // Repository returns DESC order; re-sort ascending so the last element is latest.
    private List<MenstrualRecord> getSortedRecords(Long userId) {
        return recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId)
                .stream()
                .sorted(Comparator.comparing(MenstrualRecord::getStartDate))
                .collect(Collectors.toList());
    }

    private int calculateAverageCycleLength(List<MenstrualRecord> chronological) {
        List<Integer> lengths = new ArrayList<>();
        for (int i = 1; i < chronological.size(); i++) {
            int len = CycleCalculator.calculateCycleLength(
                    chronological.get(i - 1).getStartDate(),
                    chronological.get(i).getStartDate());
            // Ignore implausible intervals so a missed entry or typo does not skew predictions.
            if (len >= 15 && len <= 60) {
                lengths.add(len);
            }
        }
        return (int) Math.round(lengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(28));
    }

    private int calculateAveragePeriodLength(List<MenstrualRecord> chronological) {
        List<Integer> lengths = chronological.stream()
                .filter(r -> r.getStartDate() != null && r.getEndDate() != null)
                .map(r -> CycleCalculator.calculatePeriodLength(
                        r.getStartDate(), r.getEndDate()))
                .filter(len -> len >= 1 && len <= 31)
                .collect(Collectors.toList());
        return (int) Math.round(lengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(5));
    }

    private PredictionDTO toPredictionDTO(CycleCalculator.PredictionResult result) {
        return PredictionDTO.builder()
                .nextPeriodStart(result.getNextPeriodStart())
                .nextPeriodEnd(result.getNextPeriodEnd())
                .predictedCycleLength(result.getPredictedCycleLength())
                .predictedPeriodLength(result.getPredictedPeriodLength())
                .ovulationStart(result.getOvulationStart())
                .ovulationEnd(result.getOvulationEnd())
                .fertileWindowStart(result.getFertileWindowStart())
                .fertileWindowEnd(result.getFertileWindowEnd())
                .safePeriodStart(result.getSafePeriodStart())
                .safePeriodEnd(result.getSafePeriodEnd())
                .confidence("MEDIUM")
                .build();
    }
}
