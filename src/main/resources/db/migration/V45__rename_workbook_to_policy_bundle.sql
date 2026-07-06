-- V45__rename_workbook_to_policy_bundle.sql

ALTER TABLE policy_bundle RENAME COLUMN workbook_hash TO policy_bundle_hash;
ALTER TABLE policy_activation_history RENAME COLUMN workbook_hash TO policy_bundle_hash;
