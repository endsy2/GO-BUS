package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.dto.mapper.PaymentMapper;
import com.busapp.bookingservice.dto.request.TicketFilterRequest;
import com.busapp.bookingservice.dto.response.*;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.BookingSeat;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.PromoCode;
import com.busapp.bookingservice.model.Ticket;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.BookingSeatRepository;
import com.busapp.bookingservice.repository.PaymentRepository;
import com.busapp.bookingservice.repository.TicketRepository;
import com.busapp.bookingservice.repository.spec.TicketSpecification;
import com.busapp.bookingservice.service.BookingService;
import com.busapp.bookingservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository       ticketRepository;
    private final BookingRepository      bookingRepository;
    private final BookingSeatRepository  bookingSeatRepository;
    private final PaymentRepository      paymentRepository;
    private final PaymentMapper          paymentMapper;
    private final BookingService         bookingService;
    private final UserClient             userClient;
    private final BusClient              busClient;

    @Override
    public TicketDetailResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        BookingDetailResponse bookingDetail = bookingService.getBookingDetailById(ticket.getBooking().getId());
        return toResponseDetail(ticket, bookingDetail);
    }

    @Override
    @Transactional
    public TicketResponse regenerateQr(Long bookingId) {
        log.debug("TICKET_QR_REGENERATE", kv("bookingId", bookingId));
        Ticket ticket = ticketRepository.findByBookingId(bookingId)
                .orElseGet(() -> {
                    Booking booking = bookingRepository.findById(bookingId)
                            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
                    return Ticket.builder().booking(booking).build();
                });

        ticket.setQrCode("QR-" + bookingId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        Ticket saved = ticketRepository.save(ticket);
        log.debug("TICKET_QR_REGENERATED", kv("bookingId", bookingId), kv("ticketId", saved.getId()));
        return toResponse(saved);
    }

    /**
     * Optimised list query — replaces N calls to getBookingDetailById with:
     *   1 batch user Feign call
     *   M schedule Feign calls (M = unique schedules on the page, parallel)
     *   M bus Feign calls (parallel)
     *   1 batch route Feign call
     *   1 batch seat-type Feign call
     *   K parallel seat-detail Feign calls (K = unique seats on the page)
     *   1 DB query for all booking seats
     *   1 DB query for all payments
     * Total Feign: O(M + K) instead of O(N × (3 + seats_per_booking))
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TicketDetailResponse> filterTickets(TicketFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issuedAt"));
        Page<Ticket> ticketPage = ticketRepository.findAll(TicketSpecification.filterBy(filter), pageable);

        if (ticketPage.isEmpty()) {
            return ticketPage.map(t -> toResponseDetail(t, null));
        }

        List<Ticket> tickets = ticketPage.getContent();

        // ── Collect all IDs in one pass ──────────────────────────────────────────
        Set<Long> userIds     = new HashSet<>();
        Set<Long> scheduleIds = new HashSet<>();
        List<Long> bookingIds = new ArrayList<>();

        for (Ticket t : tickets) {
            Booking b = t.getBooking();
            userIds.add(b.getUserId());
            scheduleIds.add(b.getScheduleId());
            bookingIds.add(b.getId());
        }

        // ── Batch fetch — users (1 Feign call) ──────────────────────────────────
        Map<Long, UserClient.UserBasicInfo> usersMap = batchUsers(userIds);

        // ── Batch fetch — routes (1 Feign call) ─────────────────────────────────
        Map<Long, BusClient.RouteInfo> routesMap = batchRoutes(scheduleIds);

        // ── Batch fetch — schedules in parallel (1 call per unique schedule) ────
        Map<Long, BusClient.ScheduleInfo> schedulesMap = batchSchedules(scheduleIds);

        // ── Batch fetch — buses in parallel (1 call per unique bus) ─────────────
        Set<Long> busIds = schedulesMap.values().stream()
                .map(BusClient.ScheduleInfo::busId)
                .collect(Collectors.toSet());
        Map<Long, BusClient.BusInfo> busesMap = batchBuses(busIds);

        // ── Batch fetch — booking seats (1 DB query) ─────────────────────────────
        Map<Long, List<BookingSeat>> seatsPerBooking = bookingSeatRepository
                .findByBookingIdIn(bookingIds).stream()
                .collect(Collectors.groupingBy(bs -> bs.getBooking().getId()));

        // ── Batch fetch — seat details in parallel (1 call per unique seat) ─────
        Set<Long> allSeatIds = seatsPerBooking.values().stream()
                .flatMap(List::stream)
                .map(BookingSeat::getSeatId)
                .collect(Collectors.toSet());
        Map<Long, BusClient.SeatResponse> seatDetailsMap = batchSeatDetails(allSeatIds);

        // ── Batch fetch — payments (1 DB query) ──────────────────────────────────
        Map<Long, Payment> paymentsMap = paymentRepository
                .findByBookingIdIn(bookingIds).stream()
                .collect(Collectors.toMap(p -> p.getBooking().getId(), p -> p));

        // ── Map each ticket using pre-fetched data ────────────────────────────────
        return ticketPage.map(ticket -> {
            Booking b       = ticket.getBooking();
            Long bookingId  = b.getId();

            // User snapshot
            UserClient.UserBasicInfo ui = usersMap.get(b.getUserId());
            BookingDetailResponse.UserSnapshot userSnapshot = ui == null ? null
                    : BookingDetailResponse.UserSnapshot.builder()
                        .id(ui.id()).fullName(ui.fullName())
                        .email(ui.email()).phone(ui.phone()).build();

            // Schedule + route + bus snapshot
            BusClient.ScheduleInfo si  = schedulesMap.get(b.getScheduleId());
            BusClient.RouteInfo    ri  = routesMap.get(b.getScheduleId());
            BusClient.BusInfo      bi  = si != null ? busesMap.get(si.busId()) : null;
            BookingDetailResponse.ScheduleSnapshot scheduleSnapshot = si == null ? null
                    : BookingDetailResponse.ScheduleSnapshot.builder()
                        .id(b.getScheduleId())
                        .busId(si.busId())
                        .busNumber(bi != null ? bi.busNumber() : null)
                        .busType(bi != null ? bi.busType() : null)
                        .departureDateTime(si.departureDateTime())
                        .arrivalDateTime(si.arrivalDateTime())
                        .price(si.price())
                        .origin(ri != null ? ri.origin() : null)
                        .destination(ri != null ? ri.destination() : null)
                        .build();

            // Promo (eagerly loaded by spec fetch join — no extra query)
            PromoCodeResponse promoResponse = buildPromoResponse(b.getPromo());

            // Seats
            List<BookingDetailResponse.SeatDetail> seats =
                    seatsPerBooking.getOrDefault(bookingId, List.of()).stream()
                            .map(bs -> {
                                BusClient.SeatResponse sr = seatDetailsMap.get(bs.getSeatId());
                                return BookingDetailResponse.SeatDetail.builder()
                                        .seatId(bs.getSeatId())
                                        .seatNumber(sr != null ? sr.seatNumber() : null)
                                        .seatType(sr != null ? sr.seatType() : null)
                                        .passengerNumber(bs.getPassengerNumber())
                                        .build();
                            })
                            .collect(Collectors.toList());

            // Payment
            Payment pmt = paymentsMap.get(bookingId);
            PaymentResponse paymentResponse = pmt != null ? paymentMapper.toResponse(pmt) : null;

            // Ticket itself
            TicketResponse ticketResponse = TicketResponse.builder()
                    .id(ticket.getId()).bookingId(bookingId)
                    .qrCode(ticket.getQrCode()).issuedAt(ticket.getIssuedAt())
                    .build();

            BookingDetailResponse bookingDetail = BookingDetailResponse.builder()
                    .id(bookingId)
                    .bookingStatus(b.getBookingStatus())
                    .paymentStatus(b.getPaymentStatus())
                    .refundStatus(b.getRefund() != null ? b.getRefund().getStatus() : null)
                    .paymentMethod(b.getPaymentMethod())
                    .totalAmount(b.getTotalAmount().doubleValue())
                    .createdAt(b.getCreatedAt())
                    .phoneNumber(b.getPhoneNumber())
                    .user(userSnapshot)
                    .schedule(scheduleSnapshot)
                    .promo(promoResponse)
                    .seats(seats)
                    .payment(paymentResponse)
                    .ticket(ticketResponse)
                    .build();

            return toResponseDetail(ticket, bookingDetail);
        });
    }

    // ── Batch helpers ─────────────────────────────────────────────────────────────

    private Map<Long, UserClient.UserBasicInfo> batchUsers(Set<Long> userIds) {
        try {
            var resp = userClient.getUsersByIds(userIds).getBody();
            if (resp != null && resp.data() != null) {
                return resp.data().stream()
                        .collect(Collectors.toMap(UserClient.UserBasicInfo::id, u -> u));
            }
        } catch (Exception e) {
            log.warn("TICKET_BATCH_USERS_FAILED: {}", e.getMessage());
        }
        return Map.of();
    }

    private Map<Long, BusClient.RouteInfo> batchRoutes(Set<Long> scheduleIds) {
        try {
            var resp = busClient.getRoutesWithScheduleMapping(scheduleIds).getBody();
            if (resp != null && resp.data() != null) {
                return resp.data().stream()
                        .collect(Collectors.toMap(
                                BusClient.ScheduleRouteInfo::scheduleId,
                                BusClient.ScheduleRouteInfo::route));
            }
        } catch (Exception e) {
            log.warn("TICKET_BATCH_ROUTES_FAILED: {}", e.getMessage());
        }
        return Map.of();
    }

    /** One Feign call per unique schedule, run in parallel. */
    private Map<Long, BusClient.ScheduleInfo> batchSchedules(Set<Long> scheduleIds) {
        List<CompletableFuture<Map.Entry<Long, BusClient.ScheduleInfo>>> futures =
                scheduleIds.stream()
                        .map(id -> CompletableFuture.supplyAsync(() -> {
                            try {
                                var resp = busClient.getScheduleById(id).getBody();
                                if (resp != null && resp.data() != null) {
                                    return Map.entry(id, resp.data());
                                }
                            } catch (Exception e) {
                                log.warn("TICKET_FETCH_SCHEDULE_FAILED scheduleId={}: {}", id, e.getMessage());
                            }
                            return null;
                        }))
                        .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** One Feign call per unique bus, run in parallel. */
    private Map<Long, BusClient.BusInfo> batchBuses(Set<Long> busIds) {
        List<CompletableFuture<Map.Entry<Long, BusClient.BusInfo>>> futures =
                busIds.stream()
                        .map(id -> CompletableFuture.supplyAsync(() -> {
                            try {
                                var resp = busClient.getBusById(id).getBody();
                                if (resp != null && resp.data() != null) {
                                    return Map.entry(id, resp.data());
                                }
                            } catch (Exception e) {
                                log.warn("TICKET_FETCH_BUS_FAILED busId={}: {}", id, e.getMessage());
                            }
                            return null;
                        }))
                        .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** All seat detail calls for the page run in parallel. */
    private Map<Long, BusClient.SeatResponse> batchSeatDetails(Set<Long> seatIds) {
        List<CompletableFuture<Map.Entry<Long, BusClient.SeatResponse>>> futures =
                seatIds.stream()
                        .map(id -> CompletableFuture.supplyAsync(() -> {
                            try {
                                var resp = busClient.getSeatById(id).getBody();
                                if (resp != null && resp.data() != null) {
                                    return Map.entry(id, resp.data());
                                }
                            } catch (Exception e) {
                                log.warn("TICKET_FETCH_SEAT_FAILED seatId={}: {}", id, e.getMessage());
                            }
                            return null;
                        }))
                        .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────────

    private PromoCodeResponse buildPromoResponse(PromoCode p) {
        if (p == null) return null;
        return PromoCodeResponse.builder()
                .id(p.getId()).code(p.getCode()).description(p.getDescription())
                .discountType(p.getDiscountType()).discountValue(p.getDiscountValue())
                .maxUses(p.getMaxUses()).usedCount(p.getUsedCount())
                .validFrom(p.getValidFrom()).validTo(p.getValidTo())
                .status(p.getStatus()).createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }

    private TicketResponse toResponse(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId())
                .bookingId(t.getBooking() != null ? t.getBooking().getId() : null)
                .qrCode(t.getQrCode())
                .issuedAt(t.getIssuedAt())
                .build();
    }

    private TicketDetailResponse toResponseDetail(Ticket ticket, BookingDetailResponse bookingDetail) {
        return TicketDetailResponse.builder()
                .id(ticket.getId())
                .qrCode(ticket.getQrCode())
                .bookingDetailResponse(bookingDetail)
                .issuedAt(ticket.getIssuedAt())
                .build();
    }
}
