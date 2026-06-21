package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.request.TicketFilterRequest;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.TicketDetailResponse;
import com.busapp.bookingservice.dto.response.TicketResponse;
import com.busapp.bookingservice.model.enums.TicketStatus;
import com.busapp.bookingservice.service.TicketService;
import com.busapp.bookingservice.util.UserUtil;
import com.thoughtworks.xstream.core.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserUtil userUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketDetailResponse>>> filterTickets(
            @RequestParam(required = false) Long bookingId,
//            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime issuedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime issuedTo,
            @RequestParam(required = false) TicketStatus ticketStatus,
            @RequestParam(value = "pageStart", defaultValue = "1") int pageStart,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        Long userId = userUtil.getCurrentUserId();
        TicketFilterRequest filter = TicketFilterRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .scheduleId(scheduleId)
                .issuedFrom(issuedFrom)
                .issuedTo(issuedTo)
                .ticketStatus(ticketStatus)
                .build();

        Page<TicketDetailResponse> page = ticketService.filterTickets(filter, pageStart - 1, pageSize);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Tickets retrieved successfully", page));
    }

//    @GetMapping("/booking/{bookingId}")
//    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByBookingId(
//            @PathVariable Long bookingId) {
//        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
//                "Ticket retrieved successfully",
//                ticketService.getTicketByBookingId(bookingId)));
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Ticket retrieved successfully",
                ticketService.getTicketById(id)));
    }

    @PostMapping("/booking/{bookingId}/regenerate")
    public ResponseEntity<ApiResponse<TicketResponse>> regenerateQr(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "QR code regenerated successfully",
                ticketService.regenerateQr(bookingId)));
    }
}
