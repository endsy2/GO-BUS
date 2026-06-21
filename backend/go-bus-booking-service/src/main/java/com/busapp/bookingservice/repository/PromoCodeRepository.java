package com.busapp.bookingservice.repository;

import com.busapp.bookingservice.model.PromoCode;
import com.busapp.bookingservice.model.enums.PromoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCode(String code);
    List<PromoCode> findByStatus(PromoStatus status);
    List<PromoCode> findByStatusAndValidFromBeforeAndValidToAfter(
            PromoStatus status, LocalDateTime now1, LocalDateTime now2);
}
