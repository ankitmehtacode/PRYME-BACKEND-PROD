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

    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    private boolean active;

    // 🧠 LENDER CODE BRIDGE: loan_products.lender_id is BIGINT (modular monolith rule — no FK).
    // This stable numeric code lets the frontend map a Bank UUID → a numeric lenderId
    // without fragile name-hashing hacks. Auto-generated via banks_lender_code_seq.
    @Column(name = "lender_code", nullable = false, unique = true, insertable = false, updatable = false)
    private Long lenderCode;

    // 🧠 OPTIMISTIC LOCKING: Prevents silent last-write-wins when two admins
    // edit the same bank simultaneously. Hibernate auto-increments on save and
    // throws OptimisticLockException if the row was modified between read and write.
    @Version
    private Long version;
}