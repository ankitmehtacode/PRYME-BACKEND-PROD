package com.pryme.Backend.loanproduct;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminLoanProductController {

    private final LoanProductRepository productRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<LoanProduct>> getAdminProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<LoanProduct> createProduct(@RequestBody LoanProduct product) {
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LoanProduct> updateProduct(@PathVariable Long id, @RequestBody LoanProduct updatedProduct) {
        return productRepository.findById(id).map(existing -> {
            // Because LoanProduct has many fields, we do a full replacement here, but keep ID & Create time
            updatedProduct.setId(existing.getId());
            updatedProduct.setCreatedAt(existing.getCreatedAt());
            return ResponseEntity.ok(productRepository.save(updatedProduct));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
