package com.busapp.busservice.service.impl;

import com.busapp.busservice.client.BookingClient;
import com.busapp.busservice.dto.*;
import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.BusRoute;
import com.busapp.busservice.model.BusSchedule;
import com.busapp.busservice.model.repository.BusRepository;
import com.busapp.busservice.model.repository.BusRouteRepository;
import com.busapp.busservice.model.repository.BusScheduleRepository;
import com.busapp.busservice.service.BusReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusReportServiceImpl implements BusReportService {

    private final BusRepository busRepository;
    private final BusRouteRepository busRouteRepository;
    private final BusScheduleRepository busScheduleRepository;
    private final BookingClient bookingClient;

    @Override
    public List<BusUtilizationReportResponse> getBusUtilizationReport(LocalDate startDate, LocalDate endDate) {
        List<Bus> buses = busRepository.findAll();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return buses.stream().map(bus -> {
            List<BusSchedule> schedules = busScheduleRepository
                    .findByBusIdAndDepartureDateTimeBetween(bus.getId(), start, end);
            
            List<Long> scheduleIds = schedules.stream()
                    .map(BusSchedule::getId)
                    .collect(Collectors.toList());
            
            long totalBookings = 0;
            try {
                if (!scheduleIds.isEmpty()) {
                    totalBookings = bookingClient.getBookingCountBySchedules(scheduleIds);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch bookings for bus {}: {}", bus.getId(), e.getMessage());
            }

            double utilizationRate = schedules.isEmpty() ? 0.0 : 
                    (totalBookings * 100.0) / (schedules.size() * bus.getTotalSeats());

            return BusUtilizationReportResponse.builder()
                    .busId(bus.getId())
                    .busNumber(bus.getBusNumber())
                    .plate(bus.getPlate())
                    .model(bus.getModel())
                    .busType(bus.getBusType().name())
                    .routeName(bus.getRoute().getOrigin() + " - " + bus.getRoute().getDestination())
                    .totalSchedules(schedules.size())
                    .totalBookings(totalBookings)
                    .utilizationRate(Math.round(utilizationRate * 100.0) / 100.0)
                    .status(bus.getStatus().name())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<RoutePerformanceReportResponse> getRoutePerformanceReport(LocalDate startDate, LocalDate endDate) {
        List<BusRoute> routes = busRouteRepository.findAll();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return routes.stream().map(route -> {
            List<Bus> buses = busRepository.findByRouteId(route.getId());
            
            int totalSchedules = 0;
            long totalBookings = 0;
            double totalRevenue = 0.0;

            for (Bus bus : buses) {
                List<BusSchedule> schedules = busScheduleRepository
                        .findByBusIdAndDepartureDateTimeBetween(bus.getId(), start, end);
                totalSchedules += schedules.size();

                List<Long> scheduleIds = schedules.stream()
                        .map(BusSchedule::getId)
                        .collect(Collectors.toList());

                try {
                    if (!scheduleIds.isEmpty()) {
                        Long bookings = bookingClient.getBookingCountBySchedules(scheduleIds);
                        totalBookings += bookings != null ? bookings : 0;
                        
                        Double revenue = bookingClient.getRevenueBySchedules(scheduleIds);
                        totalRevenue += revenue != null ? revenue : 0.0;
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch data for bus {}: {}", bus.getId(), e.getMessage());
                }
            }

            double avgBookingsPerSchedule = totalSchedules > 0 ? 
                    (double) totalBookings / totalSchedules : 0.0;

            return RoutePerformanceReportResponse.builder()
                    .routeId(route.getId())
                    .origin(route.getOrigin())
                    .destination(route.getDestination())
                    .distanceKm(route.getDistanceKm())
                    .totalBuses(buses.size())
                    .totalSchedules(totalSchedules)
                    .totalBookings(totalBookings)
                    .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                    .averageBookingsPerSchedule(Math.round(avgBookingsPerSchedule * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<InactiveBusReportResponse> getInactiveBusReport(Integer daysThreshold) {
        List<Bus> buses = busRepository.findAll();
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysThreshold);
        List<InactiveBusReportResponse> inactiveBuses = new ArrayList<>();

        for (Bus bus : buses) {
            List<BusSchedule> schedules = busScheduleRepository.findByBusId(bus.getId());
            
            LocalDateTime lastScheduleDate = schedules.stream()
                    .map(BusSchedule::getDepartureDateTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            List<Long> scheduleIds = schedules.stream()
                    .map(BusSchedule::getId)
                    .collect(Collectors.toList());

            long totalBookings = 0;
            try {
                if (!scheduleIds.isEmpty()) {
                    totalBookings = bookingClient.getBookingCountBySchedules(scheduleIds);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch bookings for bus {}: {}", bus.getId(), e.getMessage());
            }

            String inactivityReason = null;
            if (schedules.isEmpty()) {
                inactivityReason = "No schedules created";
            } else if (lastScheduleDate != null && lastScheduleDate.isBefore(thresholdDate)) {
                inactivityReason = "No recent schedules (last: " + lastScheduleDate.toLocalDate() + ")";
            } else if (totalBookings == 0) {
                inactivityReason = "No bookings received";
            }

            if (inactivityReason != null) {
                inactiveBuses.add(InactiveBusReportResponse.builder()
                        .busId(bus.getId())
                        .busNumber(bus.getBusNumber())
                        .plate(bus.getPlate())
                        .model(bus.getModel())
                        .busType(bus.getBusType().name())
                        .routeName(bus.getRoute().getOrigin() + " - " + bus.getRoute().getDestination())
                        .totalSchedules(schedules.size())
                        .totalBookings(totalBookings)
                        .lastScheduleDate(lastScheduleDate)
                        .inactivityReason(inactivityReason)
                        .build());
            }
        }

        return inactiveBuses;
    }

    @Override
    public List<InactiveRouteReportResponse> getInactiveRouteReport(Integer daysThreshold) {
        List<BusRoute> routes = busRouteRepository.findAll();
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysThreshold);
        List<InactiveRouteReportResponse> inactiveRoutes = new ArrayList<>();

        for (BusRoute route : routes) {
            List<Bus> buses = busRepository.findByRouteId(route.getId());
            
            int totalSchedules = 0;
            long totalBookings = 0;
            LocalDateTime lastScheduleDate = null;
            List<Long> allScheduleIds = new ArrayList<>();

            for (Bus bus : buses) {
                List<BusSchedule> schedules = busScheduleRepository.findByBusId(bus.getId());
                totalSchedules += schedules.size();

                schedules.stream()
                        .map(BusSchedule::getId)
                        .forEach(allScheduleIds::add);

                LocalDateTime busLastSchedule = schedules.stream()
                        .map(BusSchedule::getDepartureDateTime)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                if (busLastSchedule != null && (lastScheduleDate == null || busLastSchedule.isAfter(lastScheduleDate))) {
                    lastScheduleDate = busLastSchedule;
                }
            }

            try {
                if (!allScheduleIds.isEmpty()) {
                    Long bookings = bookingClient.getBookingCountBySchedules(allScheduleIds);
                    totalBookings += bookings != null ? bookings : 0;
                }
            } catch (Exception e) {
                log.warn("Failed to fetch bookings for route {}: {}", route.getId(), e.getMessage());
            }

            String inactivityReason = null;
            if (buses.isEmpty()) {
                inactivityReason = "No buses assigned";
            } else if (totalSchedules == 0) {
                inactivityReason = "No schedules created";
            } else if (lastScheduleDate != null && lastScheduleDate.isBefore(thresholdDate)) {
                inactivityReason = "No recent schedules (last: " + lastScheduleDate.toLocalDate() + ")";
            } else if (totalBookings == 0) {
                inactivityReason = "No bookings received";
            }

            if (inactivityReason != null) {
                inactiveRoutes.add(InactiveRouteReportResponse.builder()
                        .routeId(route.getId())
                        .origin(route.getOrigin())
                        .destination(route.getDestination())
                        .distanceKm(route.getDistanceKm())
                        .totalBuses(buses.size())
                        .totalSchedules(totalSchedules)
                        .totalBookings(totalBookings)
                        .lastScheduleDate(lastScheduleDate)
                        .inactivityReason(inactivityReason)
                        .build());
            }
        }

        return inactiveRoutes;
    }

    @Override
    public List<BusCapacityAnalysisResponse> getBusCapacityAnalysis(LocalDate startDate, LocalDate endDate) {
        List<Bus> buses = busRepository.findAll();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return buses.stream().map(bus -> {
            List<BusSchedule> schedules = busScheduleRepository
                    .findByBusIdAndDepartureDateTimeBetween(bus.getId(), start, end);

            int fullyBookedCount = 0;
            long totalBookedSeats = 0;

            for (BusSchedule schedule : schedules) {
                try {
                    Map<String, Object> stats = bookingClient.getBookingStatsBySchedule(schedule.getId());
                    Integer bookedSeats = (Integer) stats.getOrDefault("bookedSeats", 0);
                    totalBookedSeats += bookedSeats;
                    
                    if (bookedSeats >= bus.getTotalSeats()) {
                        fullyBookedCount++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch stats for schedule {}: {}", schedule.getId(), e.getMessage());
                }
            }

            double fullyBookedPercentage = schedules.isEmpty() ? 0.0 : 
                    (fullyBookedCount * 100.0) / schedules.size();
            
            double averageOccupancyRate = schedules.isEmpty() ? 0.0 : 
                    (totalBookedSeats * 100.0) / (schedules.size() * bus.getTotalSeats());

            return BusCapacityAnalysisResponse.builder()
                    .busId(bus.getId())
                    .busNumber(bus.getBusNumber())
                    .plate(bus.getPlate())
                    .routeName(bus.getRoute().getOrigin() + " - " + bus.getRoute().getDestination())
                    .totalSeats(bus.getTotalSeats())
                    .totalSchedules(schedules.size())
                    .fullyBookedCount(fullyBookedCount)
                    .fullyBookedPercentage(Math.round(fullyBookedPercentage * 100.0) / 100.0)
                    .averageOccupancyRate(Math.round(averageOccupancyRate * 100.0) / 100.0)
                    .totalBookedSeats(totalBookedSeats)
                    .build();
        }).collect(Collectors.toList());
    }
}
