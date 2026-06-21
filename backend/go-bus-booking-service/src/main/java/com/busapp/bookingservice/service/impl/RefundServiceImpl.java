package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.dto.mapper.BookingMapper;
import com.busapp.bookingservice.dto.request.RefundApprovalRequest;
import com.busapp.bookingservice.dto.request.RefundRequest;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.response.PromoCodeResponse;
import com.busapp.bookingservice.dto.response.RefundDetailResponse;
import com.busapp.bookingservice.dto.response.RefundResponse;
import com.busapp.bookingservice.dto.response.RefundStatisticsResponse;
import com.busapp.bookingservice.exception.BadRequestException;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.BookingSeat;
import com.busapp.bookingservice.model.Refund;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.model.enums.RefundMethod;
import com.busapp.bookingservice.model.enums.RefundStatus;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.RefundRepository;
import com.busapp.bookingservice.service.RefundService;
import com.busapp.bookingservice.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final UserClient userClient;
    private final BusClient busClient;
    private final BookingMapper bookingMapper;
    private final TelegramNotificationService telegramNotificationService;
    private final com.busapp.bookingservice.service.WebSocketService webSocketService;

    // ── User Operations ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public RefundResponse requestRefund(Long bookingId, RefundRequest request, Long userId) {
        log.info("[REFUND] User {} requesting refund for booking {}", userId, bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Verify booking belongs to user
        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("You can only request refund for your own bookings");
        }

        // Check if booking is eligible for refund
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed bookings can be refunded");
        }

        if (booking.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only paid bookings can be refunded");
        }

        // Check if refund already exists
        if (refundRepository.existsByBookingId(bookingId)) {
            throw new BadRequestException("Refund request already exists for this booking");
        }
        BusClient.ScheduleInfo scheduleInfo= Objects.requireNonNull(busClient.getScheduleById(booking.getScheduleId()).getBody()).data();

        if(scheduleInfo.departureDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Bus have been departed");
        }
        log.info("[Refund]:Request Refund Method:{}",request.getMethod());
        // Create refund request
        Refund refund = Refund.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .reason(request.getReason())
                .refundMethod(request.getMethod() != null ? request.getMethod() : RefundMethod.MANUAL)
                .status(RefundStatus.PENDING)
                .build();

        // Update booking status
//        booking.setBookingStatus(BookingStatus.REFUND_REQUESTED);
//        bookingRepository.save(booking);

        Refund savedRefund = refundRepository.save(refund);

        log.info("[REFUND] Refund request created: {}", savedRefund.getId());

        // Send notification to admin
        BookingResponse bookingResponse = bookingMapper.toResponse(booking);
        telegramNotificationService.sendAdminActionAlert(
            "NEW REFUND REQUEST",
            bookingResponse,
            "User ID: " + userId + " | Reason: " + request.getReason()
        );

        // Live update: refund affects the booking row + dashboard pending-refunds counter
//        webSocketService.broadcastBookingUpdate("UPDATED", bookingResponse);

        return toRefundResponse(savedRefund);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundDetailResponse> getMyRefunds(Long userId, RefundStatus status, Pageable pageable) {
        log.info("[REFUND] Fetching refunds for user {} with status: {}", userId, status);

        Page<Refund> refunds = status != null
                ? refundRepository.findByUserIdAndStatus(userId, status, pageable)
                : refundRepository.findByUserId(userId, pageable);
        return refunds.map(this::buildRefundDetailResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundById(Long refundId, Long userId) {
        log.info("[REFUND] User {} fetching refund {}", userId, refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        // Verify user owns the booking
        if (!refund.getBooking().getUserId().equals(userId)) {
            throw new BadRequestException("You can only view your own refunds");
        }

        return toRefundResponse(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundDetailResponse getRefundDetailById(Long refundId, Long userId) {
        log.info("[REFUND_DETAIL] User {} fetching detailed refund {}", userId, refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        // Verify user owns the booking
        if (!refund.getBooking().getUserId().equals(userId)) {
            throw new BadRequestException("You can only view your own refunds");
        }

        return buildRefundDetailResponse(refund);
    }

    // ── Admin Operations ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public RefundResponse processRefund(Long refundId, RefundApprovalRequest request, Long adminId) {
        log.info("[REFUND] Admin {} processing refund {}", adminId, refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new BadRequestException("Refund has already been processed");
        }

        Booking booking = refund.getBooking();

        if (request.getApproved()) {
            // Approve refund
            log.info("[REFUND] Approving refund {} for booking {}", refundId, booking.getId());

            refund.setStatus(RefundStatus.APPROVED);
            refund.setAdminNote(request.getAdminNote());
            refund.setProcessedBy(adminId);
            refund.setProcessedAt(LocalDateTime.now());

            // Update booking status
            booking.setBookingStatus(BookingStatus.REFUNDED);
            bookingRepository.save(booking);

            // Process wallet refund via user-service
            try {
                log.info("[REFUND] Processing wallet refund for user {}, amount: {}",
                    booking.getUserId(), refund.getAmount());
                if(refund.getRefundMethod().toString().equals(RefundMethod.WALLET.toString())) {
                    log.info("[REFUND] Manual refund for user {}, amount: {}",booking.getUserId(), refund.getAmount());
                    var refundBody = userClient.refundWallet(
                            booking.getUserId(),
                            refund.getAmount().doubleValue(),
                            "Refund for booking #" + booking.getId()
                    ).getBody();
                    if (refundBody == null || refundBody.data() == null) {
                        throw new BadRequestException("Wallet refund returned no transaction data");
                    }
                    log.info("[REFUND] Wallet refund transaction amount: {}", refundBody.data().amount());
                }
                log.info("[REFUND] Wallet refund successful");
            } catch (Exception e) {
                log.error("[REFUND] Failed to process wallet refund: {}", e.getMessage());
                throw new BadRequestException("Failed to process wallet refund: " + e.getMessage());
            }
            
            // Release seats via bus-service
            try {
                log.info("[REFUND] Releasing seats for booking {}", booking.getId());
                busClient.releaseSeats(booking.getId());
                log.info("[REFUND] Seats released successfully");
            } catch (Exception e) {
                log.error("[REFUND] Failed to release seats: {}", e.getMessage());
                // Continue even if seat release fails - refund is more important
            }
            
            // Send notification
            BookingResponse bookingResponse = bookingMapper.toResponse(booking);
            telegramNotificationService.sendAdminActionAlert(
                "REFUND APPROVED",
                bookingResponse,
                "Admin ID: " + adminId + " | Amount: $" + refund.getAmount()
            );

            // Live update: refund resolution affects the booking row + dashboard counters
            webSocketService.broadcastBookingUpdate("UPDATED", bookingResponse);

        } else {
            // Reject refund
            log.info("[REFUND] Rejecting refund {} for booking {}", refundId, booking.getId());
            
            refund.setStatus(RefundStatus.REJECTED);
            refund.setAdminNote(request.getAdminNote());
            refund.setProcessedBy(adminId);
            refund.setProcessedAt(LocalDateTime.now());
            
            // Revert booking status back to confirmed
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            
            // Send notification
            BookingResponse bookingResponse = bookingMapper.toResponse(booking);
            telegramNotificationService.sendAdminActionAlert(
                "REFUND REJECTED",
                bookingResponse,
                "Admin ID: " + adminId + " | Reason: " + request.getAdminNote()
            );

            // Live update: refund resolution affects the booking row + dashboard counters
            webSocketService.broadcastBookingUpdate("UPDATED", bookingResponse);
        }

        Refund savedRefund = refundRepository.save(refund);
        return toRefundResponse(savedRefund);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundResponse> getAllRefunds(RefundStatus status, Pageable pageable) {
        log.info("[REFUND] Fetching all refunds with status: {}", status);
        
        Page<Refund> refunds;
        if (status != null) {
            refunds = refundRepository.findByStatus(status, pageable);
        } else {
            refunds = refundRepository.findAll(pageable);
        }
        
        return refunds.map(this::toRefundResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundResponse> getRefundsByDateRange(
            RefundStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        
        log.info("[REFUND] Fetching refunds - status: {}, from: {}, to: {}", status, fromDate, toDate);
        
        Page<Refund> refunds = refundRepository.findByStatusAndDateRange(
            status, fromDate, toDate, pageable
        );
        
        return refunds.map(this::toRefundResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundStatisticsResponse getRefundStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("[REFUND] Calculating statistics from {} to {}", fromDate, toDate);
        
        Long totalPending = refundRepository.countByStatusAndDateRange(
            RefundStatus.PENDING, fromDate, toDate
        );
        Long totalApproved = refundRepository.countByStatusAndDateRange(
            RefundStatus.APPROVED, fromDate, toDate
        );
        Long totalRejected = refundRepository.countByStatusAndDateRange(
            RefundStatus.REJECTED, fromDate, toDate
        );
        
        Double totalPendingAmount = refundRepository.getTotalRefundAmountByStatusAndDateRange(
            RefundStatus.PENDING, fromDate, toDate
        );
        Double totalApprovedAmount = refundRepository.getTotalRefundAmountByStatusAndDateRange(
            RefundStatus.APPROVED, fromDate, toDate
        );
        Double totalRejectedAmount = refundRepository.getTotalRefundAmountByStatusAndDateRange(
            RefundStatus.REJECTED, fromDate, toDate
        );
        
        Double averageRefundAmount = refundRepository.getAverageRefundAmountByStatusAndDateRange(
            RefundStatus.APPROVED, fromDate, toDate
        );
        
        return RefundStatisticsResponse.builder()
                .totalPending(totalPending)
                .totalApproved(totalApproved)
                .totalRejected(totalRejected)
                .totalPendingAmount(totalPendingAmount)
                .totalApprovedAmount(totalApprovedAmount)
                .totalRejectedAmount(totalRejectedAmount)
                .averageRefundAmount(averageRefundAmount)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RefundResponse toRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .bookingId(refund.getBooking().getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .adminNote(refund.getAdminNote())
                .processedBy(refund.getProcessedBy())
                .processedAt(refund.getProcessedAt())
                .refundMethod(refund.getRefundMethod())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }

    private RefundDetailResponse buildRefundDetailResponse(Refund refund) {
        Booking booking = refund.getBooking();
        Long userId = booking.getUserId();
        Long scheduleId = booking.getScheduleId();

        log.info("[REFUND_DETAIL] Building detailed response - refundId: {}, bookingId: {}, userId: {}", 
            refund.getId(), booking.getId(), userId);

        // Fetch user info from user-service
        RefundDetailResponse.UserSnapshot userSnapshot = null;
        RefundDetailResponse.WalletSnapshot walletSnapshot = null;
        try {
            var userResponse = userClient.getUserBasicInfoById(userId);
            if (userResponse.getBody() != null && userResponse.getBody().data() != null) {
                var user = userResponse.getBody().data();
                userSnapshot = RefundDetailResponse.UserSnapshot.builder()
                        .id(user.id())
                        .userName(user.userName())
                        .fullName(user.fullName())
                        .email(user.email())
                        .phone(user.phone())
                        .build();
            }

            // Fetch wallet info
            var walletResponse = userClient.getWalletByUserId(userId);
            if (walletResponse.getBody() != null && walletResponse.getBody().data() != null) {
                var wallet = walletResponse.getBody().data();
                walletSnapshot = RefundDetailResponse.WalletSnapshot.builder()
                        .walletId(wallet.walletId())
                        .balance(wallet.balance())
                        .currency(wallet.currency())
                        .status(wallet.status())
                        .build();
            }
        } catch (Exception e) {
            log.error("[REFUND_DETAIL] Failed to fetch user/wallet info: {}", e.getMessage());
        }

        // Fetch schedule info from bus-service
        RefundDetailResponse.ScheduleSnapshot scheduleSnapshot = null;
        RefundDetailResponse.BusSnapshot busSnapshot = null;
        RefundDetailResponse.RouteSnapshot routeSnapshot = null;
        
        try {
            var scheduleResponse = busClient.getScheduleById(scheduleId);
            if (scheduleResponse.getBody() != null && scheduleResponse.getBody().data() != null) {
                var schedule = scheduleResponse.getBody().data();
                
                // Fetch bus info
                try {
                    var busResponse = busClient.getBusById(schedule.busId());
                    if (busResponse.getBody() != null && busResponse.getBody().data() != null) {
                        var bus = busResponse.getBody().data();
                        busSnapshot = RefundDetailResponse.BusSnapshot.builder()
                                .id(bus.id())
                                .busNumber(bus.busNumber())
                                .busType(bus.busType())
                                .totalSeats(bus.totalSeats())
                                .build();
                    }
                } catch (Exception e) {
                    log.error("[REFUND_DETAIL] Failed to fetch bus info: {}", e.getMessage());
                }

                // Fetch route info
                try {
                    var routeResponse = busClient.getRouteByScheduleId(scheduleId);
                    if (routeResponse.getBody() != null && routeResponse.getBody().data() != null) {
                        var route = routeResponse.getBody().data();
                        routeSnapshot = RefundDetailResponse.RouteSnapshot.builder()
                                .id(route.id())
                                .routeName(route.name())
                                .origin(route.origin())
                                .destination(route.destination())
                                .distance(route.distance())
                                .estimatedDuration((int) route.duration())
                                .build();
                    }
                } catch (Exception e) {
                    log.error("[REFUND_DETAIL] Failed to fetch route info: {}", e.getMessage());
                }

                scheduleSnapshot = RefundDetailResponse.ScheduleSnapshot.builder()
                        .id(schedule.id())
                        .departureDateTime(schedule.departureDateTime())
                        .arrivalDateTime(schedule.arrivalDateTime())
                        .price(schedule.price())
                        .availableSeats(null) // Not available in this context
                        .bus(busSnapshot)
                        .build();
            }
        } catch (Exception e) {
            log.error("[REFUND_DETAIL] Failed to fetch schedule info: {}", e.getMessage());
        }

        // Build seat details
        List<RefundDetailResponse.SeatDetail> seatDetails = booking.getSeats().stream()
                .map(seat -> RefundDetailResponse.SeatDetail.builder()
                        .seatId(seat.getSeatId())
                        .passengerNumber(seat.getPassengerNumber())
                        .build())
                .collect(Collectors.toList());

        // Build promo details if exists
        PromoCodeResponse promoResponse = null;
        if (booking.getPromo() != null) {
            var promo = booking.getPromo();
            promoResponse = PromoCodeResponse.builder()
                    .id(promo.getId())
                    .code(promo.getCode())
                    .discountType(promo.getDiscountType())
                    .discountValue(promo.getDiscountValue())
                    .description(promo.getDescription())
                    .build();
        }

        // Build booking snapshot
        RefundDetailResponse.BookingSnapshot bookingSnapshot = RefundDetailResponse.BookingSnapshot.builder()
                .id(booking.getId())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .totalAmount(booking.getTotalAmount())
                .phoneNumber(booking.getPhoneNumber())
                .createdAt(booking.getCreatedAt())
                .seats(seatDetails)
                .promo(promoResponse)
                .build();

        // Build complete refund detail response
        return RefundDetailResponse.builder()
                .id(refund.getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .adminNote(refund.getAdminNote())
                .processedBy(refund.getProcessedBy())
                .processedAt(refund.getProcessedAt())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .booking(bookingSnapshot)
                .user(userSnapshot)
                .wallet(walletSnapshot)
                .schedule(scheduleSnapshot)
                .route(routeSnapshot)
                .build();
    }
}
