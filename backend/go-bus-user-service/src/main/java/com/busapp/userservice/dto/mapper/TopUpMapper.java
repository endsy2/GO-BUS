package com.busapp.userservice.dto.mapper;

import com.busapp.userservice.dto.request.TopUpRequest;
import com.busapp.userservice.dto.response.TopUpResponse;
import com.busapp.userservice.model.TopUp;
import com.busapp.userservice.model.User;
import org.springframework.stereotype.Component;

@Component
public class TopUpMapper {

    public TopUpResponse toResponse(TopUp topUp) {
        TopUpResponse response = new TopUpResponse();
        response.setId(topUp.getId());
        response.setUserId(topUp.getUser().getId());
        response.setAmount(topUp.getAmount());
        response.setPaymentMethod(topUp.getPaymentMethod());
        response.setStatus(topUp.getStatus());
        response.setPaymentGateway(topUp.getPaymentGateway());
        response.setTransactionId(topUp.getTransactionId());
        response.setCompletedAt(topUp.getCompletedAt());
        response.setCreatedAt(topUp.getCreatedAt());
        return response;
    }

    public TopUp toEntity(TopUpRequest request, User user) {
        TopUp topUp = new TopUp();
        topUp.setUser(user);
        topUp.setAmount(request.getAmount());
        topUp.setPaymentMethod(request.getPaymentMethod());
        topUp.setPaymentGateway(request.getPaymentGateway()==null?null:request.getPaymentGateway());
        topUp.setTransactionId(request.getTransactionId());
        return topUp;
    }
}
