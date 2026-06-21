package com.busapp.busservice.model.repository;

import com.busapp.busservice.model.BusSchedule;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BusScheduleRepository extends JpaRepository<BusSchedule, Long>, JpaSpecificationExecutor<BusSchedule> {
    List<BusSchedule> findByBusId(Long busId);
    List<BusSchedule> findByDepartureDateTimeBetween(LocalDateTime from, LocalDateTime to);
    List<BusSchedule> findByBusIdAndDepartureDateTimeBetween(Long busId, LocalDateTime from, LocalDateTime to);
    List<BusSchedule> findByPriceLessThanEqual(Double maxPrice);
    @Modifying
    @Query("DELETE FROM BusSchedule bs WHERE bs.bus.id= :busId" )
    void deleteByBusId(@Param("busId")Long busId);

//    Page<BusSchedule> findAll(Specification<BusSchedule> filter, PageRequest pageRequest);
}
