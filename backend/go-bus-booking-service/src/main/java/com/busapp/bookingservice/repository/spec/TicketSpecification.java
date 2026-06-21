package com.busapp.bookingservice.repository.spec;

import  com.busapp.bookingservice.dto.request.TicketFilterRequest;
import com.busapp.bookingservice.model.Ticket;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.TicketStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    private TicketSpecification() {}

    /**
     * Builds a {@link Specification} from the given filter.
     * Every non-null field adds an AND predicate — all filters compose freely.
     */
    public static Specification<Ticket> filterBy(TicketFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch-join booking + its lazy associations to avoid N+1 queries
            if (query != null && Long.class != query.getResultType()) {
                var bookingFetch = root.fetch("booking", JoinType.LEFT);
                bookingFetch.fetch("promo", JoinType.LEFT);
                bookingFetch.fetch("refund", JoinType.LEFT);
            }

            // Always exclude tickets whose booking has been refunded or cancelled
            predicates.add(root.get("booking").get("bookingStatus").in(
                    BookingStatus.REFUNDED, BookingStatus.CANCELLED
            ).not());

            if (filter.getBookingId() != null) {
                predicates.add(cb.equal(root.get("booking").get("id"), filter.getBookingId()));
            }
            if(filter.getTicketStatus() != null) {
                if (filter.getTicketStatus().equals(TicketStatus.INCOMING)) {
                    predicates.add(cb.greaterThan(root.get("booking").get("departureAt"), LocalDateTime.now()));
                }
                if (filter.getTicketStatus().equals(TicketStatus.PASS)) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("booking").get("departureAt"), LocalDateTime.now()));
                }
            }
            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("booking").get("userId"), filter.getUserId()));
            }

            if (filter.getScheduleId() != null) {
                predicates.add(cb.equal(root.get("booking").get("scheduleId"), filter.getScheduleId()));
            }

            if (filter.getIssuedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issuedAt"), filter.getIssuedFrom()));
            }

            if (filter.getIssuedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issuedAt"), filter.getIssuedTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
