package com.busapp.bookingservice.dto.event;

import com.busapp.bookingservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEvent {
    private String type;
    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private PaymentStatus status;
    private Double amount;
    private String qrCode;
    private String reason;
    private Long ticketId;
    private LocalDateTime timestamp;
}
