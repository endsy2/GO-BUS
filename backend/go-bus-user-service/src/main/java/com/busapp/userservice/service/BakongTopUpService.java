package com.busapp.userservice.service;

import com.busapp.userservice.dto.request.CheckTopUpRequest;
import com.busapp.userservice.dto.request.TopUpBakongRequest;
import com.busapp.userservice.dto.response.BakongCheckTopUpResponse;
import com.busapp.userservice.dto.response.BakongQrData;
import com.busapp.userservice.dto.response.BakongResponse;
import com.busapp.userservice.dto.response.TopUpBakongResponse;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;

public interface BakongTopUpService {
    
    /**
     * Generate Bakong KHQR for wallet top-up
     */
    BakongQrData generateTopUpKhqr(Long userId, TopUpBakongRequest request);
    
    /**
     * Check top-up transaction status
     */
    void checkTopUpTransaction(Long userId, CheckTopUpRequest request);
}
