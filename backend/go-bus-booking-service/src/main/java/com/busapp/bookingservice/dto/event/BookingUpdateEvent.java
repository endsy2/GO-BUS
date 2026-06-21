package com.busapp.bookingservice.dto.event;

import com.busapp.bookingservice.dto.response.BookingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Real-time event broadcast to the admin bookings list (topic /topic/admin/bookings).
 *
 * Unlike {@link DashboardUpdateEvent} (which carries aggregate counters), this event
 * carries the full {@link BookingResponse} row so the admin table can update a row
 * in place — or insert a new one — without re-fetching the whole page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingUpdateEvent {
    /** CREATED | PAYMENT_UPDATED | CANCELLED | UPDATED */
    private String action;
    private Long bookingId;
    private BookingResponse booking;
    private LocalDateTime timestamp;
}
