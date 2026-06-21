package com.busapp.userservice.service;

import com.busapp.userservice.dto.request.TopUpRequest;
import com.busapp.userservice.dto.response.TopUpResponse;
import com.busapp.userservice.model.enums.TopUpStatus;

import java.util.List;

public interface TopUpService {

    List<TopUpResponse> getByUserId(Long userId);

    List<TopUpResponse> getByUserIdAndStatus(Long userId, TopUpStatus status);

    TopUpResponse getByTransactionId(String transactionId);

    TopUpResponse createTopUp(Long userId, TopUpRequest request);
}
