package com.busapp.userservice.repository;

import com.busapp.userservice.model.WalletTransaction;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long>, JpaSpecificationExecutor<WalletTransaction> {
    List<WalletTransaction> findByWalletId(UUID walletId);
    List<WalletTransaction> findByWalletIdAndType(UUID walletId, TransactionType type);
    List<WalletTransaction> findByWalletIdAndStatus(UUID walletId, TransactionStatus status);
    Optional<WalletTransaction> findByReferenceId(String referenceId);
}
