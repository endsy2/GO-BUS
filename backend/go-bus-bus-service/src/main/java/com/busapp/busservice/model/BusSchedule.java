package com.busapp.busservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "\"BusSchedule\"",
        indexes = {
                @Index(name = "idx_schedule_bus",       columnList = "busId"),
                @Index(name = "idx_schedule_departure", columnList = "departureDateTime"),
                @Index(name = "idx_schedule_price",     columnList = "price"),
                @Index(name = "idx_schedule_route_date", columnList = "busId, departureDateTime")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"busId\"")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Bus bus;

    @Column
    private Double price;

    @Column(name = "\"departureDateTime\"")
    private LocalDateTime departureDateTime;

    @Column(name = "\"arrivalDateTime\"")
    private LocalDateTime arrivalDateTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "\"bookingIds\"", columnDefinition = "jsonb")
    @Builder.Default
    private String bookingIds = "[]";
    
    /**
     * Schedule-specific seat availability.
     * Each ScheduleSeat represents the availability of a physical seat for this specific schedule.
     */
    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ScheduleSeat> scheduleSeats;
}
