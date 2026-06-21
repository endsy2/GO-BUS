package com.busapp.busservice.model.repository;

import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.BusLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long>, JpaSpecificationExecutor<Bus> {
    Optional<Bus> findByBusNumber(String busNumber);
    List<Bus> findByRouteId(Long routeId);
    List<Bus> findByRouteIdAndBusType(Long routeId, com.busapp.busservice.model.enums.BusType busType);

    Optional<Bus> findByPlate(String plate);
    
    @Query("SELECT DISTINCT b.model FROM Bus b WHERE b.model IS NOT NULL ORDER BY b.model")
    List<String> findDistinctModels();
    
    // Dashboard statistics
    Long countByStatus(com.busapp.busservice.model.enums.BusStatus status);

    boolean existsBusByLayoutId(Long layoutId);
}
