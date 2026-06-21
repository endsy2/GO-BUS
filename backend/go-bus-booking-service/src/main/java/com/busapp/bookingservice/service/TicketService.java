package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.request.TicketFilterRequest;
import com.busapp.bookingservice.dto.response.TicketDetailResponse;
import com.busapp.bookingservice.dto.response.TicketResponse;
import org.springframework.data.domain.Page;

public interface TicketService {
//    TicketResponse getTicketByBookingId(Long bookingId);
    TicketDetailResponse getTicketById(Long id);
    TicketResponse regenerateQr(Long bookingId);
    Page<TicketDetailResponse> filterTickets(TicketFilterRequest filter, int page, int size);
}
