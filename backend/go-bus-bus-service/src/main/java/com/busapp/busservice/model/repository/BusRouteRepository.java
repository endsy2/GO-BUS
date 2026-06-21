package com.busapp.busservice.model.repository;

import com.busapp.busservice.model.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    // ── Exact match ───────────────────────────────────────────────────────────

    List<BusRoute> findByOriginIgnoreCaseAndDestinationIgnoreCase(String origin, String destination);

    boolean existsByOriginIgnoreCaseAndDestinationIgnoreCase(String origin, String destination);

    // ── Partial keyword search (for search bar / admin) ───────────────────────

    @Query("""
            SELECT r FROM BusRoute r
            WHERE (:origin      IS NULL OR LOWER(r.origin)      LIKE LOWER(CONCAT('%', :origin,      '%')))
              AND (:destination IS NULL OR LOWER(r.destination) LIKE LOWER(CONCAT('%', :destination, '%')))
            ORDER BY r.origin, r.destination
            """)
    List<BusRoute> searchByOriginAndDestination(
            @Param("origin")      String origin,
            @Param("destination") String destination);

    @Query("""
            SELECT r FROM BusRoute r
            JOIN Bus b ON b.route.id = r.id
            JOIN BusSchedule s ON s.bus.id = b.id
            WHERE s.id = :scheduleId
            """)
    java.util.Optional<BusRoute> findByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("""
            SELECT DISTINCT r FROM BusRoute r
            JOIN Bus b ON b.route.id = r.id
            JOIN BusSchedule s ON s.bus.id = b.id
            WHERE s.id IN :scheduleIds
            """)
    List<BusRoute> findByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    @Query("""
            SELECT s.id, r FROM BusRoute r
            JOIN Bus b ON b.route.id = r.id
            JOIN BusSchedule s ON s.bus.id = b.id
            WHERE s.id IN :scheduleIds
            """)
    List<Object[]> findRoutesWithScheduleIds(@Param("scheduleIds") Set<Long> scheduleIds);
}
