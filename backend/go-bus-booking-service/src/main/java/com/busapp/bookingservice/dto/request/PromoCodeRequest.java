package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.DiscountType;
import com.busapp.bookingservice.model.enums.PromoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequest {
    @NotBlank
    private String code;
    private String description;
    @NotNull
    private DiscountType discountType;
    @NotNull
    private Double discountValue;
    private Integer maxUses;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    @Builder.Default
    private PromoStatus status = PromoStatus.ACTIVE;
}
