package com.busapp.busservice.model.repository;

import com.busapp.busservice.model.Seat;
import com.busapp.busservice.model.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByBusId(Long busId);
    List<Seat> findByBusIdAndSeatType(Long busId, SeatType seatType);
    Optional<Seat> findByBusIdAndSeatNumber(Long busId, String seatNumber);
    
    @Modifying
    @Query("DELETE FROM Seat s WHERE s.bus.id = :busId")
    void deleteByBusId(@Param("busId") Long busId);
}
