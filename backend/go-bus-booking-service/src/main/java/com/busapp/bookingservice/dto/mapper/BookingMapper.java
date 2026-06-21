package com.busapp.bookingservice.dto.mapper;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingMapper {
    private final UserClient userClient;
    private final BusClient busClient;

    /**
     * Convert booking to response with pre-fetched user and route data (optimized for batch operations)
     */
    public BookingResponse toResponse(Booking booking, String fullName, String destination) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setFullName(fullName != null ? fullName : "Unknown User");
        response.setDestination(destination != null ? destination : "Unknown Destination");
        response.setScheduleId(booking.getScheduleId());
        response.setBookingStatus(booking.getBookingStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setPromoId(booking.getPromo() != null ? booking.getPromo().getId() : null);
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setPaymentMethod(booking.getPaymentMethod());
        
        response.setRefundStatus(booking.getRefund() != null ? booking.getRefund().getStatus() : null);
        
        response.setCreatedAt(booking.getCreatedAt());
        response.setDepartureAt(booking.getDepartureAt());
        response.setIsDeleted(booking.getIsDeleted());
        response.setDeletedAt(booking.getDeletedAt());
        response.setPhoneNumber(booking.getPhoneNumber());
        return response;
    }

    /**
     * Convert booking to response (fetches user and route data - use for single booking operations)
     */
    public BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        
        try {
            var userResponse = userClient.getUsersByIds(Set.of(booking.getUserId())).getBody();
            if (userResponse != null && userResponse.data() != null && !userResponse.data().isEmpty()) {
                response.setFullName(userResponse.data().get(0).fullName());
            } else {
                response.setFullName("Unknown User");
            }
        } catch (Exception e) {
            response.setFullName("Unknown User");
        }
        
        try {
            response.setDestination(Objects.requireNonNull(
                    busClient.getRouteByScheduleId(booking.getScheduleId()).getBody()
            ).data().destination());
        } catch (Exception e) {
            response.setDestination("Unknown Destination");
        }
        
        response.setScheduleId(booking.getScheduleId());
        response.setBookingStatus(booking.getBookingStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setPromoId(booking.getPromo() != null ? booking.getPromo().getId() : null);
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setPaymentMethod(booking.getPaymentMethod());
        
        response.setRefundStatus(booking.getRefund() != null ? booking.getRefund().getStatus() : null);
        
        response.setCreatedAt(booking.getCreatedAt());
        response.setDepartureAt(booking.getDepartureAt());
        response.setIsDeleted(booking.getIsDeleted());
        response.setDeletedAt(booking.getDeletedAt());
        response.setPhoneNumber(booking.getPhoneNumber());
        return response;
    }

    /**
     * Convert list of bookings to responses using batch API calls (optimized for multiple bookings)
     */
    public List<BookingResponse> toListResponse(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return new ArrayList<>();
        }

        // Collect unique user IDs and schedule IDs
        Set<Long> userIds = bookings.stream()
                .map(Booking::getUserId)
                .collect(Collectors.toSet());
        
        Set<Long> scheduleIds = bookings.stream()
                .map(Booking::getScheduleId)
                .collect(Collectors.toSet());

        // Batch fetch users and routes
        var usersMap = fetchUsersMap(userIds);
        var routesMap = fetchRoutesMap(scheduleIds);

        // Map bookings to responses
        return bookings.stream()
                .map(booking -> {
                    String fullName = usersMap.getOrDefault(booking.getUserId(), "Unknown User");
                    String destination = routesMap.getOrDefault(booking.getScheduleId(), "Unknown Destination");
                    return toResponse(booking, fullName, destination);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, String> fetchUsersMap(Set<Long> userIds) {
        try {
            var response = userClient.getUsersByIds(userIds).getBody();
            if (response != null && response.data() != null) {
                return response.data().stream()
                        .collect(Collectors.toMap(
                                UserClient.UserBasicInfo::id,
                                UserClient.UserBasicInfo::fullName
                        ));
            }
        } catch (Exception e) {
            log.error("Failed to fetch users from userClient. userIds={}", userIds, e);
        }
        return new java.util.HashMap<>();
    }

    private Map<Long, String> fetchRoutesMap(Set<Long> scheduleIds) {
        try {
            var response = busClient.getRoutesWithScheduleMapping(scheduleIds).getBody();
            if (response != null && response.data() != null) {
                return response.data().stream()
                        .collect(Collectors.toMap(
                                BusClient.ScheduleRouteInfo::scheduleId,
                                info -> info.route().destination()
                        ));
            }
        } catch (Exception e) {
            log.error("Failed to fetch routes from scheduleIds. routes={}", scheduleIds, e);
        }
        return new java.util.HashMap<>();
    }
}
