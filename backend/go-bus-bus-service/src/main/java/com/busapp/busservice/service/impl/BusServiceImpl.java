package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.BusDetailResponse;
import com.busapp.busservice.dto.BusRequest;
import com.busapp.busservice.dto.BusResponse;
import com.busapp.busservice.exception.BadRequestException;
import com.busapp.busservice.exception.ResourceNotFoundException;
import com.busapp.busservice.mapper.BusMapper;
import com.busapp.busservice.model.*;
import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import com.busapp.busservice.model.enums.SeatStatus;
import com.busapp.busservice.model.enums.SeatType;
import com.busapp.busservice.model.repository.*;
import com.busapp.busservice.service.BusService;
import com.busapp.busservice.model.specification.BusSpecification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.Time;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final BusRouteRepository busRouteRepository;
    private final BusLayoutRepository busLayoutRepository;
    private final SeatRepository seatRepository;
    private final BusMapper busMapper;
    private final ObjectMapper objectMapper;
    private final BusScheduleRepository busScheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    @Override
    public List<BusResponse> getAllBuses() {
        return busRepository.findAll()
                .stream().map(busMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public BusDetailResponse getBusById(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));
        return busMapper.toResponseDetail(bus);
    }

    @Override
    public BusDetailResponse getBusByScheduleId(Long scheduleId) {
        BusSchedule schedule = busScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        Bus bus = schedule.getBus();
        if (bus == null) {
            throw new ResourceNotFoundException("Bus not found for schedule id: " + scheduleId);
        }

        return busMapper.toResponseDetail(bus);
    }

    @Override
    public List<BusResponse> getBusesByRoute(Long routeId) {
        return busRepository.findByRouteId(routeId)
                .stream().map(busMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BusResponse createBus(BusRequest request) {
        BusRoute route = busRouteRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + request.getRouteId()));

        BusLayout layout = null;
        int totalSeats = 0;

        if (request.getLayoutId() != null) {
            layout = busLayoutRepository.findById(request.getLayoutId())
                    .orElseThrow(() -> new ResourceNotFoundException("Layout not found: " + request.getLayoutId()));
            // Auto-calculate total seats from layout
            totalSeats = calculateTotalSeatsFromLayout(layout);
        } else {
            throw new BadRequestException("Layout ID is required to calculate total seats");
        }

        if (busRepository.findByPlate(request.getPlate()).isPresent()) {
            throw new com.busapp.busservice.exception.DuplicateResourceException("Bus plate already exists: " + request.getPlate());
        }
        if (busRepository.findByBusNumber(request.getBusNumber()).isPresent()) {
            throw new com.busapp.busservice.exception.DuplicateResourceException("Bus number already exists: " + request.getBusNumber());
        }

        Bus bus = busMapper.toEntity(request, route, layout);
        bus.setTotalSeats(totalSeats); // Set calculated total seats
        Bus savedBus = busRepository.save(bus);

        // Auto-create seats based on layout
        createSeatsFromLayout(savedBus, layout);

        return busMapper.toResponse(savedBus);
    }

    /**
     * Calculates total number of seats from layout JSON.
     * Counts all non-null seat entries in the layout.
     * <p>
     * Expected format:
     * {
     * "rows": 2,
     * "seats": [[{"seatNumber": "A1", "isAvailable": true}, ...], [...]],
     * "columns": 5,
     * "totalSeats": 10,
     * "aisleColumns": "3",
     * "driverColumn": 2
     * }
     */
    private int calculateTotalSeatsFromLayout(BusLayout layout) {
        try {
            log.debug("[BUS_SERVICE] Calculating total seats from layout - layoutId={}", layout.getId());

            // Parse as JsonNode
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(layout.getLayout());

            // Check if totalSeats field exists and use it directly
            if (rootNode.has("totalSeats")) {
                int totalSeats = rootNode.get("totalSeats").asInt();
                log.debug("[BUS_SERVICE] Using totalSeats from layout - layoutId={}, totalSeats={}",
                        layout.getId(), totalSeats);
                return totalSeats;
            }

            // Otherwise, count seats from the seats array
            if (!rootNode.has("seats")) {
                throw new BadRequestException("Layout JSON must have either 'totalSeats' or 'seats' field");
            }

            com.fasterxml.jackson.databind.JsonNode seatsArray = rootNode.get("seats");
            if (!seatsArray.isArray()) {
                throw new BadRequestException("'seats' field must be an array");
            }

            int count = 0;
            int rowNumber = 0;

            // Iterate through each row in the seats array
            for (com.fasterxml.jackson.databind.JsonNode rowNode : seatsArray) {
                rowNumber++;
                int rowSeats = 0;

                if (rowNode.isArray()) {
                    // Each row is an array of seat objects
                    for (com.fasterxml.jackson.databind.JsonNode seatNode : rowNode) {
                        if (seatNode != null && !seatNode.isNull() && seatNode.isObject()) {
                            // Check if seat has seatNumber field
                            if (seatNode.has("seatNumber")) {
                                String seatNumber = seatNode.get("seatNumber").asText();
                                if (seatNumber != null && !seatNumber.trim().isEmpty()) {
                                    count++;
                                    rowSeats++;
                                }
                            }
                        }
                    }
                }

                log.trace("[BUS_SERVICE] Row {} has {} seats (running total {})", rowNumber, rowSeats, count);
            }

            log.debug("[BUS_SERVICE] Total seats calculated - layoutId={}, totalSeats={}, rows={}",
                    layout.getId(), count, rowNumber);
            return count;
        } catch (Exception e) {
            log.error("[BUS_SERVICE] Failed to calculate seats from layout - layoutId={}, error={}",
                    layout.getId(), e.getMessage(), e);
            throw new BadRequestException("Failed to calculate seats from layout: " + e.getMessage());
        }
    }

    /**
     * Creates seats for a bus based on its layout configuration.
     * Parses the layout JSON and creates seat records.
     * <p>
     * Expected format:
     * {
     * "rows": 2,
     * "seats": [[{"seatNumber": "A1", "isAvailable": true}, ...], [...]],
     * "columns": 5,
     * "totalSeats": 10,
     * "aisleColumns": "3",
     * "driverColumn": 2
     * }
     */
    private void createSeatsFromLayout(Bus bus, BusLayout layout) {
        try {
            log.debug("[BUS_SERVICE] Creating seats from layout - busId={}", bus.getId());

            // Parse as JsonNode
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(layout.getLayout());

            if (!rootNode.has("seats")) {
                throw new BadRequestException("Layout JSON must have 'seats' field");
            }

            com.fasterxml.jackson.databind.JsonNode seatsArray = rootNode.get("seats");
            if (!seatsArray.isArray()) {
                throw new BadRequestException("'seats' field must be an array");
            }

            List<Seat> seats = new ArrayList<>();
            int rowNumber = 0;

            // Iterate through each row in the seats array
            for (com.fasterxml.jackson.databind.JsonNode rowNode : seatsArray) {
                rowNumber++;

                if (rowNode.isArray()) {
                    // Each row is an array of seat objects
                    for (com.fasterxml.jackson.databind.JsonNode seatNode : rowNode) {
                        if (seatNode == null || seatNode.isNull() || !seatNode.isObject()) {
                            continue;
                        }

                        // Check if seat has seatNumber field
                        if (!seatNode.has("seatNumber")) {
                            continue;
                        }

                        String seatNumber = seatNode.get("seatNumber").asText();
                        if (seatNumber == null || seatNumber.trim().isEmpty()) {
                            continue;
                        }

                        // Determine seat type based on naming convention
                        SeatType seatType = determineSeatType(seatNumber, bus.getBusType());

                        Seat seat = Seat.builder()
                                .bus(bus)
                                .seatNumber(seatNumber)
                                .seatType(seatType)
                                .build();

                        seats.add(seat);
                        log.trace("[BUS_SERVICE] Created seat - seatNumber={}, type={}", seatNumber, seatType);
                    }
                }
            }

            // Save all seats in batch
            if (!seats.isEmpty()) {
                seatRepository.saveAll(seats);
                log.debug("[BUS_SERVICE] Saved seats for bus - busId={}, seatCount={}", bus.getId(), seats.size());
            } else {
                log.warn("[BUS_SERVICE] No seats created for bus - busId={}", bus.getId());
            }

        } catch (Exception e) {
            log.error("[BUS_SERVICE] Failed to create seats from layout - busId={}, error={}",
                    bus.getId(), e.getMessage(), e);
            throw new BadRequestException("Failed to create seats from layout: " + e.getMessage());
        }
    }

    /**
     * Determines seat type based on seat number pattern and bus type.
     */
    private SeatType determineSeatType(String seatNumber, BusType busType) {
        if (busType == BusType.SLEEPER) {
            return SeatType.SLEEPER;
        }

        // Check if seat number contains indicators for VIP seats
        // Typically first row or seats with specific letters
//        if (seatNumber.startsWith("1") || seatNumber.contains("A")) {
//            return SeatType.VIP;
//        }

        return SeatType.SEATER;
    }

    @Override
    @Transactional
    public BusResponse updateBus(Long id, BusRequest request) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));

        BusRoute route = busRouteRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + request.getRouteId()));

        bus.setRoute(route);
        bus.setBusNumber(request.getBusNumber());
        bus.setPlate(request.getPlate());
        bus.setModel(request.getModel());
        bus.setBusType(request.getBusType());
        if (request.getBusStatus() != null) bus.setStatus(request.getBusStatus());

//        if (request.getLayoutId() != null) {
//            BusLayout layout = busLayoutRepository.findById(request.getLayoutId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Layout not found: " + request.getLayoutId()));
//            bus.setLayout(layout);
//
//            // Recalculate total seats from new layout
//            int totalSeats = calculateTotalSeatsFromLayout(layout);
//            bus.setTotalSeats(totalSeats);
//
//            // Delete old seats and create new ones based on new layout
//            seatRepository.deleteByBusId(id);
            Bus savedBus = busRepository.save(bus);
//            createSeatsFromLayout(savedBus, layout);
//
            return busMapper.toResponse(savedBus);
//        } else {
//            throw new BadRequestException("Layout ID is required to calculate total seats");
//        }
    }

    @Override
    @Transactional
    public void deleteBus(Long id) {
        if (!busRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bus not found with id: " + id);
        }
        if(!busScheduleRepository.findByDepartureDateTimeBetween(LocalDateTime.now(),LocalDateTime.now()).isEmpty()){
           throw new BadRequestException("Existing Schedule");
        }
        seatRepository.deleteByBusId(id);
        busRepository.deleteById(id);
    }

    // ── Admin ──────────────────────────────────────────────────────────────────

    @Override
    public BusResponse getBusByNumber(String busNumber) {
        Bus bus = busRepository.findByBusNumber(busNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with number: " + busNumber));
        return busMapper.toResponse(bus);
    }

    @Override
    public List<BusResponse> getBusesByRouteAndType(Long routeId, BusType type) {
        return busRepository.findByRouteIdAndBusType(routeId, type)
                .stream().map(busMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public Page<BusResponse> filterBuses(
            Integer pageNo,
            Integer pageSize,
            Long routeId,
            BusType busType,
            BusStatus status,
            String busNumber,
            String plate,
            Integer minSeats,
            Integer maxSeats) {
        PageRequest pageRequest=PageRequest.of(pageNo-1, pageSize);
        return busMapper.toPageResponse(busRepository.findAll(
                        BusSpecification.filter(routeId, busType, status, busNumber, plate, minSeats, maxSeats),pageRequest));
    }


    @Override
    public Integer getActiveBusCount() {
        // Count buses that are Active or InService
        long activeCount = busRepository.countByStatus(BusStatus.Active);
        long inServiceCount = busRepository.countByStatus(BusStatus.InService);
        return (int) (activeCount + inServiceCount);
    }
    
    @Override
    public Map<Long, String> getSeatTypesBatch(Set<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<Seat> seats = seatRepository.findAllById(seatIds);
        
        return seats.stream()
                .collect(Collectors.toMap(
                        Seat::getId,
                        seat -> seat.getSeatType().name()
                ));
    }

}
