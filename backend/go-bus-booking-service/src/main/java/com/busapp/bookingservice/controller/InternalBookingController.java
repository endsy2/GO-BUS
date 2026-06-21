package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.BookingSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/bookings")
@RequiredArgsConstructor
public class InternalBookingController {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @GetMapping("/count-by-schedule")
    public Long getBookingCountBySchedule(@RequestParam Long scheduleId) {
        return bookingRepository.countByScheduleIdAndBookingStatus(scheduleId, BookingStatus.CONFIRMED);
    }

    @GetMapping("/count-by-schedules")
    public Long getBookingCountBySchedules(@RequestParam List<Long> scheduleIds) {
        return scheduleIds.stream()
                .mapToLong(scheduleId -> bookingRepository.countByScheduleIdAndBookingStatus(scheduleId, BookingStatus.CONFIRMED))
                .sum();
    }

    @GetMapping("/revenue-by-schedules-date")
    public Double getRevenueBySchedules(@RequestParam List<Long> scheduleIds,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        return scheduleIds.stream()
                .mapToDouble(scheduleId -> {
                    Double revenue = bookingRepository.calculateRevenueByScheduleIdWithDate(scheduleId, start, end);
                    return revenue != null ? revenue : 0.0;
                })
                .sum();
    }
    @GetMapping("/revenue-by-schedules")
    public Double getRevenueBySchedules(
            @RequestParam List<Long> scheduleIds) {


        return scheduleIds.stream()
                .mapToDouble(scheduleId -> {
                    Double revenue = bookingRepository.calculateRevenueByScheduleId(scheduleId);
                    return revenue != null ? revenue : 0.0;
                })
                .sum();
    }
    @GetMapping("/stats-by-schedule")
    public Map<String, Object> getBookingStatsBySchedule(@RequestParam Long scheduleId) {
        Long confirmedBookings = bookingRepository.countByScheduleIdAndBookingStatus(scheduleId, BookingStatus.CONFIRMED);
        Long bookedSeats = bookingSeatRepository.countBookedSeatsBySchedule(scheduleId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("confirmedBookings", confirmedBookings);
        stats.put("bookedSeats", bookedSeats != null ? bookedSeats : 0);
        
        return stats;
    }

    // Customer analytics endpoints

    @GetMapping("/user-stats")
    public Map<String, Object> getUserBookingStats(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Long totalBookings = bookingRepository.countByUserIdAndDateRange(userId, start, end);
        Double totalRevenue = bookingRepository.calculateRevenueByUserId(userId, start, end);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", totalBookings != null ? totalBookings : 0L);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        return stats;
    }

    @GetMapping("/active-users")
    public List<Map<String, Object>> getActiveUsers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return bookingRepository.getActiveUsersStats(start, end);
    }

    @GetMapping("/frequent-travelers")
    public List<Map<String, Object>> getFrequentTravelers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Integer limit) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return bookingRepository.getFrequentTravelers(start, end, limit);
    }

    @GetMapping("/patterns-by-day")
    public List<Map<String, Object>> getBookingPatternsByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return bookingRepository.getBookingPatternsByDayOfWeek(start, end);
    }







    @GetMapping("/patterns-by-hour")
    public List<Map<String, Object>> getBookingPatternsByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return bookingRepository.getBookingPatternsByHour(start, end);
    }
}
