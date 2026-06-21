package com.busapp.bookingservice.repository.spec;

import com.busapp.bookingservice.dto.request.AdminBookingFilterRequest;
import com.busapp.bookingservice.dto.request.BookingFilterRequest;
import com.busapp.bookingservice.model.Booking;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    private BookingSpecification() {}

    /**
     * Optimized filter with fetch join for promo to avoid N+1 queries
     */
    public static Specification<Booking> filterBy(BookingFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch joins to avoid N+1 queries (only for non-count queries)
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("promo", JoinType.LEFT);
                root.fetch("ticket", JoinType.LEFT);
                root.fetch("refund", JoinType.LEFT);
            }

            // Always filter out soft-deleted bookings
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
            }

            // Filter by whether a refund exists
            if (filter.getRefund() != null) {
                if (filter.getRefund()) {
                    predicates.add(cb.isNotNull(root.get("refund")));
                } else {
                    predicates.add(cb.isNull(root.get("refund")));
                }
            }

            if (filter.getScheduleId() != null) {
                predicates.add(cb.equal(root.get("scheduleId"), filter.getScheduleId()));
            }

            if (filter.getBookingStatus() != null) {
                predicates.add(cb.equal(root.get("bookingStatus"), filter.getBookingStatus()));
            }

            if (filter.getPaymentStatus() != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), filter.getPaymentStatus()));
            }

            if (filter.getPaymentMethod() != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"),
                     BigDecimal.valueOf(filter.getMinAmount())));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"),
                     BigDecimal.valueOf(filter.getMaxAmount())));
            }

            if (filter.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds a {@link Specification} from the given filter.
     * Every non-null field adds an AND predicate — all filters compose freely.
     */
    public static Specification<Booking> from(AdminBookingFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch joins to avoid N+1 queries (only for non-count queries)
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("promo", JoinType.LEFT);
                root.fetch("refund", JoinType.LEFT);
            }

            // Always filter out soft-deleted bookings
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
            }
            if (filter.getScheduleId() != null) {
                predicates.add(cb.equal(root.get("scheduleId"), filter.getScheduleId()));
            }
            if (filter.getBookingStatus() != null) {
                predicates.add(cb.equal(root.get("bookingStatus"), filter.getBookingStatus()));
            }
            if (filter.getPaymentStatus() != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), filter.getPaymentStatus()));
            }

            // Filter by whether a refund exists
            if (filter.getRefund() != null) {
                if (filter.getRefund()) {
                    predicates.add(cb.isNotNull(root.get("refund")));
                } else {
                    predicates.add(cb.isNull(root.get("refund")));
                }
            }

            // Payment method lives on the Payment entity, not on Booking — join to it.
            if (filter.getPaymentMethod() != null) {
                Join<Object, Object> paymentJoin = root.join("payment", JoinType.LEFT);
                predicates.add(cb.equal(paymentJoin.get("method"), filter.getPaymentMethod()));
            }

            // Departure date range (inclusive): departureAt >= start-of-day, < start-of-next-day
            if (filter.getDepartureFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureAt"),
                        filter.getDepartureFrom().atStartOfDay()));
            }
            if (filter.getDepartureTo() != null) {
                predicates.add(cb.lessThan(root.get("departureAt"),
                        filter.getDepartureTo().plusDays(1).atStartOfDay()));
            }

            // Booking creation date range (inclusive)
            if (filter.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        filter.getCreatedFrom().atStartOfDay()));
            }
            if (filter.getCreatedTo() != null) {
                predicates.add(cb.lessThan(root.get("createdAt"),
                        filter.getCreatedTo().plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
