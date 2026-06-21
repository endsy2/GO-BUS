package com.busapp.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "booking-service", url = "${booking.service.url:http://localhost:8083}")
public interface BookingClient {

    @GetMapping("/api/internal/bookings/user-stats")
    Map<String, Object> getUserBookingStats(@RequestParam Long userId,
                                            @RequestParam LocalDate startDate,
                                            @RequestParam LocalDate endDate);

    @GetMapping("/api/internal/bookings/user-detail-stats")
    Map<String, Object> getUserDetailStats(@RequestParam Long userId);

    @GetMapping("/api/internal/bookings/active-users")
    List<Map<String, Object>> getActiveUsers(@RequestParam LocalDate startDate,
                                             @RequestParam LocalDate endDate);

    @GetMapping("/api/internal/bookings/frequent-travelers")
    List<Map<String, Object>> getFrequentTravelers(@RequestParam LocalDate startDate,
                                                   @RequestParam LocalDate endDate,
                                                   @RequestParam Integer limit);

    @GetMapping("/api/internal/bookings/patterns-by-day")
    List<Map<String, Object>> getBookingPatternsByDay(@RequestParam LocalDate startDate,
                                                      @RequestParam LocalDate endDate);

    @GetMapping("/api/internal/bookings/patterns-by-hour")
    List<Map<String, Object>> getBookingPatternsByHour(@RequestParam LocalDate startDate,
                                                       @RequestParam LocalDate endDate);
}
