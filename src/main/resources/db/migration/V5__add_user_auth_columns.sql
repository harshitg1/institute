-- V5: Add missing user auth/profile columns to align with JPA entity

-- Password hash (nullable to avoid failing on legacy rows; seeding will set it)
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS password varchar(255);

-- Basic profile names
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS first_name varchar(150);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS last_name varchar(150);

-- Account flags
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS account_non_expired boolean NOT NULL DEFAULT true;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS account_non_locked boolean NOT NULL DEFAULT true;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS credentials_non_expired boolean NOT NULL DEFAULT true;

-- Timestamps
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS created_at timestamptz DEFAULT now();
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS updated_at timestamptz DEFAULT now();
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS last_login_at timestamptz;
