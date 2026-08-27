package com.menstrualtracker.record.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.record.dto.*;
import com.menstrualtracker.record.entity.MenstrualRecord;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenstrualRecordService {

    private final MenstrualRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    private static final String PREDICT_CACHE_PATTERN = "menstrual:user:%d:predict";
    private static final String OVERVIEW_CACHE_PATTERN = "menstrual:user:%d:overview";

    // List queries go straight to the database. The previous Redis cache stored
    // only page content, which corrupted totalElements on cache hits.
    public ApiResponse<Page<RecordDTO>> getRecords(Long userId, RecordPageQuery query) {
        PageRequest pageRequest = PageRequest.of(query.getPage() - 1, query.getSize());
        Page<RecordDTO> result;
        if (query.getYear() != null && query.getMonth() != null) {
            int month = Integer.parseInt(query.getMonth().split("-")[1]);
            result = recordRepository
                    .findByUserIdAndYearAndMonth(userId, query.getYear(), month, pageRequest)
                    .map(this::toDTO);
        } else if (query.getYear() != null) {
            result = recordRepository
                    .findByUserIdAndYear(userId, query.getYear(), pageRequest)
                    .map(this::toDTO);
        } else {
            result = recordRepository
                    .findByUserIdAndDeletedFalseOrderByStartDateDesc(userId, pageRequest)
                    .map(this::toDTO);
        }
        return ApiResponse.success(result);
    }

    public ApiResponse<RecordDTO> getRecord(Long userId, Long recordId) {
        return ApiResponse.success(
                toDTO(recordRepository.findByIdAndUserIdAndDeletedFalse(recordId, userId)
                        .orElseThrow(() -> BusinessException.notFound("Record not found"))));
    }

    @Transactional
    public ApiResponse<RecordDTO> createRecord(Long userId, RecordCreateRequest request) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw BusinessException.badRequest("End date cannot be before start date");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));

        List<MenstrualRecord> allRecords =
                recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId);
        var prevStart = allRecords.stream()
                .map(MenstrualRecord::getStartDate)
                .filter(d -> d.isBefore(request.getStartDate()))
                .findFirst().orElse(null);

        int cycleDay = 1;
        if (prevStart != null) {
            cycleDay = (int) ChronoUnit.DAYS.between(prevStart, request.getStartDate()) + 1;
        }

        MenstrualRecord record = MenstrualRecord.builder()
                .userId(userId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .flow(request.getFlow())
                .painLevel(request.getPainLevel())
                .color(request.getColor())
                .clots(request.getClots() != null && request.getClots())
                .symptoms(toJson(request.getSymptoms()))
                .moodTags(toJson(request.getMoodTags()))
                .notes(request.getNotes())
                .cycleDay(cycleDay)
                .build();
        record = recordRepository.save(record);

        invalidatePredictionCaches(userId);
        return ApiResponse.success("Record created", toDTO(record));
    }

    @Transactional
    public ApiResponse<RecordDTO> updateRecord(Long userId, Long recordId,
                                               RecordUpdateRequest request) {
        MenstrualRecord record =
                recordRepository.findByIdAndUserIdAndDeletedFalse(recordId, userId)
                        .orElseThrow(() -> BusinessException.notFound("Record not found"));
        if (request.getStartDate() != null) record.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) record.setEndDate(request.getEndDate());
        if (request.getFlow() != null) record.setFlow(request.getFlow());
        if (request.getPainLevel() != null) record.setPainLevel(request.getPainLevel());
        if (request.getColor() != null) record.setColor(request.getColor());
        if (request.getClots() != null) record.setClots(request.getClots());
        if (request.getSymptoms() != null) record.setSymptoms(toJson(request.getSymptoms()));
        if (request.getMoodTags() != null) record.setMoodTags(toJson(request.getMoodTags()));
        if (request.getNotes() != null) record.setNotes(request.getNotes());
        if (record.getEndDate() != null && record.getEndDate().isBefore(record.getStartDate())) {
            throw BusinessException.badRequest("End date cannot be before start date");
        }
        record = recordRepository.save(record);

        invalidatePredictionCaches(userId);
        return ApiResponse.success("Record updated", toDTO(record));
    }

    @Transactional
    public ApiResponse<Void> deleteRecord(Long userId, Long recordId) {
        MenstrualRecord record =
                recordRepository.findByIdAndUserIdAndDeletedFalse(recordId, userId)
                        .orElseThrow(() -> BusinessException.notFound("Record not found"));
        record.setDeleted(true);
        recordRepository.save(record);

        invalidatePredictionCaches(userId);
        return ApiResponse.success("Record deleted", null);
    }

    public List<MenstrualRecord> getAllRecords(Long userId) {
        return recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId);
    }

    private void invalidatePredictionCaches(Long userId) {
        cacheService.deleteByPattern(String.format(PREDICT_CACHE_PATTERN, userId));
        cacheService.deleteByPattern(String.format(OVERVIEW_CACHE_PATTERN, userId));
    }

    private RecordDTO toDTO(MenstrualRecord record) {
        return RecordDTO.builder()
                .id(record.getId()).userId(record.getUserId())
                .startDate(record.getStartDate()).endDate(record.getEndDate())
                .cycleDay(record.getCycleDay()).flow(record.getFlow())
                .painLevel(record.getPainLevel()).color(record.getColor())
                .clots(record.getClots())
                .symptoms(fromJson(record.getSymptoms()))
                .moodTags(fromJson(record.getMoodTags()))
                .notes(record.getNotes()).createdAt(record.getCreatedAt())
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
