package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingVelocityResponse {
    private LocalDate date;
    private Long seaterBookings;
    private Long sleeperBookings;
    private Long totalBookings;
    private BigDecimal seaterRevenue;
    private BigDecimal sleeperRevenue;
    
    // Calculated fields for insights
    private Double seaterPercentage;
    private Double sleeperPercentage;
}
