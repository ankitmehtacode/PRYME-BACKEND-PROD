-- V24: Fix banks with null version column and set default to 0
UPDATE banks SET version = 0 WHERE version IS NULL;
ALTER TABLE banks ALTER COLUMN version SET DEFAULT 0;
