package com.busapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentTravelerReportResponse {
    private Long userId;
    private String userName;
    private String fullName;
    private String email;
    private String phone;
    private Long totalBookings;
    private Long confirmedBookings;
    private Double totalSpent;
    private String firstBookingDate;
    private String lastBookingDate;
    private Integer rank;
}
