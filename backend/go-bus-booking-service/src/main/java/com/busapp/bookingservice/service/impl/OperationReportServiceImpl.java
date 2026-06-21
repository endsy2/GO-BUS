package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.dto.response.*;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.BookingSeatRepository;
import com.busapp.bookingservice.service.OperationReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationReportServiceImpl implements OperationReportService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BusClient busClient;

    @Override
    public BookingReportResponse getBookingReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Long totalBookings = bookingRepository.countBookingsByDateRange(startDateTime, endDateTime);
        
        List<Object[]> statusCounts = bookingRepository.countBookingsByStatusAndDateRange(startDateTime, endDateTime);
        Map<String, Long> bookingsByStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        arr -> ((BookingStatus) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));

        return BookingReportResponse.builder()
                .totalBookings(totalBookings)
                .confirmedBookings(bookingsByStatus.getOrDefault("CONFIRMED", 0L))
                .pendingBookings(bookingsByStatus.getOrDefault("PENDING", 0L))
                .cancelledBookings(bookingsByStatus.getOrDefault("CANCELLED", 0L))
                .bookingsByStatus(bookingsByStatus)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public List<RouteRevenueReportResponse> getRouteRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> revenueData = bookingRepository.getRevenueBySchedule(startDateTime, endDateTime);
        List<Object[]> statusData = bookingRepository.getBookingStatusCountsBySchedule(startDateTime, endDateTime);
        
        Map<Long, RouteRevenueData> routeRevenueMap = new HashMap<>();
        Map<Long, Map<BookingStatus, Long>> scheduleStatusMap = new HashMap<>();

        // Process status counts by schedule
        for (Object[] row : statusData) {
            Long scheduleId = (Long) row[0];
            BookingStatus status = (BookingStatus) row[1];
            Long count = (Long) row[2];
            
            scheduleStatusMap.computeIfAbsent(scheduleId, k -> new HashMap<>())
                    .put(status, count);
        }

        for (Object[] row : revenueData) {
            Long scheduleId = (Long) row[0];
            Long bookingCount = (Long) row[1];
            Double revenue = (Double) row[2];

            try {
                var scheduleResp = busClient.getScheduleById(scheduleId);
                if (scheduleResp.getBody() != null && scheduleResp.getBody().data() != null) {
                    var schedule = scheduleResp.getBody().data();
                    
                    // Fetch bus to get route information
                    var busResp = busClient.getBusById(schedule.busId());
                    if (busResp.getBody() != null && busResp.getBody().data() != null) {
                        var bus = busResp.getBody().data();
                        Long routeId = bus.route().id();
                        
                        // Fetch route to get route name
                        String routeName = bus.route().origin()+" - "+bus.route().destination();
//                        try {
//                            var routeResp = busClient.getRouteById(routeId);
//                            if (routeResp.getBody() != null && routeResp.getBody().data() != null) {
//                                var route = routeResp.getBody().data();
//                                routeName = route.origin() + " - " + route.destination();
//                            }
//                        } catch (Exception e) {
//                            log.warn("Could not fetch route name for routeId {}", routeId);
//                        }
                        log.info("Route name = {}", routeName);

                        RouteRevenueData data = routeRevenueMap.getOrDefault(routeId, 
                            new RouteRevenueData(routeId, routeName, 0L, 0L, 0L, 0.0, 0L));
                        
                        Map<BookingStatus, Long> statusCounts = scheduleStatusMap.getOrDefault(scheduleId, new HashMap<>());
                        data.confirmedBookings += statusCounts.getOrDefault(BookingStatus.CONFIRMED, 0L);
                        data.cancelledBookings += statusCounts.getOrDefault(BookingStatus.CANCELLED, 0L);
                        data.totalBookings += bookingCount;
                        data.totalRevenue += revenue;
                        
                        Long ticketsSold = bookingSeatRepository.countTicketsSoldByScheduleAndDateRange(
                            scheduleId, startDateTime, endDateTime);
                        data.totalTicketsSold += ticketsSold;

                        routeRevenueMap.put(routeId, data);
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching schedule {} for revenue report: {}", scheduleId, e.getMessage());
            }
        }

        return routeRevenueMap.values().stream()
                .map(data -> RouteRevenueReportResponse.builder()
                        .routeId(data.routeId)
                        .routeName(data.routeName)
                        .totalBookings(data.totalBookings)
                        .confirmedBookings(data.confirmedBookings)
                        .cancelledBookings(data.cancelledBookings)
                        .totalRevenue(data.totalRevenue)
                        .totalTicketsSold(data.totalTicketsSold)
                        .build())
                .sorted(Comparator.comparing(RouteRevenueReportResponse::getTotalRevenue).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<PopularRouteReportResponse> getPopularRoutesReport(LocalDate startDate, LocalDate endDate, Integer limit) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> popularSchedules = bookingRepository.getPopularSchedules(startDateTime, endDateTime);
        List<Object[]> statusData = bookingRepository.getBookingStatusCountsBySchedule(startDateTime, endDateTime);
        
        Map<Long, PopularRouteData> routePopularityMap = new HashMap<>();
        Map<Long, Map<BookingStatus, Long>> scheduleStatusMap = new HashMap<>();

        // Process status counts by schedule
        for (Object[] row : statusData) {
            Long scheduleId = (Long) row[0];
            BookingStatus status = (BookingStatus) row[1];
            Long count = (Long) row[2];
            
            scheduleStatusMap.computeIfAbsent(scheduleId, k -> new HashMap<>())
                    .put(status, count);
        }

        for (Object[] row : popularSchedules) {
            Long scheduleId = (Long) row[0];
            Long bookingCount = (Long) row[1];

            try {
                var scheduleResp = busClient.getScheduleById(scheduleId);
                if (scheduleResp.getBody() != null && scheduleResp.getBody().data() != null) {
                    var schedule = scheduleResp.getBody().data();
                    
                    // Fetch bus to get route and seat information
                    var busResp = busClient.getBusById(schedule.busId());
                    if (busResp.getBody() != null && busResp.getBody().data() != null) {
                        var bus = busResp.getBody().data();
                        Long routeId = bus.route().id();
                        Integer totalSeats = bus.totalSeats();
                        
                        // Fetch route to get route name
                        String routeName = bus.route().origin() + " - " + bus.route().destination();
//                        try {
//                            var routeResp = busClient.getRouteById(routeId);
//                            if (routeResp.getBody() != null && routeResp.getBody().data() != null) {
//                                var route = routeResp.getBody().data();
//                                routeName = route.name() != null ? route.name() :
//                                           route.origin() + " - " + route.destination();
//                            }
//                        } catch (Exception e) {
//                            log.warn("Could not fetch route name for routeId {}", routeId);
//                        }

                        PopularRouteData data = routePopularityMap.getOrDefault(routeId, 
                            new PopularRouteData(routeId, routeName, 0L, 0L, 0L, 0L, 0, 0));
                        
                        Map<BookingStatus, Long> statusCounts = scheduleStatusMap.getOrDefault(scheduleId, new HashMap<>());
                        data.confirmedBookings += statusCounts.getOrDefault(BookingStatus.CONFIRMED, 0L);
                        data.cancelledBookings += statusCounts.getOrDefault(BookingStatus.CANCELLED, 0L);
                        data.totalBookings += bookingCount;
                        
                        Long ticketsSold = bookingSeatRepository.countTicketsSoldByScheduleAndDateRange(
                            scheduleId, startDateTime, endDateTime);
                        data.totalTicketsSold += ticketsSold;
                        
                        Long bookedSeats = bookingSeatRepository.countBookedSeatsBySchedule(scheduleId);
                        data.totalBookedSeats += bookedSeats;
                        data.totalSeats += totalSeats;

                        routePopularityMap.put(routeId, data);
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching schedule {} for popular routes report: {}", scheduleId, e.getMessage());
            }
        }

        return routePopularityMap.values().stream()
                .map(data -> {
                    double occupancyRate = data.totalSeats > 0 
                        ? (data.totalBookedSeats * 100.0) / data.totalSeats 
                        : 0.0;
                    
                    return PopularRouteReportResponse.builder()
                            .routeId(data.routeId)
                            .routeName(data.routeName)
                            .totalBookings(data.totalBookings)
                            .confirmedBookings(data.confirmedBookings)
                            .cancelledBookings(data.cancelledBookings)
                            .totalTicketsSold(data.totalTicketsSold)
                            .averageOccupancyRate(Math.round(occupancyRate * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(PopularRouteReportResponse::getTotalBookings).reversed())
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatOccupancyReportResponse> getSeatOccupancyReport(Long scheduleId) {
        List<SeatOccupancyReportResponse> reports = new ArrayList<>();

        try {
            if (scheduleId != null) {
                SeatOccupancyReportResponse report = calculateOccupancyForSchedule(scheduleId);
                if (report != null) {
                    reports.add(report);
                }
            } else {
                List<Booking> allBookings = bookingRepository.findAll();
                Set<Long> scheduleIds = allBookings.stream()
                        .map(Booking::getScheduleId)
                        .collect(Collectors.toSet());

                for (Long sid : scheduleIds) {
                    SeatOccupancyReportResponse report = calculateOccupancyForSchedule(sid);
                    if (report != null) {
                        reports.add(report);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error generating seat occupancy report: {}", e.getMessage());
        }

        return reports;
    }

    @Override
    public List<TicketSalesReportResponse> getTicketSalesReport(LocalDate startDate, LocalDate endDate, 
                                                                 Long routeId, Long busId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findBookingsByDateRange(startDateTime, endDateTime);
        
        Map<Long, TicketSalesData> salesMap = new HashMap<>();

        for (Booking booking : bookings) {
            if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
                continue;
            }

            try {
                var scheduleResp = busClient.getScheduleById(booking.getScheduleId());
                if (scheduleResp.getBody() != null && scheduleResp.getBody().data() != null) {
                    var schedule = scheduleResp.getBody().data();
                    
                    // Fetch bus to get route information
                    var busResp = busClient.getBusById(schedule.busId());
                    if (busResp.getBody() != null && busResp.getBody().data() != null) {
                        var bus = busResp.getBody().data();
                        Long currentRouteId = bus.route().id();
                        Long currentBusId = bus.id();

                        if ((routeId == null || routeId.equals(currentRouteId)) &&
                            (busId == null || busId.equals(currentBusId))) {
                            
                            Long scheduleId = booking.getScheduleId();
                            TicketSalesData data = salesMap.getOrDefault(scheduleId, 
                                new TicketSalesData(scheduleId, currentRouteId, currentBusId, 0L, 0.0));
                            
                            Long ticketsSold = (long) booking.getSeats().size();
                            data.totalTicketsSold += ticketsSold;
                            data.totalRevenue += booking.getTotalAmount().doubleValue();

                            salesMap.put(scheduleId, data);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing booking {} for ticket sales report: {}", 
                    booking.getId(), e.getMessage());
            }
        }

        return salesMap.values().stream()
                .map(data -> TicketSalesReportResponse.builder()
                        .scheduleId(data.scheduleId)
                        .routeId(data.routeId)
                        .busId(data.busId)
                        .totalTicketsSold(data.totalTicketsSold)
                        .totalRevenue(data.totalRevenue)
                        .build())
                .sorted(Comparator.comparing(TicketSalesReportResponse::getTotalTicketsSold).reversed())
                .collect(Collectors.toList());
    }

    private SeatOccupancyReportResponse calculateOccupancyForSchedule(Long scheduleId) {
        try {
            var scheduleResp = busClient.getScheduleById(scheduleId);
            if (scheduleResp.getBody() != null && scheduleResp.getBody().data() != null) {
                var schedule = scheduleResp.getBody().data();
                
                // Fetch bus to get route and seat information
                var busResp = busClient.getBusById(schedule.busId());
                if (busResp.getBody() != null && busResp.getBody().data() != null) {
                    var bus = busResp.getBody().data();
                    Long routeId = bus.route().id();
                    Long busId = bus.id();
                    Integer totalSeats = bus.totalSeats();

                    Long bookedSeats = bookingSeatRepository.countBookedSeatsBySchedule(scheduleId);
                    double occupancyRate = totalSeats > 0 
                        ? (bookedSeats * 100.0) / totalSeats 
                        : 0.0;

                    return SeatOccupancyReportResponse.builder()
                            .scheduleId(scheduleId)
                            .routeId(routeId)
                            .busId(busId)
                            .totalSeats(totalSeats)
                            .bookedSeats(bookedSeats.intValue())
                            .occupancyRate(Math.round(occupancyRate * 100.0) / 100.0)
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error calculating occupancy for schedule {}: {}", scheduleId, e.getMessage());
        }
        return null;
    }

    // Helper classes for data aggregation
    private static class RouteRevenueData {
        Long routeId;
        String routeName;
        Long totalBookings;
        Long confirmedBookings;
        Long cancelledBookings;
        Double totalRevenue;
        Long totalTicketsSold;

        RouteRevenueData(Long routeId, String routeName, Long totalBookings, 
                        Long confirmedBookings, Long cancelledBookings,
                        Double totalRevenue, Long totalTicketsSold) {
            this.routeId = routeId;
            this.routeName = routeName;
            this.totalBookings = totalBookings;
            this.confirmedBookings = confirmedBookings;
            this.cancelledBookings = cancelledBookings;
            this.totalRevenue = totalRevenue;
            this.totalTicketsSold = totalTicketsSold;
        }
    }

    private static class PopularRouteData {
        Long routeId;
        String routeName;
        Long totalBookings;
        Long confirmedBookings;
        Long cancelledBookings;
        Long totalTicketsSold;
        long totalBookedSeats;
        int totalSeats;

        PopularRouteData(Long routeId, String routeName, Long totalBookings, 
                        Long confirmedBookings, Long cancelledBookings,
                        Long totalTicketsSold, long totalBookedSeats, int totalSeats) {
            this.routeId = routeId;
            this.routeName = routeName;
            this.totalBookings = totalBookings;
            this.confirmedBookings = confirmedBookings;
            this.cancelledBookings = cancelledBookings;
            this.totalTicketsSold = totalTicketsSold;
            this.totalBookedSeats = totalBookedSeats;
            this.totalSeats = totalSeats;
        }
    }

    private static class TicketSalesData {
        Long scheduleId;
        Long routeId;
        Long busId;
        Long totalTicketsSold;
        Double totalRevenue;

        TicketSalesData(Long scheduleId, Long routeId, Long busId, 
                       Long totalTicketsSold, Double totalRevenue) {
            this.scheduleId = scheduleId;
            this.routeId = routeId;
            this.busId = busId;
            this.totalTicketsSold = totalTicketsSold;
            this.totalRevenue = totalRevenue;
        }
    }
}
