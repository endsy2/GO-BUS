package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.SeatRequest;
import com.busapp.busservice.dto.SeatResponse;
import com.busapp.busservice.dto.SeatUpdateRequest;
import com.busapp.busservice.exception.ResourceNotFoundException;
import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.Seat;
import com.busapp.busservice.model.enums.SeatType;
import com.busapp.busservice.model.repository.BusRepository;
import com.busapp.busservice.model.repository.SeatRepository;
import com.busapp.busservice.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final BusRepository  busRepository;

    @Override
    public List<SeatResponse> getSeatsByBus(Long busId) {
        return seatRepository.findByBusId(busId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SeatResponse getSeatById(Long id) {
        return toResponse(findSeat(id));
    }

    @Override
    @Transactional
    public SeatResponse createSeat(SeatRequest request) {
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + request.getBusId()));

        Seat seat = Seat.builder()
                .bus(bus)
                .seatNumber(request.getSeatNumber())
                .seatType(request.getSeatType())
                .build();
        return toResponse(seatRepository.save(seat));
    }

    @Override
    @Transactional
    public void deleteSeat(Long id) {
        if (!seatRepository.existsById(id)) {
            throw new ResourceNotFoundException("Seat not found: " + id);
        }
        seatRepository.deleteById(id);
    }

    // ── Admin ──────────────────────────────────────────────────────────────────

    @Override
    public List<SeatResponse> getSeatsByBusAndType(Long busId, SeatType type) {
        return seatRepository.findByBusIdAndSeatType(busId, type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SeatResponse getSeatByBusAndNumber(Long busId, String seatNumber) {
        Seat seat = seatRepository.findByBusIdAndSeatNumber(busId, seatNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seat not found on bus " + busId + " with number: " + seatNumber));
        return toResponse(seat);
    }

    @Override
    @Transactional
    public SeatResponse updateSeat(Long id, SeatUpdateRequest request) {
        Seat seat = findSeat(id);
        if (request.getSeatNumber() != null) seat.setSeatNumber(request.getSeatNumber());
        if (request.getSeatType() != null) seat.setSeatType(request.getSeatType());
        return toResponse(seatRepository.save(seat));
    }

    @Override
    @Transactional
    public List<SeatResponse> createBulkSeats(Long busId, List<SeatRequest> requests) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + busId));
        List<Seat> seats = requests.stream()
                .map(r -> Seat.builder()
                        .bus(bus)
                        .seatNumber(r.getSeatNumber())
                        .seatType(r.getSeatType())
                        .build())
                .collect(Collectors.toList());
        return seatRepository.saveAll(seats).stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Seat findSeat(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + id));
    }

    private SeatResponse toResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .busId(seat.getBus() != null ? seat.getBus().getId() : null)
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .status(null) // Status is now tracked in ScheduleSeat
                .bookings(null) // Bookings are now tracked in ScheduleSeat
                .build();
    }
}
