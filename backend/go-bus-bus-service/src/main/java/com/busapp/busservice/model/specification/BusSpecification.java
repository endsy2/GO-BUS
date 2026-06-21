package com.busapp.busservice.model.specification;

import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BusSpecification {

    private static final Logger log = LoggerFactory.getLogger(BusSpecification.class);

    private BusSpecification() {
    }

    public static Specification<Bus> filter(
            Long routeId,
            BusType busType,
            BusStatus status,
            String busNumber,
            String plate,
            Integer minSeats,
            Integer maxSeats) {

        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (routeId != null) {
                predicates.add(cb.equal(root.get("route").get("id"), routeId));
            }
            if (busType != null) {
                predicates.add(cb.equal(root.get("busType"), busType));
            }
            if (status != null) {
                log.debug("[BUS_SPEC] Filtering by status={}", status);
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (busNumber != null && !busNumber.isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("busNumber")), "%" + busNumber.trim().toLowerCase() + "%"));
            }
            if (plate != null && !plate.isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("plate")), "%" + plate.trim().toLowerCase() + "%"));
            }
            if (minSeats != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalSeats"), minSeats));
            }
            if (maxSeats != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalSeats"), maxSeats));
            }

            log.debug("[BUS_SPEC] Built bus filter - predicateCount={}", predicates.size());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}