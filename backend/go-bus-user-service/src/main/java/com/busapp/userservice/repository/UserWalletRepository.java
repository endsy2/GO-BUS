package com.busapp.userservice.repository;

import com.busapp.userservice.model.UserWallet;
import com.busapp.userservice.model.enums.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, UUID>, JpaSpecificationExecutor<UserWallet> {
    Optional<UserWallet> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    Optional<UserWallet> findByUserIdAndStatus(Long userId, WalletStatus status);
}
