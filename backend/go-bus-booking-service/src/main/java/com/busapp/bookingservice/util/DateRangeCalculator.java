package com.busapp.bookingservice.util;

import com.busapp.bookingservice.exception.BadRequestException;
import com.busapp.bookingservice.model.enums.ReportPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

public class DateRangeCalculator {

    @Data
    @AllArgsConstructor
    public static class DateRange {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    public static DateRange calculateDateRange(
            ReportPeriod period,
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate,
            Integer year,
            Integer month) {

        LocalDate now = LocalDate.now();

        return switch (period) {
            case DAILY -> calculateDaily(date, now);
            case WEEKLY -> calculateWeekly(startDate, now);
            case MONTHLY -> calculateMonthly(year, month, now);
            case YEARLY -> calculateYearly(year, now);
            case CUSTOM -> calculateCustom(startDate, endDate);
        };
    }

    private static DateRange calculateDaily(LocalDate date, LocalDate now) {
        LocalDate targetDate = date != null ? date : now;
        return new DateRange(targetDate, targetDate);
    }

    private static DateRange calculateWeekly(LocalDate startDate, LocalDate now) {
        LocalDate start = startDate != null ? startDate : now.minusWeeks(1);
        LocalDate end = start.plusWeeks(1).minusDays(1);
        return new DateRange(start, end);
    }

    private static DateRange calculateMonthly(Integer year, Integer month, LocalDate now) {
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        
        LocalDate start = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return new DateRange(start, end);
    }

    private static DateRange calculateYearly(Integer year, LocalDate now) {
        int targetYear = year != null ? year : now.getYear();
        LocalDate start = LocalDate.of(targetYear, 1, 1);
        LocalDate end = LocalDate.of(targetYear, 12, 31);
        return new DateRange(start, end);
    }

    private static DateRange calculateCustom(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("startDate and endDate are required for custom period");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
        return new DateRange(startDate, endDate);
    }
}
