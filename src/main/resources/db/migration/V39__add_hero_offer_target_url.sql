-- Add target link URL column to hero_offer_configs for hyperlink actions on CTA
ALTER TABLE hero_offer_configs
  ADD COLUMN IF NOT EXISTS target_url VARCHAR(1024);
