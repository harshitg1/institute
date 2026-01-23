-- V4: Safely migrate or drop legacy users.username column
-- Strategy:
-- 1) If email is NULL and username is not NULL, copy username -> email.
-- 2) Ensure email is NOT NULL uniquely constrained (existing constraints may already be present).
-- 3) Drop the username column.

-- Copy username to email where email is null (be cautious: ensure username values are valid emails or adjust logic)
UPDATE users SET email = username WHERE email IS NULL AND username IS NOT NULL;

-- If you want to enforce not-null on email column, uncomment the following (do this only if you're certain every row has an email):
-- ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Drop username column if present
ALTER TABLE users DROP COLUMN IF EXISTS username;
