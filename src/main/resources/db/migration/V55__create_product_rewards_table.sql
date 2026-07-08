CREATE TABLE product_rewards (
    id UUID PRIMARY KEY,
    bank VARCHAR(100) NOT NULL,
    product_code VARCHAR(100) NOT NULL,
    icon_type VARCHAR(50) NOT NULL,
    reward_text VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_bank_product_code UNIQUE (bank, product_code)
);
