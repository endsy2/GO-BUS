package com.busapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveCustomerReportResponse {
    private Long userId;
    private String userName;
    private String fullName;
    private String email;
    private String phone;
    private Long totalBookings;
    private Double totalSpent;
    private String lastBookingDate;
}
