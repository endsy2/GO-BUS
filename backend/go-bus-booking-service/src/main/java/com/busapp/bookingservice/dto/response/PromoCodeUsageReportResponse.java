package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeUsageReportResponse {
    private Long promoId;
    private String promoCode;
    private String discountType;
    private Double discountValue;
    private Long totalUsages;
    private Integer maxUses;
    private Double totalDiscountGiven;
    private Double totalRevenueWithPromo;
    private String status;
    private String validFrom;
    private String validTo;
}
