package com.busapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDemographicsReportResponse {
    private String gender;
    private Long totalUsers;
    private Long activeUsers;
    private Long totalBookings;
    private Double totalRevenue;
    private Double averageBookingsPerUser;
}
