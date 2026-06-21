package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.dto.request.RefundApprovalRequest;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.request.AdminBookingFilterRequest;
import com.busapp.bookingservice.dto.response.PagedResponse;
import com.busapp.bookingservice.dto.response.RefundResponse;
import com.busapp.bookingservice.dto.response.UserStatusResponse;
import com.busapp.bookingservice.exception.BadRequestException;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.dto.mapper.BookingMapper;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.Refund;
import com.busapp.bookingservice.model.Ticket;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.model.enums.RefundStatus;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.PaymentRepository;
import com.busapp.bookingservice.repository.RefundRepository;
import com.busapp.bookingservice.repository.spec.BookingSpecification;
import com.busapp.bookingservice.service.AdminBookingService;
import com.busapp.bookingservice.service.TelegramNotificationService;
import com.busapp.bookingservice.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet;

import java.awt.image.BufferStrategy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminBookingServiceImpl implements AdminBookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper     bookingMapper;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final TelegramNotificationService telegramNotificationService;
    private final BusClient busClient;
    private final WebSocketService webSocketService;
    private final UserClient userClient;
    // ── List / Filter ─────────────────────────────────────────────────────────

    @Override
    public Page<BookingResponse> getBookings(AdminBookingFilterRequest filter) {
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            UserClient.UserBasicInfo resp = userClient.getUserByUsername(filter.getUsername()).getBody().data();
            filter.setUserId(resp.id());
        }

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Booking> page = bookingRepository.findAll(
                BookingSpecification.from(filter), pageable);

        return page.map(bookingMapper::toResponse);
    }

    // ── Admin Actions ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = findBooking(id);
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Payment payment=paymentRepository.findByBookingId(booking.getId()).orElseThrow(()->new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        BookingResponse response = bookingMapper.toResponse(bookingRepository.save(booking));
        
        // Send Telegram notification for admin cancellation
        telegramNotificationService.sendBookingCancelledAlert(response, "Admin cancelled");

        // Broadcast the status change to the admin bookings list (live update)
        webSocketService.broadcastBookingUpdate("CANCELLED", response);

        return response;
    }

    @Override
    @Transactional
    public BookingResponse forceMarkPaid(Long id) {
        Booking booking = findBooking(id);
        booking.setPaymentStatus(PaymentStatus.SUCCESS);
        booking.setPaymentMethod(PaymentMethodType.ADMIN);
        BookingResponse response = bookingMapper.toResponse(bookingRepository.save(booking));
        Payment payment=paymentRepository.findByBookingId(booking.getId()).orElseThrow(()->new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setMethod(PaymentMethodType.ADMIN);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Ticket ticket=new Ticket();
        ticket.setBooking(booking);
        ticket.setIssuedAt(busClient.getScheduleById(booking.getScheduleId()).getBody().data().departureDateTime());
        
        // Send Telegram notification for force payment
        telegramNotificationService.sendAdminActionAlert("FORCE MARKED AS PAID", response, "Admin");

        // Broadcast the payment change to the admin bookings list (live update)
        webSocketService.broadcastBookingUpdate("PAYMENT_UPDATED", response);

        return response;
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = findBooking(id);
        booking.setIsDeleted(true);
        booking.setDeletedAt(java.time.LocalDateTime.now());
        bookingRepository.save(booking);
        
        // Send Telegram notification for deletion
        BookingResponse response = bookingMapper.toResponse(booking);
        telegramNotificationService.sendAdminActionAlert("BOOKING DELETED", response, "Admin");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    @Override
    public UserStatusResponse getUserLifetimeStats(Long userId) {

        try {
            // Native multi-column query returns one row as List<Object[]> (aggregates
            // without GROUP BY always yield exactly one row, even for users with no bookings).
            List<Object[]> rows = bookingRepository.getUserLifetimeStats(userId);
            if (rows == null || rows.isEmpty() || rows.get(0) == null) {
                return UserStatusResponse.builder()
                        .totalBookings(0L)
                        .totalSpent(0.0)
                        .activeTickets(0L)
                        .build();
            }

            // Convert via Number: COUNT -> Long, SUM(total_amount) -> BigDecimal (money column).
            Object[] row = rows.get(0);
            return UserStatusResponse.builder()
                    .totalBookings(row[0] == null ? 0L : ((Number) row[0]).longValue())
                    .totalSpent(row[1] == null ? 0.0 : ((Number) row[1]).doubleValue())
                    .activeTickets(row[2] == null ? 0L : ((Number) row[2]).longValue())
                    .build();

        } catch (Exception ex) {
            log.error("USER_STATS_FAILED - userId={}, error={}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to load user lifetime stats for userId=" + userId, ex);
        }
    }
}
