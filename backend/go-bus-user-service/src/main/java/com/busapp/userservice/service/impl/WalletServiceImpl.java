package com.busapp.userservice.service.impl;

import com.busapp.userservice.dto.admin.PagedResponse;
import com.busapp.userservice.dto.request.TransactionFilterRequest;
import com.busapp.userservice.dto.request.WalletFilterRequest;
import com.busapp.userservice.dto.request.WalletLoginRequest;
import com.busapp.userservice.dto.response.WalletBalanceResponse;
import com.busapp.userservice.dto.response.WalletResponse;
import com.busapp.userservice.dto.response.WalletTransactionDetailResponse;
import com.busapp.userservice.dto.response.WalletTransactionResponse;
import com.busapp.userservice.exception.BadRequestException;
import com.busapp.userservice.exception.DuplicateResourceException;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.dto.mapper.WalletMapper;
import com.busapp.userservice.model.*;
import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.model.enums.WalletStatus;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.repository.UserWalletRepository;
import com.busapp.userservice.repository.WalletTransactionRepository;
import com.busapp.userservice.service.WalletService;
import com.busapp.userservice.service.WalletSessionService;
import com.busapp.userservice.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserWalletRepository        walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserRepository              userRepository;
    private final WalletMapper                walletMapper;
    private final UserUtil userUtil;
    private final PasswordEncoder passwordEncoder;
    private final WalletSessionService walletSessionService;


    @Override
    @Transactional
    public WalletResponse createWallet(WalletLoginRequest walletLoginRequest) {
        Long userId = userUtil.getCurrentUserId();
        // Check if wallet already exists
        if (walletRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("Wallet already exists for user: " + userId);
        }

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Create new wallet
        UserWallet wallet = UserWallet.builder()
                .user(user)
                .pinCode(passwordEncoder.encode(walletLoginRequest.getPinCode()))
                .balance(0.0)
                .currency(Currency.USD)
                .status(WalletStatus.ACTIVE)
                .build();
        user.setIsWalletExist(true);
        userRepository.save(user);

        UserWallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse createWalletForUser(Long userId) {
        // Admin provisioning a wallet for another user — no current-user lookup, no PIN.
        if (walletRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("Wallet already exists for user: " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserWallet wallet = UserWallet.builder()
                .user(user)
                .pinCode(null)            // user sets their PIN on first wallet login
                .balance(0.0)
                .currency(Currency.USD)
                .status(WalletStatus.ACTIVE)
                .build();
        user.setIsWalletExist(true);
        userRepository.save(user);

        UserWallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    @Override
    public WalletResponse userCurrentWallet() {
        return walletMapper.toResponse(walletRepository.findByUserId(userUtil.getCurrentUserId()).orElseThrow(()->
        {
            log.error("User wallet not found for user: " + userUtil.getCurrentUserId());
           return new ResourceNotFoundException("User Wallet User Id : " + userUtil.getCurrentUserId()+"not found");
        }
        ));
    }

    @Override
    public WalletResponse getWalletById(UUID walletId) {
        UserWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));
        return walletMapper.toResponse(wallet);
    }

    @Override
    public WalletResponse getWalletByUserId(Long userId) {
        UserWallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
        return walletMapper.toResponse(wallet);
    }

    @Override
    public PagedResponse<WalletResponse> getWallets(WalletFilterRequest filter, int page, int size) {
        Page<UserWallet> walletPage = walletRepository.findAll(
                WalletSpecification.filterBy(filter),
                PageRequest.of(page, size)
        );

        List<WalletResponse> content = walletPage.getContent().stream()
                .map(walletMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<WalletResponse>builder()
                .content(content)
                .page(walletPage.getNumber())
                .size(walletPage.getSize())
                .totalElements(walletPage.getTotalElements())
                .totalPages(walletPage.getTotalPages())
                .build();
    }

    @Override
    public PagedResponse<WalletTransactionResponse> getTransactions(TransactionFilterRequest filter, int page, int size) {
        if(page<1){
            page=1;
        }
        Page<WalletTransaction> transactionPage = transactionRepository.findAll(
                TransactionSpecification.filterBy(filter),
                PageRequest.of(page-1, size)
        );

        List<WalletTransactionResponse> content = transactionPage.getContent().stream()
                .map(walletMapper::toTransactionResponse)
                .collect(Collectors.toList());

        return PagedResponse.<WalletTransactionResponse>builder()
                .content(content)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletTransactionDetailResponse getTransactionByReferenceId(String referenceId) {
        Long currentUserId = userUtil.getCurrentUserId();

        WalletTransaction transaction = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for reference: " + referenceId));

        // Ensure the transaction belongs to the current user's wallet
        Long ownerUserId = transaction.getWallet().getUser().getId();
        if (!ownerUserId.equals(currentUserId)) {
            log.warn("[WALLET TRANSACTION] User {} attempted to access transaction {} owned by user {}",
                    currentUserId, referenceId, ownerUserId);
            throw new ResourceNotFoundException("Transaction not found for reference: " + referenceId);
        }

        return walletMapper.toTransactionDetailResponse(transaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponse doTransactionInternal(Long userId, String walletSessionToken, TransactionType transactionType, Double amount) {
        // Validate wallet session
        if (!walletSessionService.isWalletSessionValid(userId, walletSessionToken)) {
            log.error("[WALLET PAYMENT] Invalid wallet session for user: {}", userId);
            throw new BadRequestException("Wallet session invalid or expired. Please login to your wallet first.");
        }
        
        // Refresh session on valid request
        walletSessionService.refreshWalletSession(userId);
        
        UserWallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
        
        switch (transactionType) {
            case PAYMENT -> {
                return paymentTransaction(userWallet, userId, amount);
            }
            case REFUND -> {
                return refundTransaction(userWallet, userId, amount);
            }
            default -> throw new BadRequestException("Unknown transaction type: " + transactionType);
        }
    }

    private WalletTransactionResponse paymentTransaction(UserWallet userWallet, Long userId, Double amount) {
        // Validate amount
        if (amount == null || amount <= 0) {
            throw new BadRequestException("Payment amount must be greater than 0");
        }
        
        // Check if wallet has sufficient balance
        if (userWallet.getBalance() < amount) {
            throw new BadRequestException(
                String.format("Insufficient wallet balance. Current balance: %.2f, Required: %.2f", 
                    userWallet.getBalance(), amount)
            );
        }
        
        String referenceId = UUID.randomUUID().toString().substring(0, 8);
        
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .wallet(userWallet)
                .amount(amount)
                .type(TransactionType.PAYMENT)
                .status(TransactionStatus.COMPLETED)
                .referenceId(referenceId)
                .balanceAfter(userWallet.getBalance() - amount)
                .balanceBefore(userWallet.getBalance())
                .metadata("{\"referenceId\":\"" + referenceId + "\",\"transactionType\":\"PAYMENT\"}")
                .build();
        
        userWallet.setBalance(userWallet.getBalance() - amount);
        userWallet.setLastTransaction(LocalDateTime.now());
        
        transactionRepository.save(walletTransaction);
        walletRepository.save(userWallet);
        
        log.info("[WALLET PAYMENT] Payment completed - userId={}, amount={}, newBalance={}", 
                userId, amount, userWallet.getBalance());
        
        return walletMapper.toTransactionResponse(walletTransaction);
    }

    private WalletTransactionResponse refundTransaction(UserWallet userWallet, Long userId, Double amount) {
        // Validate amount
        if (amount == null || amount <= 0) {
            throw new BadRequestException("Refund amount must be greater than 0");
        }
        
        String referenceId = UUID.randomUUID().toString().substring(0, 8);
        
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .wallet(userWallet)
                .amount(amount)
                .type(TransactionType.REFUND)
                .status(TransactionStatus.COMPLETED)
                .referenceId(referenceId)
                .balanceAfter(userWallet.getBalance() + amount)
                .balanceBefore(userWallet.getBalance())
                .metadata("{\"referenceId\":\"" + referenceId + "\",\"transactionType\":\"REFUND\"}")
                .build();
        
        userWallet.setBalance(userWallet.getBalance() + amount);
        userWallet.setLastTransaction(LocalDateTime.now());
        
        transactionRepository.save(walletTransaction);
        walletRepository.save(userWallet);
        
        log.info("[WALLET REFUND] Refund completed - userId={}, amount={}, newBalance={}", 
                userId, amount, userWallet.getBalance());
        
        return walletMapper.toTransactionResponse(walletTransaction);
    }
    
    @Override
    public WalletResponse walletLogin(WalletLoginRequest walletLoginRequest) {
        Long currentUserId = userUtil.getCurrentUserId();
        UserWallet wallet = walletRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + currentUserId));
        if(!userRepository.findById(currentUserId).isPresent()) {
            throw new  ResourceNotFoundException("User not found for user: " + currentUserId);
        }
        if (!passwordEncoder.matches(walletLoginRequest.getPinCode(), wallet.getPinCode())) {
            throw new BadRequestException("Invalid PIN code");
        }

        // Create wallet session
        String sessionToken = walletSessionService.createWalletSession(currentUserId);
        
        WalletResponse response = walletMapper.toResponse(wallet);
        response.setWalletSessionToken(sessionToken);
        
        log.info("[WALLET LOGIN] User {} logged into wallet successfully", currentUserId);
        return response;
    }

    @Override
    @Transactional
    public WalletTransactionResponse refundTransaction(Long userId, Double amount, String description) {
        log.info("[WALLET REFUND] Processing refund for user {} - amount: {}", userId, amount);
        
        // Validate amount
        if (amount == null || amount <= 0) {
            throw new BadRequestException("Refund amount must be greater than 0");
        }
        
        // Get user wallet
        UserWallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
        
        String referenceId = UUID.randomUUID().toString().substring(0, 8);
        
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .wallet(userWallet)
                .amount(amount)
                .type(TransactionType.REFUND)
                .status(TransactionStatus.COMPLETED)
                .referenceId(referenceId)
                .description(description)
                .balanceAfter(userWallet.getBalance() + amount)
                .balanceBefore(userWallet.getBalance())
                .metadata("{\"referenceId\":\"" + referenceId + "\",\"transactionType\":\"REFUND\",\"description\":\"" + description + "\"}")
                .build();
        
        userWallet.setBalance(userWallet.getBalance() + amount);
        userWallet.setLastTransaction(LocalDateTime.now());
        
        transactionRepository.save(walletTransaction);
        walletRepository.save(userWallet);
        
        log.info("[WALLET REFUND] Refund completed - userId={}, amount={}, newBalance={}", 
                userId, amount, userWallet.getBalance());
        
        return walletMapper.toTransactionResponse(walletTransaction);
    }

    @Override
    public WalletBalanceResponse getCurrentUserBalance(Long userId) {
        UserWallet wallet=walletRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
        return WalletBalanceResponse.builder()
                .balance(wallet.getBalance())
                .build();
    }
}
