package com.busapp.userservice.repository;

import com.busapp.userservice.model.TopUp;
import com.busapp.userservice.model.enums.TopUpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopUpRepository extends JpaRepository<TopUp, Long> {
    List<TopUp> findByUserId(Long userId);
    List<TopUp> findByUserIdAndStatus(Long userId, TopUpStatus status);
    Optional<TopUp> findByTransactionId(String transactionId);
}
