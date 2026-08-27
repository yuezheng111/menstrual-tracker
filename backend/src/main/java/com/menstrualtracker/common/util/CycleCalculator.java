package com.menstrualtracker.common.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CycleCalculator {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionResult {
        private LocalDate nextPeriodStart;
        private LocalDate nextPeriodEnd;
        private LocalDate ovulationStart;
        private LocalDate ovulationEnd;
        private LocalDate fertileWindowStart;
        private LocalDate fertileWindowEnd;
        private LocalDate safePeriodStart;
        private LocalDate safePeriodEnd;
        private int predictedCycleLength;
        private int predictedPeriodLength;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reminder {
        private LocalDate date;
        private String type;
        private String title;
        private String description;
        private int daysUntil;
    }

    public static int calculateCycleLength(LocalDate previousStart, LocalDate currentStart) {
        if (previousStart == null || currentStart == null) return 28;
        return (int) ChronoUnit.DAYS.between(previousStart, currentStart);
    }

    public static int calculatePeriodLength(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 5;
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    public static PredictionResult predict(LocalDate lastPeriodStart, int avgCycleDays, int avgPeriodDays) {
        if (lastPeriodStart == null) return null;

        int cycleDays = Math.max(1, avgCycleDays);
        LocalDate today = LocalDate.now();
        LocalDate nextStart = lastPeriodStart.plusDays(cycleDays);

        // 已错过预测日期时，按平均周期滚动到下一个未来日期，避免出现“还有 -N 天”。
        if (nextStart.isBefore(today)) {
            long missedDays = ChronoUnit.DAYS.between(nextStart, today);
            long missedCycles = (missedDays + cycleDays - 1) / cycleDays;
            nextStart = nextStart.plusDays(missedCycles * cycleDays);
        }

        LocalDate cycleStart = nextStart.minusDays(cycleDays);
        LocalDate nextEnd = nextStart.plusDays(avgPeriodDays - 1);

        int ovulationDay = cycleDays - 14;
        LocalDate ovulation = cycleStart.plusDays(ovulationDay);
        LocalDate ovulationStart = ovulation.minusDays(1);
        LocalDate ovulationEnd = ovulation.plusDays(1);

        LocalDate fertileStart = cycleStart.plusDays(Math.max(0, ovulationDay - 5));
        LocalDate fertileEnd = cycleStart.plusDays(Math.min(cycleDays - 1, ovulationDay + 2));

        LocalDate safeStart = nextStart.plusDays(avgPeriodDays);
        LocalDate safeEnd = fertileStart.minusDays(1);
        if (safeEnd.isBefore(safeStart)) {
            safeEnd = safeStart;
        }

        return PredictionResult.builder()
                .nextPeriodStart(nextStart)
                .nextPeriodEnd(nextEnd)
                .ovulationStart(ovulationStart)
                .ovulationEnd(ovulationEnd)
                .fertileWindowStart(fertileStart)
                .fertileWindowEnd(fertileEnd)
                .safePeriodStart(safeStart)
                .safePeriodEnd(safeEnd)
                .predictedCycleLength(cycleDays)
                .predictedPeriodLength(avgPeriodDays)
                .build();
    }

    public static List<Reminder> generateReminders(LocalDate nextPeriodStart, int advanceDays) {
        List<Reminder> reminders = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if (nextPeriodStart == null) return reminders;

        long daysUntil = ChronoUnit.DAYS.between(today, nextPeriodStart);

        if (daysUntil >= 0 && daysUntil <= advanceDays) {
            reminders.add(Reminder.builder()
                    .date(nextPeriodStart)
                    .type("PERIOD_EXPECTED")
                    .title("Period Expected Soon")
                    .description("Your period is expected to start in " + daysUntil + " day(s).")
                    .daysUntil((int) daysUntil)
                    .build());
        }

        LocalDate fertileStart = nextPeriodStart.minusDays(14 + 5);
        LocalDate fertileEnd = nextPeriodStart.minusDays(14 - 2);
        long fertileDaysUntil = ChronoUnit.DAYS.between(today, fertileStart);

        if (fertileDaysUntil >= 0 && fertileDaysUntil <= advanceDays) {
            reminders.add(Reminder.builder()
                    .date(fertileStart)
                    .type("FERTILE_WINDOW")
                    .title("Fertile Window Approaching")
                    .description("Your fertile window is expected to start in " + fertileDaysUntil + " day(s).")
                    .daysUntil((int) fertileDaysUntil)
                    .build());
        }

        return reminders;
    }
}
