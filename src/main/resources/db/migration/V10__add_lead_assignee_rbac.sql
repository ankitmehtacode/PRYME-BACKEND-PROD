-- ═══════════════════════════════════════════════════════════════════════════════
-- V10: Add Lead Assignee and RBAC Enforcements
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE leads 
    ADD COLUMN IF NOT EXISTS assigned_to UUID;

ALTER TABLE leads 
    ADD CONSTRAINT fk_lead_assignee 
    FOREIGN KEY (assigned_to) 
    REFERENCES users(id) 
    ON DELETE SET NULL;

COMMENT ON COLUMN leads.assigned_to IS 'UUID of the company team member (User) assigned to handle this lead.';
