package com.busapp.busservice.model.repository;

import com.busapp.busservice.model.BusLayout;
import com.busapp.busservice.model.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusLayoutRepository extends JpaRepository<BusLayout, Long> {
    Optional<BusLayout> findByName(String name);
}
