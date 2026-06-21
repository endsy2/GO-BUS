package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.dto.mapper.PaymentMapper;
import com.busapp.bookingservice.dto.request.BookingFilterRequest;
import com.busapp.bookingservice.dto.request.BookingRequest;
import com.busapp.bookingservice.dto.request.RefundRequest;
import com.busapp.bookingservice.dto.response.*;
import com.busapp.bookingservice.exception.BadRequestException;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.dto.mapper.BookingMapper;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.BookingSeat;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.PromoCode;
import com.busapp.bookingservice.model.Refund;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.model.enums.RefundStatus;
import com.busapp.bookingservice.repository.*;
import com.busapp.bookingservice.repository.spec.BookingSpecification;
import com.busapp.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository     bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository     paymentRepository;
    private final TicketRepository      ticketRepository;
    private final PromoCodeRepository   promoCodeRepository;
    private final RefundRepository      refundRepository;
    private final BusClient             busClient;
    private final UserClient            userClient;
    private final BookingMapper         bookingMapper;
    private final PaymentMapper         paymentMapper;
    private final com.busapp.bookingservice.service.NotificationPublisher notificationPublisher;
    private final com.busapp.bookingservice.service.WebSocketService webSocketService;

    @Override
    public Page<BookingResponse> filterBookings(BookingFilterRequest filter, int page, int size) {
        log.info("[FILTER_BOOKINGS] Filtering bookings - page={}, size={}, filter={}", page, size, filter);
        
        // Use specification with fetch join for promo and ticket
        Page<Booking> bookingPage = bookingRepository.findAll(
                BookingSpecification.filterBy(filter),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<Booking> bookings = bookingPage.getContent();
        
        if (bookings.isEmpty()) {
            log.info("[FILTER_BOOKINGS] No bookings found matching filter");
            return Page.empty();
        }
        
        log.debug("[FILTER_BOOKINGS] Found {} bookings", bookings.size());
        
        // Collect unique user IDs and schedule IDs
        Set<Long> userIds = bookings.stream()
                .map(Booking::getUserId)
                .collect(Collectors.toSet());
        
        Set<Long> scheduleIds = bookings.stream()
                .map(Booking::getScheduleId)
                .collect(Collectors.toSet());

        log.debug("[FILTER_BOOKINGS] Batch fetching data - userCount={}, scheduleCount={}", 
                userIds.size(), scheduleIds.size());

        // Batch fetch users and routes in single calls
        java.util.Map<Long, String> userNames = new java.util.HashMap<>();
        java.util.Map<Long, String> routeDestinations = new java.util.HashMap<>();

        // Batch fetch all users
        try {
            log.debug("[FILTER_BOOKINGS] Fetching users via user-service");
            var usersResp = userClient.getUsersByIds(userIds);
            if (usersResp.getBody() != null && usersResp.getBody().data() != null) {
                usersResp.getBody().data().forEach(user ->
                    userNames.put(user.id(), user.fullName())
                );
                log.debug("[FILTER_BOOKINGS] Fetched {} user names", userNames.size());
            }
        } catch (Exception e) {
            log.warn("[FILTER_BOOKINGS] Failed to batch fetch users: {}", e.getMessage());
            userIds.forEach(id -> userNames.put(id, "Unknown User"));
        }

        // Batch fetch all routes with schedule mapping
        try {
            log.debug("[FILTER_BOOKINGS] Fetching routes via bus-service");
            var routesResp = busClient.getRoutesWithScheduleMapping(scheduleIds);
            if (routesResp.getBody() != null && routesResp.getBody().data() != null) {
                routesResp.getBody().data().forEach(scheduleRoute -> 
                    routeDestinations.put(scheduleRoute.scheduleId(), scheduleRoute.route().destination())
                );
                log.debug("[FILTER_BOOKINGS] Fetched {} route destinations", routeDestinations.size());
            }
        } catch (Exception e) {
            log.warn("[FILTER_BOOKINGS] Failed to batch fetch routes: {}", e.getMessage());
            scheduleIds.forEach(id -> routeDestinations.put(id, "Unknown Destination"));
        }

        // Map bookings with cached data using Page.map()
        log.debug("[FILTER_BOOKINGS] Mapping bookings to response DTOs");
        Page<BookingResponse> result = bookingPage.map(booking -> bookingMapper.toResponse(booking, 
                userNames.getOrDefault(booking.getUserId(), "Unknown User"),
                routeDestinations.getOrDefault(booking.getScheduleId(), "Unknown Destination")));
        
        log.info("[FILTER_BOOKINGS] Filter completed - totalElements={}, totalPages={}", 
                result.getTotalElements(), result.getTotalPages());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetailById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // ── User info (best-effort — show nulls if user-service is unavailable) ──
        BookingDetailResponse.UserSnapshot userSnapshot = null;
        try {
            var userResp = userClient.getUserBasicInfoById(booking.getUserId());
            if (userResp.getBody() != null) {
                var u = userResp.getBody();
                userSnapshot = BookingDetailResponse.UserSnapshot.builder()
                        .id(u.data().id())
                        .fullName(u.data().fullName())
                        .email(u.data().email())
                        .phone(u.data().phone())
                        .build();
            }
        } catch (Exception ignored) {}

        // ── Schedule + Bus info (best-effort) ──────────────────────────────────
        BookingDetailResponse.ScheduleSnapshot scheduleSnapshot = null;
        try {
            var schedResp = busClient.getScheduleById(booking.getScheduleId());
            if (schedResp.getBody() != null && schedResp.getBody().data() != null) {
                var s = schedResp.getBody().data();
                String busNumber = null;
                String busType   = null;
                try {
                    var busResp = busClient.getBusById(s.busId());
                    if (busResp.getBody() != null && busResp.getBody().data() != null) {
                        busNumber = busResp.getBody().data().busNumber();
                        busType   = busResp.getBody().data().busType();
                    }
                } catch (Exception ignored) {}
                String origin = null;
                String destination = null;
                try {
                    var routeResp = busClient.getRouteByScheduleId(booking.getScheduleId());
                    if (routeResp.getBody() != null && routeResp.getBody().data() != null) {
                        origin = routeResp.getBody().data().origin();
                        destination = routeResp.getBody().data().destination();
                    }
                } catch (Exception ignored) {}
                scheduleSnapshot = BookingDetailResponse.ScheduleSnapshot.builder()
                        .id(s.id())
                        .busId(s.busId())
                        .busNumber(busNumber)
                        .busType(busType)
                        .departureDateTime(s.departureDateTime())
                        .arrivalDateTime(s.arrivalDateTime())
                        .price(s.price())
                        .origin(origin)
                        .destination(destination)
                        .build();
            }
        } catch (Exception ignored) {}

        // ── Promo details ──────────────────────────────────────────────────────
        PromoCodeResponse promoResponse = null;
        if (booking.getPromo() != null) {
            PromoCode p = booking.getPromo();
            promoResponse = PromoCodeResponse.builder()
                    .id(p.getId())
                    .code(p.getCode())
                    .description(p.getDescription())
                    .discountType(p.getDiscountType())
                    .discountValue(p.getDiscountValue())
                    .maxUses(p.getMaxUses())
                    .usedCount(p.getUsedCount())
                    .validFrom(p.getValidFrom())
                    .validTo(p.getValidTo())
                    .status(p.getStatus())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
        }

        // ── Seats ──────────────────────────────────────────────────────────────
        List<BookingDetailResponse.SeatDetail> seats = bookingSeatRepository
                .findByBookingId(id).stream()
                .map(s -> {
                    String seatNumber = null;
                    String seatType = null;
                    try {
                        var seatRes = busClient.getSeatById(s.getSeatId()).getBody();
                        if (seatRes != null && seatRes.data() != null) {
                            seatNumber = seatRes.data().seatNumber();
                            seatType   = seatRes.data().seatType();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch seat info for seatId={}: {}", s.getSeatId(), e.getMessage());
                    }
                    return BookingDetailResponse.SeatDetail.builder()
                            .seatId(s.getSeatId())
                            .seatNumber(seatNumber)
                            .seatType(seatType)
                            .passengerNumber(s.getPassengerNumber())
                            .build();
                })
                .collect(Collectors.toList());

        // ── Payment ────────────────────────────────────────────────────────────
        PaymentResponse payment = paymentRepository
                .findByBookingId(id)
                .map(paymentMapper::toResponse)
                .orElse(null);

        // ── Ticket ─────────────────────────────────────────────────────────────
        TicketResponse ticketResponse = ticketRepository.findByBookingId(id)
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .bookingId(id)
                        .qrCode(t.getQrCode())
                        .issuedAt(t.getIssuedAt())
                        .build())
                .orElse(null);

        return BookingDetailResponse.builder()
                .id(booking.getId())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(booking.getPaymentStatus())
                .refundStatus(booking.getRefund() != null ? booking.getRefund().getStatus() : null)
                .paymentMethod(booking.getPaymentMethod())
                .totalAmount(booking.getTotalAmount().doubleValue())
                .createdAt(booking.getCreatedAt())
                .phoneNumber(booking.getPhoneNumber())
                .user(userSnapshot)
                .schedule(scheduleSnapshot)
                .promo(promoResponse)
                .seats(seats)
                .payment(payment)
                .ticket(ticketResponse)
                .build();
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        // NOTE: phoneNumber intentionally not logged (PII).
        log.info("BOOKING_REQUEST_RECEIVED", kv("userId", userId), kv("scheduleId", request.getScheduleId()),
                kv("seatCount", request.getSeatIds().size()));
        
        // ⚡ OPTIMIZATION: Parallel validation - fetch user and schedule concurrently
        log.debug("[CREATE_BOOKING] Validating user and schedule in parallel");
        
        CompletableFuture<ResponseEntity<UserClient.UserApiResponse<UserClient.UserBasicInfo>>> userFuture =
            CompletableFuture.supplyAsync(() -> userClient.getUserBasicInfoById(userId));
        
        CompletableFuture<org.springframework.http.ResponseEntity<BusClient.ApiResponse<BusClient.ScheduleInfo>>> scheduleFuture =
            CompletableFuture.supplyAsync(() -> busClient.getScheduleById(request.getScheduleId()));
        
        // Wait for both to complete
        CompletableFuture.allOf(userFuture, scheduleFuture).join();
        
        // Validate user
        var userResp = userFuture.join();
        if (userResp.getBody() == null) {
            log.error("[CREATE_BOOKING] User not found - userId={}", userId);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        log.debug("[CREATE_BOOKING] User validated successfully");

        // Validate schedule
        var scheduleResp = scheduleFuture.join();
        BusClient.ScheduleInfo schedule =
            scheduleResp.getBody() != null ? scheduleResp.getBody().data() : null;
        if (schedule == null) {
            log.error("[CREATE_BOOKING] Schedule not found - scheduleId={}", request.getScheduleId());
            throw new ResourceNotFoundException("Schedule not found with id: " + request.getScheduleId());
        }
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            log.error("[CREATE_BOOKING] No seats selected");
            throw new BadRequestException("At least one seat must be selected");
        }
        log.debug("[CREATE_BOOKING] Schedule validated - price={}, departureTime={}", 
                schedule.price(), schedule.departureDateTime());

        // Validate and apply promo code
        PromoCode promo = null;
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            log.info("[CREATE_BOOKING] Validating promo code - code={}", request.getPromoCode());
            promo = promoCodeRepository.findByCode(request.getPromoCode())
                    .orElseThrow(() -> {
                        log.error("[CREATE_BOOKING] Promo code not found - code={}", request.getPromoCode());
                        return new ResourceNotFoundException("Promo code not found: " + request.getPromoCode());
                    });
            
            // Validate promo code
            if (promo.getStatus() != com.busapp.bookingservice.model.enums.PromoStatus.ACTIVE) {
                log.warn("[CREATE_BOOKING] Promo code not active - code={}, status={}", 
                        request.getPromoCode(), promo.getStatus());
                throw new BadRequestException("Promo code is not active: " + request.getPromoCode());
            }
            if (promo.getValidFrom() != null && promo.getValidFrom().isAfter(java.time.LocalDateTime.now())) {
                log.warn("[CREATE_BOOKING] Promo code not yet valid - code={}, validFrom={}", 
                        request.getPromoCode(), promo.getValidFrom());
                throw new BadRequestException("Promo code is not yet valid: " + request.getPromoCode());
            }
            if (promo.getValidTo() != null && promo.getValidTo().isBefore(java.time.LocalDateTime.now())) {
                log.warn("[CREATE_BOOKING] Promo code expired - code={}, validTo={}", 
                        request.getPromoCode(), promo.getValidTo());
                throw new BadRequestException("Promo code has expired: " + request.getPromoCode());
            }
            if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
                log.warn("[CREATE_BOOKING] Promo code max uses reached - code={}, usedCount={}, maxUses={}", 
                        request.getPromoCode(), promo.getUsedCount(), promo.getMaxUses());
                throw new BadRequestException("Promo code has reached maximum uses: " + request.getPromoCode());
            }
            log.info("[CREATE_BOOKING] Promo code validated - code={}, discountType={}, discountValue={}", 
                    request.getPromoCode(), promo.getDiscountType(), promo.getDiscountValue());
        }


        // Check seat availability using schedule-based availability
        log.info("[CREATE_BOOKING] Checking seat availability - scheduleId={}, seatIds={}", 
                request.getScheduleId(), request.getSeatIds());
        var availabilityResp = busClient.checkSeatsAvailability(request.getScheduleId(), request.getSeatIds());
        Boolean areAvailable = availabilityResp.getBody() != null ? availabilityResp.getBody().data() : false;
        
        if (!Boolean.TRUE.equals(areAvailable)) {
            log.error("[CREATE_BOOKING] Seats not available - scheduleId={}, seatIds={}", 
                    request.getScheduleId(), request.getSeatIds());
            throw new BadRequestException("One or more selected seats are not available for this schedule");
        }
        log.debug("[CREATE_BOOKING] All seats available");

        // Calculate total amount
        double baseAmount = schedule.price() * request.getSeatIds().size();
        double totalAmount = baseAmount;
        if (promo != null) {
            switch (promo.getDiscountType()) {
                case PERCENTAGE -> totalAmount = baseAmount * (1 - promo.getDiscountValue() / 100.0);
                case FIXED      -> totalAmount = Math.max(0, baseAmount - promo.getDiscountValue());
            }
            log.info("[CREATE_BOOKING] Promo discount applied - baseAmount={}, discountedAmount={}, discountType={}", 
                    baseAmount, totalAmount, promo.getDiscountType());
        }
        log.debug("[CREATE_BOOKING] Total amount calculated - baseAmount={}, finalAmount={}", baseAmount, totalAmount);

        // Create booking first (without seats booked yet)
        log.debug("[CREATE_BOOKING] Creating booking entity");
        Booking booking = Booking.builder()
                .userId(userId)
                .scheduleId(request.getScheduleId())
                .totalAmount(BigDecimal.valueOf(totalAmount))
                .promo(promo)
                .bookingStatus(BookingStatus.CONFIRMED)
                .paymentMethod(null)
                .phoneNumber(request.getPhoneNumber())
                .departureAt(schedule.departureDateTime())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("BOOKING_CREATED", kv("bookingId", saved.getId()), kv("userId", userId),
                kv("scheduleId", request.getScheduleId()), kv("totalAmount", saved.getTotalAmount()));

        // Book seats for this schedule using the new schedule-based system
        try {
            busClient.bookSeats(request.getScheduleId(), saved.getId(), request.getSeatIds());
            log.info("SEAT_RESERVED", kv("bookingId", saved.getId()), kv("scheduleId", request.getScheduleId()),
                    kv("seatIds", request.getSeatIds()));
        } catch (Exception e) {
            // Rollback booking if seat booking fails
            log.error("BOOKING_FAILED", kv("bookingId", saved.getId()), kv("scheduleId", request.getScheduleId()),
                    kv("reason", "SEAT_RESERVATION_FAILED"), kv("error", e.getMessage()), e);
            bookingRepository.delete(saved);
            throw new BadRequestException("Failed to book seats: " + e.getMessage());
        }

        // Create booking seats
        log.debug("[CREATE_BOOKING] Creating booking seat records");
        List<BookingSeat> seats = request.getSeatIds().stream()
                .map(seatId -> BookingSeat.builder()
                        .booking(saved)
                        .seatId(seatId)
                        .build())
                .collect(Collectors.toList());
        bookingSeatRepository.saveAll(seats);
        log.debug("[CREATE_BOOKING] Booking seat records created - count={}", seats.size());

        // Create payment record
        log.debug("[CREATE_BOOKING] Creating payment record");
        paymentRepository.save(Payment.builder()
                .booking(saved)
                .amount(saved.getTotalAmount().doubleValue())
                .method(null)
                .status(PaymentStatus.PENDING)
                .build());
        log.debug("[CREATE_BOOKING] Payment record created with PENDING status");

        // Increment promo usage count
        if (promo != null) {
            log.debug("[CREATE_BOOKING] Incrementing promo usage count - code={}, oldCount={}", 
                    promo.getCode(), promo.getUsedCount());
            promo.setUsedCount(promo.getUsedCount() + 1);
            promoCodeRepository.save(promo);
            log.debug("[CREATE_BOOKING] Promo usage count incremented - newCount={}", promo.getUsedCount());
        }

        BookingResponse response = bookingMapper.toResponse(saved);
        
        //  OPTIMIZATION: Async operations - don't wait for notifications and WebSocket
        CompletableFuture.runAsync(() -> {
            try {
                // Publish notification event to RabbitMQ (async)
                log.debug("[CREATE_BOOKING] Publishing booking created notification to RabbitMQ");
                notificationPublisher.publishBookingCreated(response);
            } catch (Exception e) {
                log.error("[CREATE_BOOKING] Failed to publish notification: {}", e.getMessage());
            }
        });
        
        //  Broadcast to WebSocket for real-time dashboard updates (async)
        CompletableFuture.runAsync(() -> {
            try {
                // Get route info for dashboard event
                String routeInfo = "Unknown Route";
                try {
                    var routeResp = busClient.getRouteByScheduleId(saved.getScheduleId());
                    if (routeResp.getBody() != null && routeResp.getBody().data() != null) {
                        var route = routeResp.getBody().data();
                        routeInfo = route.origin() + " → " + route.destination();
                    }
                } catch (Exception e) {
                    log.warn("[CREATE_BOOKING] Failed to fetch route info for WebSocket: {}", e.getMessage());
                }
                
                // Create dashboard update event
                log.debug("[CREATE_BOOKING] Broadcasting dashboard update via WebSocket");
                com.busapp.bookingservice.dto.event.DashboardUpdateEvent dashboardEvent = 
                    new com.busapp.bookingservice.dto.event.DashboardUpdateEvent();
                dashboardEvent.setType("NEW_BOOKING");
                dashboardEvent.setBookingId(saved.getId());
                dashboardEvent.setRoute(routeInfo);
                dashboardEvent.setAmount(saved.getTotalAmount());  // Already BigDecimal
                dashboardEvent.setTimestamp(java.time.LocalDateTime.now());
                
                // Broadcast dashboard update
                webSocketService.broadcastDashboardUpdate(dashboardEvent);

                // Broadcast the new booking row to the admin bookings list (live insert)
                webSocketService.broadcastBookingUpdate("CREATED", response);

                log.info("[CREATE_BOOKING] Broadcasted new booking event to WebSocket dashboard");
            } catch (Exception e) {
                log.error("[CREATE_BOOKING] Failed to broadcast booking to WebSocket: {}", e.getMessage());
                // Don't fail the booking if WebSocket broadcast fails
            }
        });

        // BOOKING_CREATED already emitted above; this just marks the end of the full flow (incl. async side-effects).
        log.debug("[CREATE_BOOKING] Booking flow completed - bookingId={}", saved.getId());
        return response;
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        log.info("[CANCEL_BOOKING] Starting booking cancellation - bookingId={}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[CANCEL_BOOKING] Booking not found - bookingId={}", id);
                    return new ResourceNotFoundException("Booking not found with id: " + id);
                });
        
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            log.warn("[CANCEL_BOOKING] Booking already cancelled - bookingId={}", id);
            throw new BadRequestException("Booking is already cancelled.");
        }
        
        log.debug("[CANCEL_BOOKING] Current booking status - bookingId={}, status={}, paymentStatus={}", 
                id, booking.getBookingStatus(), booking.getPaymentStatus());
        
        // Release seats for this booking
        try {
            log.info("[CANCEL_BOOKING] Releasing seats via bus-service - bookingId={}", id);
            busClient.releaseSeats(id);
            log.info("[CANCEL_BOOKING] Seats released successfully");
        } catch (Exception e) {
            log.warn("[CANCEL_BOOKING] Failed to release seats - bookingId={}, error={}", id, e.getMessage());
        }
        
        log.debug("[CANCEL_BOOKING] Updating booking and payment status to CANCELLED");
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Payment payment = paymentRepository.findByBookingId(id)
                .orElseThrow(() -> {
                    log.error("[CANCEL_BOOKING] Payment not found for booking - bookingId={}", id);
                    return new ResourceNotFoundException("Payment not found for booking: " + id);
                });
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        
        BookingResponse response = bookingMapper.toResponse(bookingRepository.save(booking));
        
        // Publish notification event to RabbitMQ (async)
        log.debug("[CANCEL_BOOKING] Publishing cancellation notification to RabbitMQ");
        notificationPublisher.publishBookingCancelled(response, "User requested cancellation");

        // Broadcast the status change to the admin bookings list (live update)
        webSocketService.broadcastBookingUpdate("CANCELLED", response);

        log.info("[CANCEL_BOOKING] Booking cancelled successfully - bookingId={}", id);
        return response;
    }

    @Override
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

}
