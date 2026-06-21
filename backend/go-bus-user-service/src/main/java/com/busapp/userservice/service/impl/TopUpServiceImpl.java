package com.busapp.userservice.service.impl;

import com.busapp.userservice.dto.request.TopUpRequest;
import com.busapp.userservice.dto.response.TopUpResponse;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.dto.mapper.TopUpMapper;
import com.busapp.userservice.model.TopUp;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.UserWallet;
import com.busapp.userservice.model.WalletTransaction;
import com.busapp.userservice.model.enums.TopUpStatus;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.repository.TopUpRepository;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.repository.UserWalletRepository;
import com.busapp.userservice.repository.WalletTransactionRepository;
import com.busapp.userservice.service.TopUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopUpServiceImpl implements TopUpService {

    private final TopUpRepository topUpRepository;
    private final UserRepository  userRepository;
    private final TopUpMapper     topUpMapper;
    private final UserWalletRepository userWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public List<TopUpResponse> getByUserId(Long userId) {
        return topUpRepository.findByUserId(userId)
                .stream()
                .map(topUpMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopUpResponse> getByUserIdAndStatus(Long userId, TopUpStatus status) {
        return topUpRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(topUpMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TopUpResponse getByTransactionId(String transactionId) {
        TopUp topUp = topUpRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("TopUp not found for transactionId: " + transactionId));
        return topUpMapper.toResponse(topUp);
    }

    @Override
    @Transactional
    public TopUpResponse createTopUp(Long userId, TopUpRequest request) {
        log.debug("TOPUP_CREATE", kv("userId", userId), kv("amount", request.getAmount()),
                kv("paymentMethod", request.getPaymentMethod()));

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Get user wallet
        UserWallet userWallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserWallet not found: " + userId));

        // Generate transaction ID
        String transactionId = "TOPUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        request.setTransactionId(transactionId);
        // Create TopUp record
        TopUp topUp = topUpMapper.toEntity(request, user);
        topUp.setTransactionId(transactionId);
        topUp.setStatus(TopUpStatus.COMPLETED);
        topUp.setCompletedAt(LocalDateTime.now());
        TopUp savedTopUp = topUpRepository.save(topUp);
        
        // Record balance before update
        Double balanceBefore = userWallet.getBalance();

        // Update wallet balance
        userWallet.setBalance(balanceBefore + request.getAmount());
        userWallet.setLastTransaction(LocalDateTime.now());
        userWalletRepository.save(userWallet);
        log.debug("TOPUP_WALLET_UPDATED", kv("userId", userId), kv("balanceBefore", balanceBefore),
                kv("balanceAfter", userWallet.getBalance()), kv("transactionId", transactionId));

        // Create wallet transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(userWallet)
                .amount(request.getAmount())
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.COMPLETED)
                .referenceId(transactionId)
                .description("Top-up via " + request.getPaymentMethod())
                .balanceBefore(balanceBefore)
                .balanceAfter(userWallet.getBalance())
                .completedAt(LocalDateTime.now())
                .metadata("{\"topUpId\": " + savedTopUp.getId() + ", \"paymentMethod\": \"" + request.getPaymentMethod() + "\"}")
                .build();
        
        walletTransactionRepository.save(transaction);
        log.debug("TOPUP_COMPLETED", kv("userId", userId), kv("topUpId", savedTopUp.getId()),
                kv("transactionId", transactionId));

        return topUpMapper.toResponse(savedTopUp);
    }
}
