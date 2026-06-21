package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketFilterRequest {
    
    private Long bookingId;
    private Long userId;
    private Long scheduleId;
    private LocalDateTime issuedFrom;
    private LocalDateTime issuedTo;
    private String qrCode;
    private TicketStatus ticketStatus;
}
