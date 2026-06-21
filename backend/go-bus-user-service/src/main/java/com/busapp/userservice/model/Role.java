package com.busapp.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Application role, e.g. ROLE_USER, ROLE_ADMIN, ROLE_BUS_OPERATOR.
 * Each role carries a set of fine-grained permissions.
 */
@Entity
@Table(name = "\"Role\"",
        indexes = @Index(name = "idx_role_name", columnList = "name"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique role name — must be prefixed with ROLE_ by convention. */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Permissions granted to this role.
     * LAZY — only loaded when explicitly accessed via JOIN FETCH queries.
     * @BatchSize batches any remaining lazy hits into one IN-clause query.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "\"RolePermission\"",
            joinColumns        = @JoinColumn(name = "\"roleId\""),
            inverseJoinColumns = @JoinColumn(name = "\"permissionId\"")
    )
    @BatchSize(size = 25)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Permission> permissions;

    /** Users assigned to this role (back-reference). */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users;
}
