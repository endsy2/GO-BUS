package com.busapp.bookingservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardUpdateEvent {
    private String type;
    private Long activeBookings;
    private BigDecimal todayRevenue;
    private Long pendingRefunds;
    private Long todayBookings;
    private Long bookingId;
    private BigDecimal amount;
    private String route;
    private Long refundId;
    private String reason;
    private LocalDateTime timestamp;
}
