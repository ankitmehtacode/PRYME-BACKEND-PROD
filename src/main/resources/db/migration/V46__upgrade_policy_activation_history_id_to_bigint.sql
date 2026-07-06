-- V46__upgrade_policy_activation_history_id_to_bigint.sql

ALTER TABLE policy_activation_history ALTER COLUMN id TYPE BIGINT;
ALTER TABLE policy_bundle ALTER COLUMN id TYPE BIGINT;
