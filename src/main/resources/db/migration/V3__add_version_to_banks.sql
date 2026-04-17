-- =============================================================================
-- V3: Add optimistic-locking version column to banks
-- The Bank entity carries @Version private Long version; which Hibernate
-- requires as a BIGINT column.  The V1 baseline omitted it, so every save
-- against an existing row would throw a schema mismatch at runtime.
-- =============================================================================

ALTER TABLE banks
    ADD COLUMN IF NOT EXISTS version BIGINT;

-- Seed existing rows with version = 0 so Hibernate's first save works correctly
-- (it reads the persisted value and increments it; NULL would cause an NPE).
UPDATE banks SET version = 0 WHERE version IS NULL;
