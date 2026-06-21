package com.busapp.userservice.model;

import com.busapp.userservice.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.naming.Name;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "\"User\"",
        indexes = {
                @Index(name = "idx_user_email",      columnList = "email"),
                @Index(name = "idx_user_phone",      columnList = "phone"),
                @Index(name = "idx_user_google_id",  columnList = "googleId"),
                @Index(name = "idx_user_active",     columnList = "active"),       // active filter in list
                @Index(name = "idx_user_created_at", columnList = "createdAt")    // ORDER BY createdAt DESC + date range
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"userName\"",nullable = false,unique = true)
    private String userName;

    @Column(name = "\"fullName\"", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @Column(name = "\"passwordHash\"", nullable = false)
    private String passwordHash;

    @Column(name = "\"googleId\"", unique = true)
    private String googleId;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    private Gender gender;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "\"isEmployee\"", nullable = false, updatable = false)
    private Boolean isEmployee=false;

    @Builder.Default
    @Column(name = "\"isDeleted\"")
    private Boolean isDeleted=false;

    @Builder.Default
    @Column(name = "\"isWalletExist\"", nullable = false)
    private Boolean isWalletExist=false;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserWallet wallet;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserPreference preference;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TopUp> topUps;

    /**
     * Roles assigned to this user.
     * LAZY — only loaded when explicitly accessed (auth uses JOIN FETCH queries;
     * admin list skips roles entirely). @BatchSize batches any remaining lazy hits.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "\"UserRole\"",
            joinColumns        = @JoinColumn(name = "\"userId\""),
            inverseJoinColumns = @JoinColumn(name = "\"roleId\"")
    )
    @BatchSize(size = 25)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles;
}
