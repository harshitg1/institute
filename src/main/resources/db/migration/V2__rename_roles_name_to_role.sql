-- V2: Rename roles.name column to 'role' to match UserRole enum mapping in the Role entity.
-- This aligns the DB schema with the JPA entity which uses @Enumerated(EnumType.STRING)
-- on a field called 'role' (column 'role').

-- Step 1: Drop unique constraint on old 'name' column
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_key;

-- Step 2: Rename column
ALTER TABLE roles RENAME COLUMN name TO role;

-- Step 3: Re-add unique constraint on renamed column
ALTER TABLE roles ADD CONSTRAINT roles_role_key UNIQUE (role);
