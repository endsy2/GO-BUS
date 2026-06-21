package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.dto.mapper.BookingMapper;
import com.busapp.bookingservice.dto.request.WalletPaymentBookingRequest;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.exception.BadRequestException;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.Ticket;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.PaymentRepository;
import com.busapp.bookingservice.repository.TicketRepository;
import com.busapp.bookingservice.service.WalletPaymentService;
import com.busapp.bookingservice.service.WebSocketService;
import com.busapp.bookingservice.util.UserUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletPaymentServiceImpl implements WalletPaymentService {
    private final BookingRepository bookingRepository;
    private final UserUtil userUtil;
    private final BookingMapper bookingMapper;
    private final UserClient userClient;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final WebSocketService webSocketService;
    private final BusClient busClient;

    @Override
    @Transactional
    public BookingResponse walletBookingPayment(Long bookingId, String walletSessionToken, WalletPaymentBookingRequest request) {
        log.info("[WALLET PAYMENT] Starting wallet payment - bookingId={}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking ID not found"));
        
        if (booking.getPaymentStatus().equals(PaymentStatus.SUCCESS)) {
            log.warn("[WALLET PAYMENT] Booking already paid - bookingId={}", bookingId);
            throw new BadRequestException("Booking Payment Already Completed");
        }
        
        Long currentUserId = userUtil.getCurrentUserId();
        
        // Verify booking belongs to current user
        if (!booking.getUserId().equals(currentUserId)) {
            log.error("[WALLET PAYMENT] User mismatch - bookingId={}, bookingUserId={}, currentUserId={}", 
                    bookingId, booking.getUserId(), currentUserId);
            throw new BadRequestException("You can only pay for your own bookings");
        }
        
        log.info("[WALLET PAYMENT] Processing wallet deduction - userId={}, amount={}", 
                currentUserId, booking.getTotalAmount());
        
        // Call user-service to process wallet payment (deduct balance) with wallet session validation
        UserClient.UserApiResponse<UserClient.WalletTransactionResponse> response = 
                Objects.requireNonNull(userClient.getWallet(
                        currentUserId,
                        walletSessionToken,
                        UserClient.TransactionType.PAYMENT,
                        booking.getTotalAmount().doubleValue()
                ).getBody());
        
        // Extract the actual transaction data from the ApiResponse wrapper
        UserClient.WalletTransactionResponse walletResponse = response.data();
        log.info("[WALLET PAYMENT] Wallet deduction successful - transactionId={}", walletResponse.id());

        // Update booking payment status + method (mirrored onto the booking so the
        // admin bookings filter and the payment-method badge can read it).
        booking.setPaymentStatus(PaymentStatus.SUCCESS);
        booking.setPaymentMethod(PaymentMethodType.WALLET);
        bookingRepository.save(booking);
        log.debug("[WALLET PAYMENT] Booking payment status updated to SUCCESS");

        // Update payment record
        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for booking"));
        payment.setMethod(PaymentMethodType.WALLET);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setDescription(request.getDescription());
        payment.setWalletTransactionId(walletResponse.walletId());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        log.debug("[WALLET PAYMENT] Payment record updated");

        // Create ticket
        String qrCode = "QR-" + bookingId + "-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();

        BusClient.ScheduleInfo schedule=busClient.getScheduleById(booking.getScheduleId()).getBody().data();


        Ticket ticket = Ticket.builder()
                .booking(booking)
                .qrCode(qrCode)
                .issuedAt(schedule.departureDateTime())
                .build();
        ticketRepository.save(ticket);
        
        log.info("[WALLET PAYMENT] Ticket created - ticketId={}, bookingId={}, qrCode={}", 
                ticket.getId(), bookingId, qrCode);
        
        log.info("[WALLET PAYMENT] Wallet payment completed successfully - bookingId={}", bookingId);
        BookingResponse bookingResponse = bookingMapper.toResponse(booking);

        // Broadcast the payment change to the admin bookings list (live update)
        webSocketService.broadcastBookingUpdate("PAYMENT_UPDATED", bookingResponse);

        return bookingResponse;
    }
}

