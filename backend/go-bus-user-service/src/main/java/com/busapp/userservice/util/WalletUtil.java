package com.busapp.userservice.util;

import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.model.UserWallet;
import com.busapp.userservice.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletUtil {
    private final UserWalletRepository userWalletRepository;

    public UserWallet findUserWalletById(Long userId){
        return userWalletRepository.findByUserId(userId).orElseThrow(()->new ResourceNotFoundException("User wallet not found for userId: " + userId));
    }
    public UserWallet findUserWalletByWalletId(UUID walletId){
        return userWalletRepository.findById(walletId).orElseThrow(()->new ResourceNotFoundException("Wallet not found for walletId: " + walletId));
    }
}
