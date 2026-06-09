-- V23: Alter banks logo_url column to TEXT to support Base64 uploaded images
ALTER TABLE banks ALTER COLUMN logo_url TYPE TEXT;
