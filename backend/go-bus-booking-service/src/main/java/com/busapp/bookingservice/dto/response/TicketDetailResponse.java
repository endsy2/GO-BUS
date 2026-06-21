package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketDetailResponse {
    private Long id;
    private BookingDetailResponse bookingDetailResponse;
    private String qrCode;
    private LocalDateTime issuedAt;
}
