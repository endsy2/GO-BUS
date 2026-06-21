package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.config.BakongConfig;
import com.busapp.bookingservice.dto.event.DashboardUpdateEvent;
import com.busapp.bookingservice.dto.event.PaymentStatusEvent;
import com.busapp.bookingservice.dto.mapper.BookingMapper;
import com.busapp.bookingservice.dto.mapper.PaymentMapper;
import com.busapp.bookingservice.dto.request.BakongRequest;
import com.busapp.bookingservice.dto.request.CheckTransactionRequest;
import com.busapp.bookingservice.dto.response.BakongCheckTopUpResponse;
import com.busapp.bookingservice.dto.response.BakongQrResponse;
import com.busapp.bookingservice.dto.response.BakongResponse;
import com.busapp.bookingservice.exception.*;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.Ticket;
import com.busapp.bookingservice.model.enums.Currency;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.PaymentRepository;
import com.busapp.bookingservice.repository.TicketRepository;
import com.busapp.bookingservice.service.TelegramNotificationService;
import com.busapp.bookingservice.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BakongServiceImpl implements BakongService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BakongConfig bakongConfig;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final BookingMapper bookingMapper;
    private final TicketRepository ticketRepository;
    private final TelegramNotificationService telegramNotificationService;
    private final UserClient userClient;
    private final BusClient busClient;
    private final WebSocketService webSocketService;

    @Value("${bakong.account-id}")
    private String bakongAccountId;

    @Value("${bakong.acquiring-bank}")
    private String acquiringBank;

    @Value("${bakong.merchant-name}")
    private String merchantName;

    @Value("${bakong.mobile-number}")
    private String mobileNumber;

    @Value("${bakong.store-label}")
    private String storeLabel;

    @Value("${bakong.base-url}")
    private String baseUrl;

    // ─────────────────────────────────────────────
    // 1. Generate KHQR
    // ─────────────────────────────────────────────

    @Override
    public BakongResponse
    generateKhqr(BakongRequest bakongRequest) {
        log.info("PAYMENT_INITIATED", kv("method", "BAKONG_KHQR"), kv("bookingId", bakongRequest.getBookingId()),
                kv("currency", bakongRequest.getCurrency()));

        try {
            // Validate booking exists
            Booking booking = bookingRepository.findById(bakongRequest.getBookingId())
                    .orElseThrow(() -> {
                        log.error("[BAKONG] Booking not found for KHQR generation - bookingId={}",
                                bakongRequest.getBookingId());
                        return new ResourceNotFoundException("Booking not found with ID: " + bakongRequest.getBookingId());
                    });

            log.debug("[BAKONG] Found booking - bookingId={}, totalAmount={}, paymentStatus={}",
                    booking.getId(), booking.getTotalAmount(), booking.getPaymentStatus());

            // Check if payment already exists and is completed
            if (booking.getPayment() != null &&
                    booking.getPayment().getStatus() == PaymentStatus.SUCCESS) {
                log.warn("[BAKONG] Payment already completed - bookingId={}, paymentId={}",
                        bakongRequest.getBookingId(), booking.getPayment().getId());
                throw new InvalidPaymentStateException(
                        "Payment already completed for booking ID: " + bakongRequest.getBookingId());
            }

            // Validate booking amount
            if (booking.getTotalAmount() == null || booking.getTotalAmount().doubleValue() <= 0) {
                log.error("[BAKONG] Invalid booking amount - bookingId={}, amount={}",
                        bakongRequest.getBookingId(), booking.getTotalAmount());
                throw new BadRequestException("Invalid booking amount");
            }

            long deadline = System.currentTimeMillis() + bakongConfig.getConnectionTimeout();

            double amount = bakongRequest.getCurrency().equals(Currency.USD)
                    ? booking.getTotalAmount().doubleValue()
                    : (booking.getTotalAmount().doubleValue() * 4000);

            log.debug("[BAKONG] Calculated amount - currency={}, amount={}", bakongRequest.getCurrency(), amount);

            String urlTemple = baseUrl.replaceAll("/+$", "") + "/bakong/generateQR";

            String url = UriComponentsBuilder
                    .fromHttpUrl(urlTemple)
                    .queryParam("amount", amount)
                    .queryParam("currency", bakongRequest.getCurrency())
                    .queryParam("merchant_name", merchantName)
                    .queryParam("bank_account", bakongAccountId)
                    .queryParam("number_phone", mobileNumber)
                    .toUriString();

            log.debug("[BAKONG] Calling QR endpoint - amount={}, url={}", amount, url);

            BakongResponse response = restTemplate.getForObject(url, BakongResponse.class);

            // Validate response
            if (response == null || response.getData() == null) {
                log.error("[BAKONG] KHQR generation returned null response - bookingId={}",
                        bakongRequest.getBookingId());
                throw new QRGenerationException("Failed to generate KHQR: null response from Bakong library");
            }
            log.debug("[BAKONG] KHQR raw response received");
            log.info("QR_GENERATED", kv("bookingId", bakongRequest.getBookingId()), kv("amount", amount),
                    kv("currency", bakongRequest.getCurrency()));

            return response;

        } catch (ResourceNotFoundException | InvalidPaymentStateException | BadRequestException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG] Unexpected error during KHQR generation - bookingId={}, error={}",
                    bakongRequest.getBookingId(), e.getMessage(), e);
            throw new QRGenerationException(
                    "Failed to generate KHQR for booking ID: " + bakongRequest.getBookingId(), e);
        }
    }

    // ─────────────────────────────────────────────
    // 2. Poll Bakong until SUCCESS / FAILED / timeout
    // ─────────────────────────────────────────────

    @Override
    public BakongResponse checkingTransaction(Long bookingId, CheckTransactionRequest checkTransactionRequest) {
        log.info("[BAKONG] Starting transaction check - bookingId={}, md5={}", bookingId, checkTransactionRequest.getMd5());

        try {
            // Validate inputs
            if (bookingId == null || bookingId <= 0) {
                throw new BadRequestException("Invalid booking ID");
            }

            if (checkTransactionRequest.getMd5() == null || checkTransactionRequest.getMd5().isBlank()) {
                throw new BadRequestException("MD5 hash is required");
            }

            // Validate booking exists
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG] Booking not found - bookingId={}", bookingId);
                        return new ResourceNotFoundException("Booking not found with ID: " + bookingId);
                    });

            long deadline = System.currentTimeMillis() + bakongConfig.getConnectionTimeout();
            log.debug("[BAKONG] Polling deadline epoch={}", deadline);
            Long pollIntervalMs = bakongConfig.getPollingIntervalMs();

            log.debug("[BAKONG] Polling configuration - timeout={}ms, interval={}ms",
                    bakongConfig.getConnectionTimeout(), pollIntervalMs);

            // Pre-build URL and headers outside the polling loop
            String url = baseUrl.replaceAll("/+$", "") + "/bakong/verifyMD5";
            log.debug("[BAKONG] Transaction check URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, String> body = Map.of("md5", checkTransactionRequest.getMd5());
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            // Get or create payment record ONCE before polling loop
            log.debug("[BAKONG] Getting or creating payment record");
            Payment payment;
            try {
                payment = getOrCreatePayment(bookingId, checkTransactionRequest.getMd5());
            } catch (Exception e) {
                log.error("[BAKONG] Failed to create payment record - bookingId={}, error={}",
                        bookingId, e.getMessage(), e);
                throw new TransactionCheckException("Failed to initialize payment record", e);
            }

            // Broadcast PENDING status via WebSocket
            log.debug("[BAKONG] Broadcasting PAYMENT_PENDING event");
            broadcastPaymentEvent("PAYMENT_PENDING", payment, booking, null, null);

            // Polling loop
            int pollCount = 0;
            int maxRetries = 3;
            int consecutiveErrors = 0;

            log.info("[BAKONG] Starting polling loop - bookingId={}, md5={}", bookingId, checkTransactionRequest.getMd5());

            while (System.currentTimeMillis() < deadline) {
                pollCount++;
                log.debug("[BAKONG] Poll attempt #{} - md5={}", pollCount, checkTransactionRequest.getMd5());

                try {
                    ResponseEntity<String> upstream = restTemplate.exchange(
                            url, HttpMethod.POST, entity, String.class
                    );

                    consecutiveErrors = 0;

                    String responseBody = upstream.getBody();
                    log.debug("[BAKONG] Response received - md5={}, body={}", checkTransactionRequest.getMd5(), responseBody);
                    if (responseBody == null || responseBody.isBlank()) {
                        log.warn("[BAKONG] Empty response from Bakong API - md5={}, attempt={}",
                                checkTransactionRequest.getMd5(), pollCount);
                        Thread.sleep(pollIntervalMs);
                        continue;
                    }

                    log.debug("[BAKONG] Received response - md5={}, body={}", checkTransactionRequest.getMd5(), responseBody);

                    BakongResponse bakongResponse = objectMapper.readValue(responseBody, BakongResponse.class);
                    BakongCheckTopUpResponse response = objectMapper.convertValue(bakongResponse.getData(), BakongCheckTopUpResponse.class);
                    log.debug("[BAKONG] Response status code - md5={}, status={}, message={}",
                            checkTransactionRequest.getMd5(), response.getStatus(), bakongResponse.getResponseMessage());

                    // Handle terminal states
                    switch (response.getStatus()) {
                        case "PAID" -> {
                            log.info("[BAKONG] Payment SUCCESS - md5={}, bookingId={}",
                                    checkTransactionRequest.getMd5(), bookingId);
                            markSuccessAsync(bookingId);
                            return bakongResponse;
                        }
                        default -> {
                            // PENDING or unknown — wait and retry
                            log.debug("[BAKONG] Payment PENDING - md5={}, status={}, retrying in {}ms",
                                    checkTransactionRequest.getMd5(), response.getStatus(), pollIntervalMs);
                            Thread.sleep(pollIntervalMs);
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[BAKONG] Polling interrupted - md5={}, bookingId={}",
                            checkTransactionRequest.getMd5(), bookingId);
                    markFailureAsync(PaymentStatus.FAILED, bookingId, "Transaction check was interrupted");
                    throw new TransactionCheckException("Transaction check was interrupted", e);

                } catch (Exception e) {
                    consecutiveErrors++;
                    log.error("[BAKONG] Polling error - md5={}, attempt={}, consecutiveErrors={}, error={}",
                            checkTransactionRequest.getMd5(), pollCount, consecutiveErrors, e.getMessage());

                    // If too many consecutive errors, fail fast
                    if (consecutiveErrors >= maxRetries) {
                        log.error("[BAKONG] Max consecutive errors reached - md5={}, bookingId={}",
                                checkTransactionRequest.getMd5(), bookingId);
                        markFailureAsync(PaymentStatus.FAILED, bookingId,
                                "Transaction check failed after " + maxRetries + " consecutive errors");
                        throw new BakongApiException(
                                "Bakong API unreachable after " + maxRetries + " attempts", e);
                    }

                    // Retry after delay
                    try {
                        Thread.sleep(pollIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[BAKONG] Sleep interrupted during error recovery - md5={}",
                                checkTransactionRequest.getMd5());
                        markFailureAsync(PaymentStatus.FAILED, bookingId, "Transaction check interrupted during error recovery");
                        throw new TransactionCheckException("Transaction check interrupted during error recovery", ie);
                    }
                }
            }

            // Deadline reached — mark as TIMEOUT
            log.warn("PAYMENT_TIMEOUT", kv("method", "BAKONG_KHQR"), kv("bookingId", bookingId),
                    kv("attempts", pollCount));
            markFailureAsync(PaymentStatus.TIMEOUT, bookingId, "Transaction check timed out");

            // Return timeout response
//            return createErrorResponse(408, "Transaction check timed out");
            throw new PaymentTimeoutException(
                    "Transaction check timed out after " + pollCount + " attempts for booking ID: " + bookingId);

        } catch (ResourceNotFoundException | BadRequestException |
                 PaymentTimeoutException | BakongApiException | TransactionCheckException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG] Unexpected error during transaction check - bookingId={}, error={}",
                    bookingId, e.getMessage(), e);
            markFailureAsync(PaymentStatus.FAILED, bookingId, "Unexpected error during transaction check");
            throw new TransactionCheckException(
                    "Unexpected error checking transaction for booking ID: " + bookingId, e);
        }

    }


    /**
     * Get existing payment or create new one - extracted for clarity and reusability
     */
    private Payment getOrCreatePayment(Long bookingId, String md5) {
        log.debug("[BAKONG] Getting or creating payment - bookingId={}, md5={}", bookingId, md5);

        return paymentRepository.findByBookingId(bookingId).orElseGet(() -> {
            log.info("[BAKONG] Payment not found, creating new payment record - bookingId={}, md5={}",
                    bookingId, md5);

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG] Booking not found when creating payment - bookingId={}", bookingId);
                        return new ResourceNotFoundException("Booking not found");
                    });

            Payment newPayment = Payment.builder()
                    .booking(booking)
                    .amount(booking.getTotalAmount().doubleValue())
                    .currency(Currency.USD)
                    .method(PaymentMethodType.BAKONG)
                    .status(PaymentStatus.PENDING)
                    .transactionId(md5)
                    .walletTransactionId(null)  // Bakong payments don't use wallet transactions
                    .paidAt(LocalDateTime.now())
                    .build();

            Payment saved = paymentRepository.save(newPayment);
            log.info("[BAKONG] New payment created - paymentId={}, bookingId={}, amount={}",
                    saved.getId(), bookingId, saved.getAmount());
            return saved;
        });
    }

    /**
     * Create error response - extracted to avoid duplication
     */
    private BakongResponse createErrorResponse(int code, String message) {
        BakongResponse response = new BakongResponse();
        response.setResponseCode(code);
        response.setResponseMessage(message);
        return response;
    }

    /**
     * Finalise a successful payment.
     * NOTE: @Async on a self-invoked method is intentionally not used here because Spring's proxy
     * cannot intercept internal calls. The caller (checkingTransaction) returns the response first,
     * then this runs inline. To make this truly async, extract to a sibling @Component.
     */
    @Transactional
    public void markSuccessAsync(Long bookingId) {
        log.debug("[BAKONG] markSuccess called - bookingId={}", bookingId);
        try {
            markSuccess(bookingId);
        } catch (Exception e) {
            log.error("[BAKONG] Error in markSuccess - bookingId={}, error={}",
                    bookingId, e.getMessage(), e);
        }
    }

    /**
     * Finalise a failed/timed-out payment.
     * NOTE: Same proxy limitation as markSuccessAsync — runs inline.
     */
    @Transactional
    public void markFailureAsync(PaymentStatus status, Long bookingId, String reason) {
        log.debug("[BAKONG] markFailure called - bookingId={}, status={}, reason={}",
                bookingId, status, reason);
        try {
            markFailure(status, bookingId, reason);
        } catch (Exception e) {
            log.error("[BAKONG] Error in markFailure - bookingId={}, error={}",
                    bookingId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void createPending(String md5, Currency currency, Double amount, Long bookingId) {
        // Idempotent — skip if md5 already exists
        if (paymentRepository.findByTransactionId(md5).isPresent()) {
            log.info("Pending payment already exists for md5={}, skipping", md5);
            return;
        }
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .currency(currency)
                .method(PaymentMethodType.BAKONG)
                .status(PaymentStatus.PENDING)
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        log.info("Pending payment created for md5={} bookingId={}", md5, bookingId);
    }

    // ─────────────────────────────────────────────
    // 4. Mark SUCCESS (unchanged, already correct)
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public void markSuccess(Long bookingId) {
        log.info("[BAKONG] Marking payment as SUCCESS - bookingId={}", bookingId);

        try {
            Payment tx = findByBookingId(bookingId);
            log.debug("[BAKONG] Found payment - paymentId={}, currentStatus={}", tx.getId(), tx.getStatus());

            // Check if already successful (idempotent)
            if (tx.getStatus() == PaymentStatus.SUCCESS) {
                log.info("[BAKONG] Payment already marked as SUCCESS - paymentId={}", tx.getId());
                return;
            }
            tx.setMethod(PaymentMethodType.BAKONG);
            tx.setStatus(PaymentStatus.SUCCESS);
            tx.setPaidAt(LocalDateTime.now());
            paymentRepository.save(tx);
            log.debug("[BAKONG] Payment status updated to SUCCESS");

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG] Booking not found when marking success - bookingId={}", bookingId);
                        return new ResourceNotFoundException("Booking not found with ID: " + bookingId);
                    });

            booking.setPaymentStatus(PaymentStatus.SUCCESS);
            booking.setPaymentMethod(PaymentMethodType.BAKONG);
            bookingRepository.save(booking);
            log.info("BOOKING_CONFIRMED", kv("bookingId", bookingId), kv("paymentId", tx.getId()));

            BusClient.ScheduleInfo schedule= busClient.getScheduleById(booking.getScheduleId()).getBody().data();

            log.debug("[BAKONG] Creating ticket for successful payment");
            Ticket ticket = Ticket.builder()
                    .booking(booking)
                    .qrCode(null)
                    .issuedAt(schedule.departureDateTime())
                    .build();
            ticketRepository.save(ticket);
            log.info("[BAKONG] Ticket created - ticketId={}, bookingId={}", ticket.getId(), bookingId);

            // Broadcast SUCCESS via WebSocket
            log.debug("[BAKONG] Broadcasting PAYMENT_SUCCESS event");
            broadcastPaymentEvent("PAYMENT_SUCCESS", tx, booking, ticket.getId(), null);

            // Broadcast dashboard update
            log.debug("[BAKONG] Broadcasting dashboard update");
            broadcastDashboardEvent("NEW_BOOKING", booking, null);

            // Broadcast the payment change to the admin bookings list (live update)
            webSocketService.broadcastBookingUpdate("PAYMENT_UPDATED", bookingMapper.toResponse(booking));

            // Send Telegram notification for successful payment
            try {
                log.debug("[BAKONG] Sending Telegram notification");
                // Fetch user info
                ResponseEntity<UserClient.UserApiResponse<UserClient.UserBasicInfo>> userResponse =
                        userClient.getUserBasicInfoById(booking.getUserId());
                String fullName = userResponse.getBody() != null && userResponse.getBody().data() != null
                        ? userResponse.getBody().data().fullName()
                        : "Unknown";

                // Fetch route info
                ResponseEntity<BusClient.ApiResponse<BusClient.RouteInfo>> routeResponse =
                        busClient.getRouteByScheduleId(booking.getScheduleId());
                String destination = routeResponse.getBody() != null && routeResponse.getBody().data() != null
                        ? routeResponse.getBody().data().destination()
                        : "Unknown";

                com.busapp.bookingservice.dto.response.BookingResponse bookingResponse =
                        com.busapp.bookingservice.dto.response.BookingResponse.builder()
                                .id(booking.getId())
                                .fullName(fullName)
                                .destination(destination)
                                .scheduleId(booking.getScheduleId())
                                .totalAmount(booking.getTotalAmount())
                                .bookingStatus(booking.getBookingStatus())
                                .paymentStatus(booking.getPaymentStatus())
                                .paymentMethod(booking.getPaymentMethod())
                                .createdAt(booking.getCreatedAt())
                                .build();

                com.busapp.bookingservice.dto.response.PaymentResponse paymentResponse = paymentMapper.toResponse(tx);

                telegramNotificationService.sendPaymentCompletedAlert(bookingResponse, paymentResponse);
                log.info("[BAKONG] Telegram notification sent successfully");
            } catch (Exception e) {
                log.error("[BAKONG] Failed to send Telegram notification - bookingId={}, error={}",
                        bookingId, e.getMessage(), e);
                // Don't fail the transaction if notification fails
            }

            log.info("PAYMENT_SUCCESS", kv("method", "BAKONG_KHQR"), kv("bookingId", bookingId),
                    kv("paymentId", tx.getId()), kv("ticketId", ticket.getId()));

        } catch (ResourceNotFoundException e) {
            log.error("[BAKONG] Resource not found during markSuccess - bookingId={}", bookingId);
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG] Unexpected error during markSuccess - bookingId={}, error={}",
                    bookingId, e.getMessage(), e);
            throw new BakongPaymentException("Failed to mark payment as successful for booking ID: " + bookingId, e);
        }
    }

    // ─────────────────────────────────────────────
    // 5. Mark FAILURE (fixed: typo + wrong status)
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public void markFailure(PaymentStatus status, Long bookingId, String reason) {
        log.info("[BAKONG] Marking payment as FAILED - bookingId={}, status={}, reason={}",
                bookingId, status, reason);

        try {
            Payment tx = findByBookingId(bookingId);
            log.debug("[BAKONG] Found payment - paymentId={}, currentStatus={}", tx.getId(), tx.getStatus());

            // Check if already in terminal state (idempotent)
            if (tx.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("[BAKONG] Payment already marked as SUCCESS, cannot mark as failed - paymentId={}", tx.getId());
                return;
            }

            if (tx.getStatus() == status) {
                log.info("[BAKONG] Payment already marked as {} - paymentId={}", status, tx.getId());
                return;
            }

            tx.setStatus(status);
            tx.setMethod(PaymentMethodType.BAKONG);
            tx.setPaidAt(LocalDateTime.now());
            paymentRepository.save(tx);
            log.debug("[BAKONG] Payment status updated to {}", status);

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG] Booking not found when marking failure - bookingId={}", bookingId);
                        return new ResourceNotFoundException("Booking not found with ID: " + bookingId);
                    });

            // Broadcast FAILED via WebSocket
            log.debug("[BAKONG] Broadcasting PAYMENT_FAILED event");
            broadcastPaymentEvent("PAYMENT_FAILED", tx, booking, null, reason);

            log.info("[BAKONG] Releasing seats due to payment failure - bookingId={}", bookingId);
            try {
                busClient.releaseSeats(bookingId);
                log.info("[BAKONG] Seats released successfully");
            } catch (Exception e) {
                log.error("[BAKONG] Failed to release seats - bookingId={}, error={}", bookingId, e.getMessage(), e);
                // Don't fail the transaction if seat release fails
            }

            log.info("[BAKONG] Payment marked as FAILED completed - bookingId={}, paymentId={}, status={}",
                    bookingId, tx.getId(), status);

        } catch (ResourceNotFoundException e) {
            log.error("[BAKONG] Resource not found during markFailure - bookingId={}", bookingId);
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG] Unexpected error during markFailure - bookingId={}, error={}",
                    bookingId, e.getMessage(), e);
            throw new BakongPaymentException("Failed to mark payment as failed for booking ID: " + bookingId, e);
        }
    }

    // ─────────────────────────────────────────────
    // 6. Find by md5 (unchanged)
    // ─────────────────────────────────────────────

    @Override
    public Payment findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for Booking ID: " + bookingId));
    }

    // ─────────────────────────────────────────────
    // 7. paymentInfo stub (implement when ready)
    // ─────────────────────────────────────────────

    @Override
    public void paymentInfo() {
        // TODO: implement payment summary/info logic
        log.info("paymentInfo called — not yet implemented");
    }

    // ─────────────────────────────────────────────
    // WebSocket Broadcasting Helpers
    // ─────────────────────────────────────────────

    private void broadcastPaymentEvent(String type, Payment payment, Booking booking, Long ticketId, String reason) {
        try {
            PaymentStatusEvent event = PaymentStatusEvent.builder()
                    .type(type)
                    .paymentId(payment.getId())
                    .bookingId(booking.getId())
                    .userId(booking.getUserId())
                    .status(payment.getStatus())
                    .amount(payment.getAmount())
                    .ticketId(ticketId)
                    .reason(reason)
                    .timestamp(LocalDateTime.now())
                    .build();

            webSocketService.broadcastPaymentStatusToUser(booking.getUserId(), event);
        } catch (Exception e) {
            log.error("Failed to broadcast payment event: {}", e.getMessage(), e);
        }
    }

    private void broadcastDashboardEvent(String type, Booking booking, String reason) {
        try {
            String route = null;
            try {
                ResponseEntity<BusClient.ApiResponse<BusClient.RouteInfo>> routeResponse =
                        busClient.getRouteByScheduleId(booking.getScheduleId());
                if (routeResponse != null && routeResponse.getBody() != null && routeResponse.getBody().data() != null) {
                    BusClient.RouteInfo routeInfo = routeResponse.getBody().data();
                    route = routeInfo.origin() + " → " + routeInfo.destination();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch route info for dashboard event", e);
            }

            DashboardUpdateEvent event = DashboardUpdateEvent.builder()
                    .type(type)
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .route(route)
                    .reason(reason)
                    .timestamp(LocalDateTime.now())
                    .build();

            webSocketService.broadcastDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to broadcast dashboard event: {}", e.getMessage(), e);
        }
    }
}