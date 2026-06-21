package com.busapp.userservice.service;

import com.busapp.userservice.dto.admin.PagedResponse;
import com.busapp.userservice.dto.request.TransactionFilterRequest;
import com.busapp.userservice.dto.request.WalletFilterRequest;
import com.busapp.userservice.dto.request.WalletLoginRequest;
import com.busapp.userservice.dto.response.WalletBalanceResponse;
import com.busapp.userservice.dto.response.WalletResponse;
import com.busapp.userservice.dto.response.WalletTransactionDetailResponse;
import com.busapp.userservice.dto.response.WalletTransactionResponse;
import com.busapp.userservice.model.enums.TransactionType;

import java.util.UUID;

public interface WalletService {

    WalletResponse createWallet(WalletLoginRequest walletLoginRequest);

    /**
     * Admin-provisioned wallet creation for an arbitrary user (no PIN set — the
     * user sets their PIN on first wallet login). Distinct from {@link #createWallet}
     * which is the self-service flow for the current user.
     */
    WalletResponse createWalletForUser(Long userId);

    WalletResponse userCurrentWallet();

    WalletResponse getWalletById(UUID walletId);

    WalletResponse getWalletByUserId(Long userId);

    PagedResponse<WalletResponse> getWallets(WalletFilterRequest filter, int page, int size);

    PagedResponse<WalletTransactionResponse> getTransactions(TransactionFilterRequest filter, int page, int size);

    WalletTransactionDetailResponse getTransactionByReferenceId(String referenceId);

    WalletTransactionResponse doTransactionInternal(Long userId, String walletSessionToken, TransactionType transactionType, Double amount);

    WalletResponse walletLogin(WalletLoginRequest walletLoginRequest);

    WalletBalanceResponse getCurrentUserBalance(Long userId);

    WalletTransactionResponse refundTransaction(Long userId, Double amount, String description);
}
