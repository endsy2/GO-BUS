package com.busapp.busservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "\"BusRoute\"",
        indexes = @Index(name = "idx_route_origin_dest", columnList = "origin, destination"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(name = "\"distanceKm\"")
    private Double distanceKm;

    @Column(name = "\"durationMinutes\"")
    private Integer durationMinutes;

    /**
     * Stores origin location data as JSON.
     * Example: {"lat": 11.5564, "lng": 104.9282}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "\"originLocation\"", columnDefinition = "jsonb")
    private String originLocation;

    /**
     * Stores destination location data as JSON.
     * Example: {"lat": 13.3671, "lng": 103.8448}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "\"destinationLocation\"", columnDefinition = "jsonb")
    private String destinationLocation;

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Bus> buses;
}

