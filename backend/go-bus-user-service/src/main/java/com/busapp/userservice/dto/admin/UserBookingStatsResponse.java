package com.busapp.userservice.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingStatsResponse {
    private Long totalBookings;
    private BigDecimal totalSpent;
    private Long activeTickets;
}
