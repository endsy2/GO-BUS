package com.busapp.bookingservice.dto.event;

import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.response.PaymentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private BookingResponse booking;
    private PaymentResponse payment;
    private String reason;
    private String adminInfo;
    private String action;
    private Double refundAmount;
    private Long bookingId;
}
