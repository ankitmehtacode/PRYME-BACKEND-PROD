-- Add image URL columns to hero_offer_configs for CRM-managed banner cards (PaisaBazaar style)
ALTER TABLE hero_offer_configs
  ADD COLUMN IF NOT EXISTS banner_image_url VARCHAR(1024),
  ADD COLUMN IF NOT EXISTS hero_image_url   VARCHAR(1024);
