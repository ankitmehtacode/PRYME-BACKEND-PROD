-- =============================================================================
-- V4: Add status column to idempotency_keys
-- The IdempotencyKey entity declares:
--   @Column(name = "status", nullable = false, length = 20)
--   private String status;
-- This column was never included in the V1 baseline, causing Hibernate's
-- schema validator to throw SchemaManagementException on startup.
-- =============================================================================

ALTER TABLE idempotency_keys
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'SAVED';

-- Remove the default after back-filling so future inserts must supply a value
-- explicitly (matching the entity's nullable=false contract).
ALTER TABLE idempotency_keys
    ALTER COLUMN status DROP DEFAULT;
