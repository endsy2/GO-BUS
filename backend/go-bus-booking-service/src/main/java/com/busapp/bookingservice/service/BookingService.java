package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.request.BookingFilterRequest;
import com.busapp.bookingservice.dto.response.BookingDetailResponse;
import com.busapp.bookingservice.dto.request.BookingRequest;
import com.busapp.bookingservice.dto.response.BookingResponse;
import org.springframework.data.domain.Page;

public interface BookingService {

    BookingDetailResponse getBookingDetailById(Long id);

    Page<BookingResponse> filterBookings(BookingFilterRequest filter, int page, int size);

    BookingResponse createBooking(BookingRequest request, Long userId);

    BookingResponse cancelBooking(Long id);

    void deleteBooking(Long id);
}
