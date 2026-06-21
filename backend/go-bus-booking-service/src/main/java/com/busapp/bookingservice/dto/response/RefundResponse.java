package com.busapp.bookingservice.dto.response;

import com.busapp.bookingservice.model.enums.RefundMethod;
import com.busapp.bookingservice.model.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String adminNote;
    private Long processedBy;
    private RefundMethod refundMethod;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
