package com.pryme.Backend.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LeadBackupServiceTest {

    @Test
    void beginAndCommitMovesWalToProcessedFolder() throws Exception {
        Path dir = Files.createTempDirectory("lead-wal");
        LeadBackupService service = new LeadBackupService(new ObjectMapper(), dir.toString());
        service.init();

        UUID opId = service.begin(
                new LeadSubmitRequest("Ravi", "9876543210", new BigDecimal("500000"), "personal", "offer-1"),
                "idem-key"
        );

        Path walFile = dir.resolve(opId + ".json");
        assertTrue(Files.exists(walFile));

        service.markCommitted(opId);

        assertTrue(Files.exists(dir.resolve("processed").resolve(opId + ".json")));
    }
}
