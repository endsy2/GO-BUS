package com.busapp.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * A fine-grained permission that can be assigned to roles.
 * Examples: BUS_READ, BUS_WRITE, USER_MANAGE, BOOKING_READ, PROMO_MANAGE
 */
@Entity
@Table(name = "\"Permission\"",
        indexes = @Index(name = "idx_permission_name", columnList = "name"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique permission key, e.g. BUS_READ, PROMO_MANAGE, ADMIN_ACCESS. */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Role> roles;
}
