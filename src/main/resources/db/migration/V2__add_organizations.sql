-- Create organizations table and add organization_id columns to relevant tables

CREATE TABLE IF NOT EXISTS organizations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name varchar(255) NOT NULL UNIQUE,
  created_by uuid
);

-- Add organization_id to users, courses, lessons, enrollments, video_progress
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS organization_id uuid;
ALTER TABLE IF EXISTS courses ADD COLUMN IF NOT EXISTS organization_id uuid;
ALTER TABLE IF EXISTS lessons ADD COLUMN IF NOT EXISTS organization_id uuid;
ALTER TABLE IF EXISTS enrollments ADD COLUMN IF NOT EXISTS organization_id uuid;
ALTER TABLE IF EXISTS video_progress ADD COLUMN IF NOT EXISTS organization_id uuid;

-- Optionally set default organization for existing rows (left null to be handled by app)

