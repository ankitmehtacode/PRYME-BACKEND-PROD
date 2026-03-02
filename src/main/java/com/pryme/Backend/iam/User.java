package com.pryme.Backend.iam;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash; // Will be BCrypt encrypted [cite: 85]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; //

    private String fullName;
    private String phone;
}