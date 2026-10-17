package com.busapp.userservice.service;

import com.busapp.userservice.dto.request.CheckTopUpRequest;
import com.busapp.userservice.dto.request.TopUpBakongRequest;
import com.busapp.userservice.dto.response.BakongQrData;

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
