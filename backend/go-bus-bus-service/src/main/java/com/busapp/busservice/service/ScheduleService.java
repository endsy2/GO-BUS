package com.busapp.busservice.service;

import com.busapp.busservice.dto.ScheduleRequest;
import com.busapp.busservice.dto.ScheduleResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    List<ScheduleResponse> getAllSchedules();
    List<ScheduleResponse> getSchedulesByBus(Long busId);
    ScheduleResponse getScheduleById(Long id);
    ScheduleResponse createSchedule(ScheduleRequest request);
    ScheduleResponse updateSchedule(Long id, ScheduleRequest request);
    void deleteSchedule(Long id);

    // ── Admin ──────────────────────────────────────────────────────────────────

    Page<ScheduleResponse> getSchedulesWithSpecification(Long routeId,LocalDateTime from, LocalDateTime to,  Double maxPrice, Integer pageNo, Integer pageSize);
}
