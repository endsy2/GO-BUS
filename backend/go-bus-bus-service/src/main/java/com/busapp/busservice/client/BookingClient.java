package com.busapp.busservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "booking-service", url = "${booking.service.url:http://localhost:8083}")
public interface BookingClient {

    @GetMapping("/api/internal/bookings/count-by-schedule")
    Long getBookingCountBySchedule(@RequestParam Long scheduleId);

    @GetMapping("/api/internal/bookings/count-by-schedules")
    Long getBookingCountBySchedules(@RequestParam List<Long> scheduleIds);

    @GetMapping("/api/internal/bookings/revenue-by-schedules")
    Double getRevenueBySchedules(@RequestParam List<Long> scheduleIds);

    @GetMapping("/api/internal/bookings/stats-by-schedule")
    Map<String, Object> getBookingStatsBySchedule(@RequestParam Long scheduleId);
}
