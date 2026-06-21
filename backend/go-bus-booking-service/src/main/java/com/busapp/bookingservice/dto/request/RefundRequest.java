package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.RefundMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class    RefundRequest {
//    private Double amount;
    private String reason;
    private RefundMethod method;
}
