-- =============================================================================
-- V1: PRYME Baseline Schema
-- Generated from @Entity classes. Matches Hibernate's mapping exactly.
-- Uses IF NOT EXISTS so baseline-on-migrate works against live databases.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- 1. USERS (com.pryme.Backend.iam.User)
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email               VARCHAR(150) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    role                VARCHAR(20) NOT NULL DEFAULT 'USER'
                        CHECK (role IN ('SUPER_ADMIN','ADMIN','EMPLOYEE','USER')),
    full_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20),
    profile_picture_url VARCHAR(500),
    city                VARCHAR(100),
    state               VARCHAR(100),
    phone               VARCHAR(20),
    metadata            JSONB,
    version             BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- =============================================================================
-- 2. SESSION RECORDS (com.pryme.Backend.iam.SessionRecord)
-- =============================================================================
CREATE TABLE IF NOT EXISTS session_records (
    id          UUID NOT NULL PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(512),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_session_user_active ON session_records(user_id, is_active, created_at);
CREATE INDEX IF NOT EXISTS idx_session_expires ON session_records(expires_at);

-- =============================================================================
-- 3. LEADS (com.pryme.Backend.crm.Lead)
-- =============================================================================
CREATE TABLE IF NOT EXISTS leads (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_name       VARCHAR(120) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    loan_amount     NUMERIC(15,2) NOT NULL,
    loan_type       VARCHAR(30) NOT NULL,
    cibil_score     INTEGER,
    metadata        TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'NEW'
                    CHECK (status IN ('NEW','CONTACTED','CONVERTED','REJECTED')),
    offer_id        VARCHAR(50),
    idempotency_key VARCHAR(40),
    created_at      TIMESTAMP,
    CONSTRAINT uq_leads_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_leads_status ON leads(status);
CREATE INDEX IF NOT EXISTS idx_leads_created_at ON leads(created_at);
CREATE INDEX IF NOT EXISTS idx_leads_phone ON leads(phone);

-- =============================================================================
-- 4. LOAN APPLICATIONS (com.pryme.Backend.crm.LoanApplication)
-- =============================================================================
CREATE TABLE IF NOT EXISTS loan_applications (
    id                      UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    application_id          VARCHAR(20) NOT NULL,
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    assignee_id             UUID REFERENCES users(id) ON DELETE SET NULL,
    loan_type               VARCHAR(50) NOT NULL,
    selected_bank           VARCHAR(50),
    requested_amount        NUMERIC(15,2) DEFAULT 0,
    declared_cibil_score    INTEGER,
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','SUBMITTED','PROCESSING','VERIFIED','APPROVED','REJECTED')),
    completion_percentage   INTEGER NOT NULL DEFAULT 25,
    current_step            VARCHAR(50) NOT NULL DEFAULT 'BANK_SELECTION',
    metadata                JSONB,
    version                 BIGINT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_loan_app_application_id UNIQUE (application_id)
);

CREATE INDEX IF NOT EXISTS idx_loan_app_status ON loan_applications(status);
CREATE INDEX IF NOT EXISTS idx_loan_app_applicant ON loan_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_loan_app_bank ON loan_applications(selected_bank);

-- =============================================================================
-- 5. APPLICATION STATUS HISTORY (com.pryme.Backend.crm.ApplicationStatusHistory)
-- =============================================================================
CREATE TABLE IF NOT EXISTS application_status_history (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    application_id  VARCHAR(255),
    status          VARCHAR(20)
                    CHECK (status IS NULL OR status IN ('DRAFT','SUBMITTED','PROCESSING','VERIFIED','APPROVED','REJECTED')),
    old_status      VARCHAR(255),
    new_status      VARCHAR(255),
    notes           VARCHAR(255),
    created_at      TIMESTAMPTZ,
    changed_by      UUID,
    changed_at      TIMESTAMPTZ
);

-- =============================================================================
-- 6. IDEMPOTENCY KEYS (com.pryme.Backend.crm.IdempotencyKey)
-- =============================================================================
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id              BIGSERIAL PRIMARY KEY,
    key_hash        VARCHAR(64) NOT NULL,
    response_body   TEXT NOT NULL,
    http_status     INTEGER NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_idempotency_keys_key_hash UNIQUE (key_hash)
);

-- =============================================================================
-- 7. DOCUMENT RECORDS (com.pryme.Backend.document.DocumentRecord)
-- =============================================================================
CREATE TABLE IF NOT EXISTS document_records (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    loan_application_id UUID NOT NULL REFERENCES loan_applications(id) ON DELETE CASCADE,
    doc_type            VARCHAR(255) NOT NULL,
    original_filename   VARCHAR(255) NOT NULL,
    content_type        VARCHAR(255) NOT NULL,
    file_size           BIGINT NOT NULL,
    storage_path        VARCHAR(255) NOT NULL,
    checksum            VARCHAR(64) NOT NULL,
    uploaded_by         UUID NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    status              VARCHAR(50) NOT NULL DEFAULT 'AWAITING_UPLOAD'
                        CHECK (status IN ('AWAITING_UPLOAD','UPLOADED','FAILED')),
    uploaded_at         TIMESTAMPTZ,
    s3_object_key       VARCHAR(255),
    CONSTRAINT uq_document_records_storage_path UNIQUE (storage_path)
);

CREATE INDEX IF NOT EXISTS idx_doc_application ON document_records(loan_application_id);
CREATE INDEX IF NOT EXISTS idx_doc_checksum ON document_records(checksum);

-- =============================================================================
-- 8. OUTBOX RECORDS (com.pryme.Backend.outbox.OutboxRecord)
-- =============================================================================
CREATE TABLE IF NOT EXISTS outbox_records (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    version         BIGINT,
    aggregate_type  VARCHAR(50) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','PROCESSING','PROCESSED','FAILED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ,
    error_message   VARCHAR(1000),
    retry_count     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_records(status, created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_status_updated ON outbox_records(status, updated_at);

-- =============================================================================
-- 9. BANKS (com.pryme.Backend.bankconfig.Bank)
-- =============================================================================
CREATE TABLE IF NOT EXISTS banks (
    id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    bank_name   VARCHAR(255) NOT NULL,
    logo_url    VARCHAR(255),
    active      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_banks_bank_name UNIQUE (bank_name)
);

-- =============================================================================
-- 10. TESTIMONIALS (com.pryme.Backend.cms.Testimonial)
-- =============================================================================
CREATE TABLE IF NOT EXISTS testimonials (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    role            VARCHAR(120) NOT NULL,
    text            VARCHAR(1200) NOT NULL,
    rating          INTEGER NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    featured        BOOLEAN NOT NULL DEFAULT FALSE,
    display_order   INTEGER NOT NULL DEFAULT 100,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_testimonials_active ON testimonials(active);
CREATE INDEX IF NOT EXISTS idx_testimonials_featured_order ON testimonials(featured, display_order);

-- =============================================================================
-- 11. LOAN PRODUCTS (com.pryme.Backend.loanproduct.entity.LoanProduct)
-- =============================================================================
CREATE TABLE IF NOT EXISTS loan_products (
    id                      BIGSERIAL PRIMARY KEY,
    product_code            VARCHAR(20) NOT NULL,
    product_name            VARCHAR(200) NOT NULL,
    loan_type               VARCHAR(50) NOT NULL,
    lender_id               BIGINT NOT NULL,
    lender_name             VARCHAR(100) NOT NULL,
    interest_type           VARCHAR(20) NOT NULL,
    min_cibil               INTEGER NOT NULL,
    max_cibil               INTEGER NOT NULL,
    roi                     NUMERIC(6,4) NOT NULL,
    processing_fee          NUMERIC(6,4),
    prepayment_charges      NUMERIC(6,4),
    foreclosure_charges     NUMERIC(6,4),
    login_fees              NUMERIC(12,2),
    legal_technical_charges NUMERIC(12,2),
    other_expense           NUMERIC(12,2),
    stamp_duties            NUMERIC(12,2),
    min_tenure_months       INTEGER NOT NULL,
    max_tenure_months       INTEGER NOT NULL,
    min_loan_amount         NUMERIC(15,2) NOT NULL,
    max_loan_amount         NUMERIC(15,2) NOT NULL,
    kyc_requirement         VARCHAR(255),
    income_proof            VARCHAR(255),
    bank_statement_months   INTEGER,
    itr_requirement_years   INTEGER,
    salary_slip_months      INTEGER,
    gst_required_months     INTEGER,
    residence_profile       VARCHAR(255),
    additional_docs         VARCHAR(255),
    max_emi_nmi_ratio       NUMERIC(5,4),
    ltv                     NUMERIC(5,4),
    obligation_treatment    VARCHAR(255),
    dpd_allowed             BOOLEAN,
    write_off_allowed       BOOLEAN,
    settlement_allowed      BOOLEAN,
    risk_category           VARCHAR(20),
    occupation              VARCHAR(255),
    employer_type           VARCHAR(255),
    nature_of_business      VARCHAR(255),
    industry                VARCHAR(255),
    pincode_restrictions    VARCHAR(255),
    rejection_codes         VARCHAR(255),
    auto_reject_conditions  VARCHAR(255),
    campaign_name           VARCHAR(100),
    offer_type              VARCHAR(50),
    offer_details           VARCHAR(255),
    notes                   VARCHAR(255),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_loan_products_product_code UNIQUE (product_code)
);

-- =============================================================================
-- 12. ELIGIBILITY CONDITIONS (com.pryme.Backend.eligibility.entity.EligibilityCondition)
-- =============================================================================
CREATE TABLE IF NOT EXISTS eligibility_conditions (
    id                      BIGSERIAL PRIMARY KEY,
    product_id              BIGINT NOT NULL,
    product_code            VARCHAR(20) NOT NULL,
    employment_type         VARCHAR(50),
    min_age                 INTEGER,
    max_age                 INTEGER,
    min_income              NUMERIC(12,2),
    income_type             VARCHAR(50),
    work_exp_years          INTEGER,
    business_age_years      INTEGER,
    cibil_min               INTEGER,
    foir_max                NUMERIC(5,4),
    property_type           VARCHAR(255),
    city_tier               VARCHAR(20),
    profile_restrictions    VARCHAR(255),
    notes                   VARCHAR(255),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
