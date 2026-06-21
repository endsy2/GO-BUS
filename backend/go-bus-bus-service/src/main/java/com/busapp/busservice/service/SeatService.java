package com.busapp.busservice.service;

import com.busapp.busservice.dto.SeatRequest;
import com.busapp.busservice.dto.SeatResponse;
import com.busapp.busservice.dto.SeatUpdateRequest;
import com.busapp.busservice.model.enums.SeatType;

import java.util.List;

public interface SeatService {
    List<SeatResponse> getSeatsByBus(Long busId);
    SeatResponse getSeatById(Long id);
    SeatResponse createSeat(SeatRequest request);
    void deleteSeat(Long id);

    // ── Admin ──────────────────────────────────────────────────────────────────

    List<SeatResponse> getSeatsByBusAndType(Long busId, SeatType type);

    SeatResponse getSeatByBusAndNumber(Long busId, String seatNumber);

    SeatResponse updateSeat(Long id, SeatUpdateRequest request);

    List<SeatResponse> createBulkSeats(Long busId, List<SeatRequest> requests);
}
