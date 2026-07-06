-- V43__create_policy_bundle_and_activation_history.sql

CREATE TABLE IF NOT EXISTS policy_bundle (
    id SERIAL PRIMARY KEY,
    bundle_id VARCHAR(255) NOT NULL UNIQUE,
    version VARCHAR(255),
    combined_hash VARCHAR(255),
    workbook_hash VARCHAR(255),
    state VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE,
    certification_id VARCHAR(255),
    active BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS policy_activation_history (
    id SERIAL PRIMARY KEY,
    activation_id VARCHAR(255) NOT NULL UNIQUE,
    bundle_id VARCHAR(255),
    bundle_hash VARCHAR(255),
    policy_version VARCHAR(255),
    state VARCHAR(255),
    activated_by VARCHAR(255),
    approved_by VARCHAR(255),
    activated_at TIMESTAMP WITH TIME ZONE,
    git_commit VARCHAR(255),
    certification_id VARCHAR(255),
    workbook_hash VARCHAR(255),
    rollback_bundle VARCHAR(255),
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE
);
