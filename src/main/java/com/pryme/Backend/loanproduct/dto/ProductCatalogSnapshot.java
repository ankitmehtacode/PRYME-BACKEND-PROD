package com.pryme.Backend.loanproduct.dto;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import java.time.Instant;
import java.util.List;

/**
 * 📦 Read-only immutable product catalog snapshot.
 */
public record ProductCatalogSnapshot(
    String bundleId,
    String bundleHash,
    Instant generatedAt,
    List<LoanProduct> products,
    int productCount
) {}
