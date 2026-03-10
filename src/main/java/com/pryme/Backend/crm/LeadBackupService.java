package com.pryme.Backend.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LeadBackupService {

    private final ObjectMapper objectMapper;
    private final Path walDir;
    private final Path processedDir;
    private final Map<UUID, Path> inFlight = new ConcurrentHashMap<>();

    public LeadBackupService(
            ObjectMapper objectMapper,
            @Value("${app.leads.backup-dir:./var/lead-wal}") String backupDir
    ) {
        this.objectMapper = objectMapper;
        this.walDir = Path.of(backupDir).toAbsolutePath().normalize();
        this.processedDir = walDir.resolve("processed").normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(walDir);
            Files.createDirectories(processedDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize lead backup WAL directories", e);
        }
    }

    public UUID begin(LeadSubmitRequest request, String idempotencyKey) {
        UUID opId = UUID.randomUUID();
        LeadWalRecord wal = new LeadWalRecord(opId, Instant.now(), idempotencyKey, request);
        Path file = walDir.resolve(opId + ".json");

        try {
            objectMapper.writeValue(file.toFile(), wal);
            inFlight.put(opId, file);
            return opId;
        } catch (IOException e) {
            throw new RuntimeException("Lead backup WAL write failed", e);
        }
    }

    public void markCommitted(UUID opId) {
        Path file = inFlight.remove(opId);
        if (file == null) {
            return;
        }

        Path target = processedDir.resolve(file.getFileName().toString());
        try {
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Do not fail request for post-commit archival issues.
        }
    }
}

record LeadWalRecord(
        UUID operationId,
        Instant receivedAt,
        String idempotencyKey,
        LeadSubmitRequest payload
) {
}
