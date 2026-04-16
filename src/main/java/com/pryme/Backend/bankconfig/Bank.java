package com.pryme.Backend.bankconfig;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "banks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String bankName;

    private String logoUrl;

    private boolean active;

    // 🧠 OPTIMISTIC LOCKING: Prevents silent last-write-wins when two admins
    // edit the same bank simultaneously. Hibernate auto-increments on save and
    // throws OptimisticLockException if the row was modified between read and write.
    @Version
    private Long version;
}