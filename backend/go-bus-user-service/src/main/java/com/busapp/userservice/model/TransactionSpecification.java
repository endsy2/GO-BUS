package com.busapp.userservice.model;

import com.busapp.userservice.dto.request.TransactionFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<WalletTransaction> filterBy(TransactionFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getWalletId() != null) {
                predicates.add(cb.equal(root.get("wallet").get("id"), filter.getWalletId()));
            }

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Reference number — case-insensitive "contains" search so partial values match.
            if (filter.getReferenceId() != null && !filter.getReferenceId().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("referenceId")),
                        "%" + filter.getReferenceId().trim().toLowerCase() + "%"));
            }

            // Transaction date range (inclusive) on createdAt.
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        filter.getFromDate().atStartOfDay()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        filter.getToDate().atTime(LocalTime.MAX)));
            }

            // Amount range (inclusive).
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
