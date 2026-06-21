package com.busapp.userservice.model;

import com.busapp.userservice.model.enums.Theme;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"UserPreference\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private Theme theme = Theme.LIGHT;
}
