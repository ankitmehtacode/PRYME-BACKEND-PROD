package com.pryme.Backend.bankconfig;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/banks")
public class BankController {

    // In a real flow, you inject the BankRepository here

    @PostMapping
    public String createBank(@RequestBody Bank bank) {
        // Full CRUD: Create
        return "Bank " + bank.getBankName() + " created successfully in CMS.";
    }

    @GetMapping
    public List<Bank> getAllBanks() {
        // Full CRUD: Read
        return List.of(new Bank(UUID.randomUUID(), "L&T Finance", "/logos/lnt.png", true));
    }

    @PutMapping("/{id}")
    public String updateBank(@PathVariable UUID id, @RequestBody Bank bank) {
        // Full CRUD: Update
        return "Bank " + id + " updated successfully.";
    }

    @DeleteMapping("/{id}")
    public String deleteBank(@PathVariable UUID id) {
        // Full CRUD: Delete
        return "Bank " + id + " deleted from system.";
    }
}