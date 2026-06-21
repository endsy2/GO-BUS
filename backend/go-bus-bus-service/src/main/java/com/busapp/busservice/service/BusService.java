package com.busapp.busservice.service;

import com.busapp.busservice.dto.BusDetailResponse;
import com.busapp.busservice.dto.BusRequest;
import com.busapp.busservice.dto.BusResponse;
import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BusService {

    List<BusResponse> getAllBuses();

    BusDetailResponse getBusById(Long id);
    
    BusDetailResponse getBusByScheduleId(Long scheduleId);

    List<BusResponse> getBusesByRoute(Long routeId);

    BusResponse createBus(BusRequest request);

    BusResponse updateBus(Long id, BusRequest request);

    void deleteBus(Long id);

    // ── Admin ──────────────────────────────────────────────────────────────────

    BusResponse getBusByNumber(String busNumber);

    List<BusResponse> getBusesByRouteAndType(Long routeId, BusType type);

    Page<BusResponse> filterBuses(
            Integer pageNo,
            Integer pageSize,
            Long routeId,
            BusType busType,
            BusStatus status,
            String busNumber,
            String plate,
            Integer minSeats,
            Integer maxSeats);
    
    /**
     * Get count of active buses for dashboard statistics
     * @return Number of active buses
     */
    Integer getActiveBusCount();
    
    /**
     * Get seat types for multiple seats in batch
     * @param seatIds Set of seat IDs
     * @return Map of seat ID to seat type
     */
    java.util.Map<Long, String> getSeatTypesBatch(java.util.Set<Long> seatIds);
}

