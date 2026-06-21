package com.busapp.busservice.model.specification;


import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.BusRoute;
import com.busapp.busservice.model.BusSchedule;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public class ScheduleSpecification {
    private ScheduleSpecification() {}
    public static Specification<BusSchedule>filter(Long routeId, LocalDateTime from, LocalDateTime to, Double maxPrice){
        return ((root, query, cb) -> {
            log.info("=== ScheduleSpecification Filter Called ===");
            log.info("routeId: {}", routeId);
            log.info("from: {}", from);
            log.info("to: {}", to);
            log.info("maxPrice: {}", maxPrice);
            
            var predicates = cb.conjunction();
            
            if(routeId!=null){
                log.info("Adding routeId filter");
                Join<BusSchedule, Bus> busJoin=root.join("bus",JoinType.INNER);
                Join<Bus, BusRoute>routeJoin=busJoin.join("route",JoinType.INNER);
                predicates = cb.and(predicates, cb.equal(routeJoin.get("id"),routeId));
            }
            
            if(from!=null && to!=null){
                log.info("Adding date range filter: {} to {}", from, to);
                predicates = cb.and(predicates, cb.between(root.get("departureDateTime"),from,to));
            } else if(from!=null){
                log.info("Adding from date filter: {}", from);
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("departureDateTime"),from));
            } else if(to!=null){
                log.info("Adding to date filter: {}", to);
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("departureDateTime"),to));
            }
            
            if(maxPrice!=null){
                log.info("Adding maxPrice filter: {}", maxPrice);
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            log.info("Returning predicates");
            return predicates;
        });
    }
}
