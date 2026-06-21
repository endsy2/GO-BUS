package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.request.PromoCodeRequest;
import com.busapp.bookingservice.dto.response.PromoCodeResponse;

import java.util.List;

public interface PromoCodeService {
    List<PromoCodeResponse> getAllPromos();
    PromoCodeResponse getPromoById(Long id);
    PromoCodeResponse getPromoByCode(String code);
    PromoCodeResponse createPromo(PromoCodeRequest request);
    PromoCodeResponse updatePromo(Long id, PromoCodeRequest request);
    void deletePromo(Long id);
}
