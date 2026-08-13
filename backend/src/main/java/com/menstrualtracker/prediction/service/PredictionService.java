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
    private static final long PREDICT_CACHE_TTL = 30; // 分钟

    /**
     * 获取周期预测 — 优先从 Redis 缓存读取，未命中则计算并写入缓存（30分钟过期）。
     */
    public ApiResponse<PredictionDTO> getPrediction(Long userId) {
        String cacheKey = String.format(PREDICT_CACHE_KEY, userId);

        // 1. 查 Redis 缓存
        Object cached = cacheService.get(cacheKey);
        if (cached instanceof PredictionDTO) {
            return ApiResponse.success((PredictionDTO) cached);
        }

        // 2. 缓存未命中，执行计算
        MenstrualRecord lastRecord = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId)
                .stream().findFirst().orElseThrow(() -> BusinessException.badRequest("No records found for prediction"));
        CycleCalculator.PredictionResult result = CycleCalculator.predict(
                lastRecord.getStartDate(), 28, 5);
        if (result == null) throw BusinessException.badRequest("Unable to generate prediction");
        savePrediction(userId, result);

        // 3. 写入 Redis 缓存（30分钟过期）
        PredictionDTO dto = toPredictionDTO(result);
        cacheService.set(cacheKey, dto, PREDICT_CACHE_TTL, TimeUnit.MINUTES);

        return ApiResponse.success(dto);
    }
    @Transactional
    public void savePrediction(Long userId, CycleCalculator.PredictionResult result) {
        CyclePrediction prediction = CyclePrediction.builder()
                .userId(userId).predictedStartDate(result.getNextPeriodStart()).predictedEndDate(result.getNextPeriodEnd())
                .ovulationStart(result.getOvulationStart()).ovulationEnd(result.getOvulationEnd())
                .fertileWindowStart(result.getFertileWindowStart()).fertileWindowEnd(result.getFertileWindowEnd())
                .safePeriodStart(result.getSafePeriodStart()).safePeriodEnd(result.getSafePeriodEnd()).build();
        predictionRepository.save(prediction);
    }
    public ApiResponse<List<ReminderDTO>> getReminders(Long userId, int advanceDays) {
        MenstrualRecord lastRecord = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId)
                .stream().findFirst().orElse(null);
        if (lastRecord == null) return ApiResponse.success(List.of());
        int avgCycle = 28;
        LocalDate nextPeriod = lastRecord.getStartDate().plusDays(avgCycle);
        List<CycleCalculator.Reminder> reminders = CycleCalculator.generateReminders(nextPeriod, advanceDays);
        List<ReminderDTO> dtos = reminders.stream()
                .map(r -> ReminderDTO.builder().date(r.getDate()).type(r.getType()).title(r.getTitle())
                        .description(r.getDescription()).daysUntil(r.getDaysUntil()).build())
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }
    private PredictionDTO toPredictionDTO(CycleCalculator.PredictionResult result) {
        return PredictionDTO.builder()
                .nextPeriodStart(result.getNextPeriodStart()).nextPeriodEnd(result.getNextPeriodEnd())
                .predictedCycleLength(result.getPredictedCycleLength()).predictedPeriodLength(result.getPredictedPeriodLength())
                .ovulationStart(result.getOvulationStart()).ovulationEnd(result.getOvulationEnd())
                .fertileWindowStart(result.getFertileWindowStart()).fertileWindowEnd(result.getFertileWindowEnd())
                .safePeriodStart(result.getSafePeriodStart()).safePeriodEnd(result.getSafePeriodEnd())
                .confidence("MEDIUM").build();
    }
}