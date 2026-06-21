package com.busapp.userservice.util;

import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.model.TopUp;
import com.busapp.userservice.repository.TopUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopUpUtil {
    private final TopUpRepository topUpRepository;

    public TopUp findByTransactionId(String transactionId){
        return topUpRepository.findByTransactionId(transactionId)
                .orElseThrow(()->new ResourceNotFoundException("Top-up transaction not found: " + transactionId));
    }
    public TopUp findById(Long topUPId){
        return topUpRepository.findById(topUPId).orElseThrow(()->new ResourceNotFoundException("Top-up ID not found: " + topUPId));
    }
}
