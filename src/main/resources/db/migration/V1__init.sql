-- Ensure pgcrypto available for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- DROP ALL TABLES (flush database)
DROP TABLE IF EXISTS video_progress CASCADE;
DROP TABLE IF EXISTS payment_orders CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS batch_transfer_log CASCADE;
DROP TABLE IF EXISTS batch_transfer_logs CASCADE;
DROP TABLE IF EXISTS batch_students CASCADE;
DROP TABLE IF EXISTS batches CASCADE;
DROP TABLE IF EXISTS password_reset_otps CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS organizations CASCADE;

-- ============================================================
-- CREATE ALL TABLES  (matches current JPA entities exactly)
-- ============================================================

CREATE TABLE organizations (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name          varchar(255) NOT NULL UNIQUE,
  active        boolean NOT NULL DEFAULT true,
  created_at    timestamp without time zone DEFAULT now(),
  updated_at    timestamp without time zone DEFAULT now(),
  created_by    uuid
);

CREATE TABLE roles (
  id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name varchar(255) NOT NULL UNIQUE
);

CREATE TABLE users (
  id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email                   varchar(255) NOT NULL UNIQUE,
  password                varchar(255) NOT NULL,
  first_name              varchar(150),
  last_name               varchar(150),
  organization_id         uuid REFERENCES organizations(id) ON DELETE SET NULL,
  role_id                 uuid NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
  student_status          varchar(20),
  enabled                 boolean NOT NULL DEFAULT true,
  account_non_expired     boolean NOT NULL DEFAULT true,
  account_non_locked      boolean NOT NULL DEFAULT true,
  credentials_non_expired boolean NOT NULL DEFAULT true,
  created_at              timestamp without time zone DEFAULT now(),
  updated_at              timestamp without time zone DEFAULT now(),
  last_login_at           timestamp without time zone
);

CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_org_id ON users(organization_id);

CREATE TABLE courses (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title           varchar(500) NOT NULL,
  description     TEXT,
  price           numeric(12, 2) DEFAULT 0.00,
  thumbnail_url   varchar(1000),
  duration_hours  integer,
  published       boolean NOT NULL DEFAULT false,
  organization_id uuid REFERENCES organizations(id) ON DELETE CASCADE,
  created_at      timestamp without time zone DEFAULT now(),
  updated_at      timestamp without time zone DEFAULT now()
);

CREATE INDEX idx_courses_org_id ON courses(organization_id);

CREATE TABLE lessons (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title           varchar(1024) NOT NULL,
  organization_id uuid
);

CREATE TABLE batches (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name            varchar(255) NOT NULL,
  duration        varchar(100),
  start_time      varchar(10),
  end_time        varchar(10),
  instructor_id   uuid REFERENCES users(id) ON DELETE SET NULL,
  organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  active          boolean NOT NULL DEFAULT true,
  created_at      timestamp without time zone DEFAULT now(),
  updated_at      timestamp without time zone DEFAULT now()
);

CREATE INDEX idx_batches_org_id ON batches(organization_id);

CREATE TABLE batch_students (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  batch_id   uuid NOT NULL REFERENCES batches(id) ON DELETE CASCADE,
  student_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  active     boolean NOT NULL DEFAULT true,
  joined_at  timestamp without time zone DEFAULT now(),
  left_at    timestamp without time zone,
  CONSTRAINT uk_batch_student UNIQUE (batch_id, student_id)
);

CREATE INDEX idx_bs_student_id ON batch_students(student_id);
CREATE INDEX idx_bs_batch_id   ON batch_students(batch_id);

CREATE TABLE attendance (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  batch_id   uuid NOT NULL REFERENCES batches(id) ON DELETE CASCADE,
  student_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  date       date NOT NULL,
  status     varchar(20) NOT NULL,
  remarks    varchar(500),
  marked_by  uuid REFERENCES users(id) ON DELETE SET NULL,
  created_at timestamp without time zone DEFAULT now(),
  updated_at timestamp without time zone,
  CONSTRAINT uk_attendance_batch_student_date UNIQUE (batch_id, student_id, date)
);

CREATE INDEX idx_att_batch_date ON attendance(batch_id, date);
CREATE INDEX idx_att_student    ON attendance(student_id);

CREATE TABLE enrollments (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id          uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  course_id        uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  organization_id  uuid REFERENCES organizations(id) ON DELETE SET NULL,
  is_purchased     boolean NOT NULL DEFAULT false,
  payment_order_id uuid,
  enrolled_at      timestamp without time zone DEFAULT now(),
  CONSTRAINT uk_enrollment_user_course UNIQUE (user_id, course_id)
);

CREATE INDEX idx_enroll_user_id   ON enrollments(user_id);
CREATE INDEX idx_enroll_course_id ON enrollments(course_id);

CREATE TABLE refresh_tokens (
  id      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  token   varchar(1024) NOT NULL UNIQUE,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_otps (
  id      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  otp     varchar(64) NOT NULL,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE video_progress (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  lesson_id       uuid NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
  seconds_watched integer NOT NULL
);

CREATE TABLE batch_transfer_log (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  from_batch_id   uuid NOT NULL REFERENCES batches(id) ON DELETE CASCADE,
  to_batch_id     uuid NOT NULL REFERENCES batches(id) ON DELETE CASCADE,
  reason          varchar(500),
  transferred_by  uuid REFERENCES users(id) ON DELETE SET NULL,
  transferred_at  timestamp without time zone DEFAULT now()
);

CREATE INDEX idx_btl_student_id ON batch_transfer_log(student_id);

CREATE TABLE payment_orders (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id          uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  course_id           uuid NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  organization_id     uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  amount              numeric(12, 2) NOT NULL,
  currency            varchar(10) NOT NULL DEFAULT 'INR',
  provider            varchar(20) NOT NULL,
  provider_order_id   varchar(255),
  provider_payment_id varchar(255),
  provider_signature  varchar(500),
  status              varchar(20) NOT NULL DEFAULT 'CREATED',
  payment_link        varchar(1000),
  failure_reason      varchar(500),
  created_at          timestamp without time zone DEFAULT now(),
  updated_at          timestamp without time zone DEFAULT now()
);

CREATE INDEX idx_po_student_id     ON payment_orders(student_id);
CREATE INDEX idx_po_org_id         ON payment_orders(organization_id);
CREATE INDEX idx_po_provider_order ON payment_orders(provider_order_id);
