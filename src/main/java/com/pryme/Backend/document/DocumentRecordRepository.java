package com.pryme.Backend.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, UUID> {
    List<DocumentRecord> findAllByLoanApplication_ApplicationIdOrderByCreatedAtDesc(String applicationId);
    Optional<DocumentRecord> findByS3ObjectKey(String s3ObjectKey);
}
