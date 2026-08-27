package com.menstrualtracker.statistics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.record.entity.MenstrualRecord;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import com.menstrualtracker.statistics.dto.StatisticsOverviewDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private MenstrualRecordRepository recordRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void staleSingleRecordPredictsFuturePeriodAndFallsBackToAverage() {
        LocalDate start = LocalDate.now().minusDays(40);
        MenstrualRecord record = MenstrualRecord.builder()
                .id(1L)
                .userId(1L)
                .startDate(start)
                .endDate(start)
                .build();

        when(recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(1L))
                .thenReturn(List.of(record));
        when(cacheService.get(anyString())).thenReturn(null);

        StatisticsOverviewDTO overview = statisticsService.getOverview(1L).getData();

        assertEquals(28.0, overview.getAvgCycleLength());
        assertFalse(overview.getNextPredictedStart().isBefore(LocalDate.now()));
    }
}
