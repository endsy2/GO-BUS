package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.ScheduleRequest;
import com.busapp.busservice.dto.ScheduleResponse;
import com.busapp.busservice.exception.ResourceNotFoundException;
import com.busapp.busservice.mapper.BusScheduleMapper;
import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.BusSchedule;
import com.busapp.busservice.model.specification.ScheduleSpecification;
import com.busapp.busservice.model.repository.BusRepository;
import com.busapp.busservice.model.repository.BusScheduleRepository;
import com.busapp.busservice.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final BusScheduleRepository scheduleRepository;
    private final BusRepository         busRepository;
    private final BusScheduleMapper     busScheduleMapper;
    private final com.busapp.busservice.service.ScheduleSeatService scheduleSeatService;


    @Override
    public List<ScheduleResponse> getAllSchedules() {
        return busScheduleMapper.toListReponseList(scheduleRepository.findAll());
    }

    @Override
    public List<ScheduleResponse> getSchedulesByBus(Long busId) {
        return busScheduleMapper.toListReponseList(scheduleRepository.findByBusId(busId));
    }

    @Override
    public ScheduleResponse getScheduleById(Long id) {
        return busScheduleMapper.toResponse(findSchedule(id));
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        log.info("[SCHEDULE] Creating schedule - busId={}, departure={}, price={}",
                request.getBusId(), request.getDepartureDateTime(), request.getPrice());

        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + request.getBusId()));

        BusSchedule schedule = BusSchedule.builder()
                .bus(bus)
                .price(request.getPrice())
                .departureDateTime(request.getDepartureDateTime())
                .arrivalDateTime(request.getArrivalDateTime())
                .build();
        BusSchedule savedSchedule = scheduleRepository.save(schedule);

        scheduleSeatService.initializeScheduleSeats(savedSchedule.getId());

        log.info("[SCHEDULE] Schedule created successfully - scheduleId={}", savedSchedule.getId());
        return busScheduleMapper.toResponse(savedSchedule);
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long id, ScheduleRequest request) {
        log.info("[SCHEDULE] Updating schedule - scheduleId={}", id);
        BusSchedule schedule = findSchedule(id);

        if (request.getBusId() != null && !request.getBusId().equals(schedule.getBus().getId())) {
            Bus bus = busRepository.findById(request.getBusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + request.getBusId()));
            schedule.setBus(bus);
        }
        if (request.getPrice() != null) schedule.setPrice(request.getPrice());
        if (request.getDepartureDateTime() != null) schedule.setDepartureDateTime(request.getDepartureDateTime());
        if (request.getArrivalDateTime() != null) schedule.setArrivalDateTime(request.getArrivalDateTime());

        ScheduleResponse updated = busScheduleMapper.toResponse(scheduleRepository.save(schedule));
        log.info("[SCHEDULE] Schedule updated - scheduleId={}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found: " + id);
        }
        log.info("[SCHEDULE] Deleting schedule - scheduleId={}", id);
        scheduleSeatService.deleteSeatsBySchedule(id);
        scheduleRepository.deleteById(id);
        log.info("[SCHEDULE] Schedule deleted - scheduleId={}", id);
    }

    // ── Admin ──────────────────────────────────────────────────────────────────

//    @Override
//    public List<ScheduleResponse> getSchedulesByDateRange(LocalDateTime from, LocalDateTime to) {
//        return scheduleRepository.findByDepartureDateBetween(from, to)
//                .stream().map(this::toResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ScheduleResponse> getSchedulesByBusAndDateRange(Long busId, LocalDateTime from, LocalDateTime to) {
//        return scheduleRepository.findByBusIdAndDepartureDateBetween(busId, from, to)
//                .stream().map(this::toResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ScheduleResponse> getSchedulesByMaxPrice(Double maxPrice) {
//        return scheduleRepository.findByPriceLessThanEqual(maxPrice)
//                .stream().map(this::toResponse).collect(Collectors.toList());
//    }

    @Override
    public Page<ScheduleResponse> getSchedulesWithSpecification(Long routeId, LocalDateTime from, LocalDateTime to, Double maxPrice, Integer pageNo, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        return busScheduleMapper.toPageResponse(
                scheduleRepository.findAll(ScheduleSpecification.filter(routeId, from, to, maxPrice), pageRequest));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BusSchedule findSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
    }


}
