package com.busapp.busservice.model;

import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "\"Bus\"",
        indexes = {
                @Index(name = "idx_bus_route",  columnList = "routeId"),
                @Index(name = "idx_bus_number", columnList = "busNumber"),
                @Index(name = "idx_bus_type",   columnList = "busType")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"routeId\"", nullable = false)
    private BusRoute route;

    @Column(name = "\"busNumber\"", nullable = false, unique = true, length = 50)
    private String busNumber;

    @Column(name = "\"plate\"", unique = true,nullable = false, length = 20)
    private String plate;

    @Column(name = "\"model\"", length = 30)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"status\"", nullable = false)
    private BusStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"busType\"", nullable = false, columnDefinition = "VARCHAR(50)")
    private BusType busType;

    @Column(name = "\"totalSeats\"", nullable = false)
    private Integer totalSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"layoutId\"")
    private BusLayout layout;

    @OneToMany(mappedBy = "bus", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BusSchedule> schedules;

    @OneToMany(mappedBy = "bus", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Seat> seats;
}

