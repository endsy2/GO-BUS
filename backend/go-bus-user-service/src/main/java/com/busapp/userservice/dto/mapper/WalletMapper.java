package com.busapp.userservice.dto.mapper;

import com.busapp.userservice.dto.request.CreateUserWalletRequest;
import com.busapp.userservice.dto.response.WalletResponse;
import com.busapp.userservice.dto.response.WalletTransactionDetailResponse;
import com.busapp.userservice.dto.response.WalletTransactionResponse;
import com.busapp.userservice.model.Role;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.UserWallet;
import com.busapp.userservice.model.WalletTransaction;
import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalletMapper {
    private final UserRepository userRepository;

    public WalletResponse toResponse(UserWallet wallet) {
        User user = wallet.getUser();
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(user.getId());
        response.setUserName(user.getUserName());
        response.setFullName(user.getFullName());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus());
        response.setLastTransaction(wallet.getLastTransaction());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        response.setUser(toUserInfo(user));
        return response;
    }

    private WalletResponse.UserInfo toUserInfo(User user) {
        List<String> roleNames = user.getRoles() == null
                ? Collections.emptyList()
                : user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        return WalletResponse.UserInfo.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .image(user.getImage())
                .gender(user.getGender())
                .googleId(user.getGoogleId())
                .active(user.getActive())
                .isEmployee(user.getIsEmployee())
                .isDeleted(user.getIsDeleted())
                .isWalletExist(user.getIsWalletExist())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleNames)
                .build();
    }
    public WalletTransactionResponse toTransactionResponse(WalletTransaction tx) {
        WalletTransactionResponse response = new WalletTransactionResponse();
        response.setId(tx.getId());
        response.setWalletId(tx.getWallet().getId());
        response.setAmount(tx.getAmount());
        response.setType(tx.getType());
        response.setStatus(tx.getStatus());
        response.setReferenceId(tx.getReferenceId());
        response.setDescription(tx.getDescription());
        response.setBalanceBefore(tx.getBalanceBefore());
        response.setBalanceAfter(tx.getBalanceAfter());
        response.setMetadata(tx.getMetadata());
        response.setCreatedAt(tx.getCreatedAt());
        response.setCompletedAt(tx.getCompletedAt());
        return response;
    }
    public WalletTransactionDetailResponse toTransactionDetailResponse(WalletTransaction tx) {
        UserWallet wallet = tx.getWallet();
        User owner = wallet.getUser();

        return WalletTransactionDetailResponse.builder()
                .id(tx.getId())
                .referenceId(tx.getReferenceId())
                .type(tx.getType())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .balanceBefore(tx.getBalanceBefore())
                .balanceAfter(tx.getBalanceAfter())
                .description(tx.getDescription())
                .metadata(tx.getMetadata())
                .createdAt(tx.getCreatedAt())
                .completedAt(tx.getCompletedAt())
                .wallet(WalletTransactionDetailResponse.WalletInfo.builder()
                        .id(wallet.getId())
                        .currentBalance(wallet.getBalance())
                        .currency(wallet.getCurrency())
                        .status(wallet.getStatus())
                        .build())
                .owner(WalletTransactionDetailResponse.OwnerInfo.builder()
                        .userId(owner.getId())
                        .userName(owner.getUserName())
                        .fullName(owner.getFullName())
                        .email(owner.getEmail())
                        .phone(owner.getPhone())
                        .build())
                .build();
    }

    public UserWallet toEntity(CreateUserWalletRequest request){
        return UserWallet.builder()
                .user(request.getUser())
                .balance(request.getAmount())
                .currency(Currency.USD)
                .build();
    }
}
