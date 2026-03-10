package com.pryme.Backend.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, UUID> {
    List<DocumentRecord> findAllByLoanApplication_ApplicationIdOrderByCreatedAtDesc(String applicationId);
}
