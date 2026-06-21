package com.busapp.userservice.model;

import com.busapp.userservice.dto.request.WalletFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WalletSpecification {

    public static Specification<UserWallet> filterBy(WalletFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
            }

            if (filter.getName() != null && !filter.getName().isBlank()) {
                String searchPattern = "%" + filter.getName().toLowerCase() + "%";
                Predicate userNameMatch = cb.like(cb.lower(root.get("user").get("userName")), searchPattern);
                Predicate fullNameMatch = cb.like(cb.lower(root.get("user").get("fullName")), searchPattern);
                predicates.add(cb.or(userNameMatch, fullNameMatch));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getMinBalance() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("balance"), filter.getMinBalance()));
            }

            if (filter.getMaxBalance() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("balance"), filter.getMaxBalance()));
            }

            if (filter.getCurrency() != null && !filter.getCurrency().isBlank()) {
                predicates.add(cb.equal(root.get("currency"), filter.getCurrency()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
