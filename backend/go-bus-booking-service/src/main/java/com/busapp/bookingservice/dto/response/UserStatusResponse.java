package com.busapp.bookingservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatusResponse {
    private Long totalBookings;
    private Double totalSpent;
    private Long activeTickets;
}
