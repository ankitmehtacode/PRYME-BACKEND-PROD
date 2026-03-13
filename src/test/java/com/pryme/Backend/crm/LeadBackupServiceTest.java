package com.pryme.Backend.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LeadBackupServiceTest {

    @Test
    void beginAndCommitMovesWalToProcessedFolder() throws Exception {
        Path dir = Files.createTempDirectory("lead-wal");
        LeadBackupService service = new LeadBackupService(new ObjectMapper(), dir.toString());
        service.init();

        // 🧠 PRODUCTION FIX: Injected Map.of() to fulfill the new 6-parameter schema requirement
        UUID opId = service.begin(
                new LeadSubmitRequest(
                        "Ravi",
                        "9876543210",
                        new BigDecimal("500000"),
                        "personal",
                        "offer-1",
                        Map.of("cibilScore", 810) // <--- The strictly required 6th parameter (Metadata)
                ),
                "idem-key"
        );

        Path walFile = dir.resolve(opId + ".json");
        assertTrue(Files.exists(walFile));

        service.markCommitted(opId);

        assertTrue(Files.exists(dir.resolve("processed").resolve(opId + ".json")));
    }
}