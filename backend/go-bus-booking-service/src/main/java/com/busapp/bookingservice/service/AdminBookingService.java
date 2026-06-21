package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.request.AdminBookingFilterRequest;
import com.busapp.bookingservice.dto.response.UserStatusResponse;
import org.springframework.data.domain.Page;

public interface AdminBookingService {
    Page<BookingResponse> getBookings(AdminBookingFilterRequest filter);
    BookingResponse cancelBooking(Long id);
    BookingResponse forceMarkPaid(Long id);
    void deleteBooking(Long id);
    UserStatusResponse getUserLifetimeStats (Long userId);
}
