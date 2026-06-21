package com.busapp.bookingservice.repository;

import com.busapp.bookingservice.model.PromoUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoUsageRepository extends JpaRepository<PromoUsage, Long> {
    List<PromoUsage> findByUserId(Long userId);
    List<PromoUsage> findByPromoId(Long promoId);
    Optional<PromoUsage> findByPromoIdAndBookingId(Long promoId, Long bookingId);
    boolean existsByPromoIdAndUserId(Long promoId, Long userId);
}
