package com.busapp.bookingservice.dto.mapper;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

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

}
