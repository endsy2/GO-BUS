package com.busapp.bookingservice.dto.response;

import com.busapp.bookingservice.model.enums.DiscountType;
import com.busapp.bookingservice.model.enums.PromoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponse {
    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private Double discountValue;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private PromoStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
