package com.busapp.userservice.model;

import com.busapp.userservice.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"Notification\"",
        indexes = {
                @Index(name = "idx_notification_user",    columnList = "userId"),
                @Index(name = "idx_notification_read",    columnList = "isRead"),
                @Index(name = "idx_notification_created", columnList = "createdAt")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private NotificationType type;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "\"isRead\"", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
