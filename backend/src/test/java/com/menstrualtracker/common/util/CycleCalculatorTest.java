package com.menstrualtracker.common.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class CycleCalculatorTest {

    @Test
    void testCalculateCycleLength() {
        LocalDate prev = LocalDate.of(2026, 6, 1);
        LocalDate curr = LocalDate.of(2026, 6, 29);
        assertEquals(28, CycleCalculator.calculateCycleLength(prev, curr));
    }

    @Test
    void testCalculatePeriodLength() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 5);
        assertEquals(5, CycleCalculator.calculatePeriodLength(start, end));
    }

    @Test
    void testPredict() {
        LocalDate lastPeriod = LocalDate.of(2026, 6, 1);
        CycleCalculator.PredictionResult result = CycleCalculator.predict(lastPeriod, 28, 5);
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 6, 29), result.getNextPeriodStart());
        assertEquals(LocalDate.of(2026, 7, 3), result.getNextPeriodEnd());
        assertNotNull(result.getOvulationStart());
        assertNotNull(result.getOvulationEnd());
    }

    @Test
    void testPredictWithNull() {
        assertNull(CycleCalculator.predict(null, 28, 5));
    }

    @Test
    void testGenerateReminders() {
        LocalDate nextPeriod = LocalDate.now().plusDays(3);
        var reminders = CycleCalculator.generateReminders(nextPeriod, 7);
        assertFalse(reminders.isEmpty());
        assertEquals("PERIOD_EXPECTED", reminders.get(0).getType());
    }

    @Test
    void testGenerateRemindersNoMatch() {
        LocalDate farFuture = LocalDate.now().plusDays(100);
        var reminders = CycleCalculator.generateReminders(farFuture, 7);
        assertTrue(reminders.isEmpty());
    }
}
