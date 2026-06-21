package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingReportResponse {
    private Long totalBookings;
    private Long confirmedBookings;
    private Long pendingBookings;
    private Long cancelledBookings;
    private Map<String, Long> bookingsByStatus;
    private LocalDate startDate;
    private LocalDate endDate;
}
