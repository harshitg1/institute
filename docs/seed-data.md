# Seed Data

This repository seeds a single deterministic demo dataset from the startup seeder in `src/main/java/com/institute/Institue/bootstrap/DatabaseSeeder.java`.
The seed now runs only when the database has no application data at all, so restarts do not overwrite or repopulate a live database.

## Login Accounts

| Email | Role | Organization | Password |
| --- | --- | --- | --- |
| `superadmin@institute.local` | `SUPER_ADMIN` | none | `SuperAdmin@123` |
| `admin@northstar.local` | `ORG_ADMIN` | `NorthStar Institute` | `OrgAdmin@123` |
| `tutor@northstar.local` | `TUTOR` | `NorthStar Institute` | `Tutor@123` |
| `student1@northstar.local` | `STUDENT` | `NorthStar Institute` | `Student@123` |
| `student2@northstar.local` | `STUDENT` | `NorthStar Institute` | `Student@123` |

## Seeded Tables

### `roles`

| ID | Role |
| --- | --- |
| `10101010-1010-1010-1010-101010101010` | `SUPER_ADMIN` |
| `20202020-2020-2020-2020-202020202020` | `ORG_ADMIN` |
| `30303030-3030-3030-3030-303030303030` | `TUTOR` |
| `40404040-4040-4040-4040-404040404040` | `STUDENT` |

### `organizations`

| ID | Name | Active | Created By |
| --- | --- | --- | --- |
| `66666666-6666-6666-6666-666666666666` | `NorthStar Institute` | `true` | `11111111-1111-1111-1111-111111111111` |

### `users`

| ID | Email | Role | Student Status |
| --- | --- | --- | --- |
| `11111111-1111-1111-1111-111111111111` | `superadmin@institute.local` | `SUPER_ADMIN` | n/a |
| `22222222-2222-2222-2222-222222222222` | `admin@northstar.local` | `ORG_ADMIN` | n/a |
| `33333333-3333-3333-3333-333333333333` | `tutor@northstar.local` | `TUTOR` | n/a |
| `44444444-4444-4444-4444-444444444444` | `student1@northstar.local` | `STUDENT` | `ACTIVE` |
| `55555555-5555-5555-5555-555555555555` | `student2@northstar.local` | `STUDENT` | `ACTIVE` |

### `courses`

| ID | Title | Published | Price |
| --- | --- | --- | --- |
| `77777777-7777-7777-7777-777777777777` | `Foundations of Mathematics` | `true` | `24999.00` |
| `88888888-8888-8888-8888-888888888888` | `Communication Skills Lab` | `false` | `18999.00` |

### `lessons`

| ID | Title |
| --- | --- |
| `90909090-9090-9090-9090-909090909090` | `Introduction to Algebra` |
| `91919191-9191-9191-9191-919191919191` | `Linear Equations Basics` |
| `92929292-9292-9292-9292-929292929292` | `Interview Communication Practice` |

### `batches`

| ID | Name | Instructor |
| --- | --- | --- |
| `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | `Mathematics Batch A` | `tutor@northstar.local` |
| `bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb` | `Communication Batch B` | `tutor@northstar.local` |

### `batch_students`

| ID | Batch | Student | Active |
| --- | --- | --- | --- |
| `cccccccc-cccc-cccc-cccc-cccccccccccc` | `Mathematics Batch A` | `student1@northstar.local` | `true` |
| `dddddddd-dddd-dddd-dddd-dddddddddddd` | `Mathematics Batch A` | `student2@northstar.local` | `false` |
| `eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee` | `Communication Batch B` | `student2@northstar.local` | `true` |

### `attendance`

| ID | Batch | Student | Date | Status |
| --- | --- | --- | --- | --- |
| `f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1` | `Mathematics Batch A` | `student1@northstar.local` | `2026-04-01` | `PRESENT` |
| `f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2` | `Mathematics Batch A` | `student2@northstar.local` | `2026-04-01` | `LATE` |
| `f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3` | `Mathematics Batch A` | `student1@northstar.local` | `2026-04-02` | `EXCUSED` |
| `f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4` | `Communication Batch B` | `student2@northstar.local` | `2026-04-03` | `ABSENT` |

### `enrollments`

| ID | User | Course | Purchased | Payment Order |
| --- | --- | --- | --- | --- |
| `eeeeeeee-0000-0000-0000-000000000001` | `student1@northstar.local` | `Foundations of Mathematics` | `true` | `dddddddd-0000-0000-0000-000000000001` |
| `eeeeeeee-0000-0000-0000-000000000002` | `student2@northstar.local` | `Communication Skills Lab` | `false` | none |

### `payment_orders`

| ID | Student | Course | Provider | Status |
| --- | --- | --- | --- | --- |
| `dddddddd-0000-0000-0000-000000000001` | `student1@northstar.local` | `Foundations of Mathematics` | `RAZORPAY` | `CAPTURED` |
| `dddddddd-0000-0000-0000-000000000002` | `student2@northstar.local` | `Communication Skills Lab` | `STRIPE` | `FAILED` |

### `refresh_tokens`

| ID | Token | User |
| --- | --- | --- |
| `ffffffff-0000-0000-0000-000000000001` | `seed-refresh-token-superadmin` | `superadmin@institute.local` |
| `ffffffff-0000-0000-0000-000000000002` | `seed-refresh-token-orgadmin` | `admin@northstar.local` |

### `password_reset_otps`

| ID | OTP | User |
| --- | --- | --- |
| `abababab-abab-abab-abab-abababababab` | `482911` | `student2@northstar.local` |

### `video_progress`

| ID | User | Lesson | Seconds Watched |
| --- | --- | --- | --- |
| `cdcdcdcd-cdcd-cdcd-cdcd-cdcdcdcdcdcd` | `student1@northstar.local` | `Introduction to Algebra` | `900` |
| `dededede-dede-dede-dede-dededededede` | `student2@northstar.local` | `Interview Communication Practice` | `300` |

### `batch_transfer_log`

| ID | Student | From Batch | To Batch | Reason |
| --- | --- | --- | --- | --- |
| `efefefef-efef-efef-efef-efefefefefef` | `student2@northstar.local` | `Mathematics Batch A` | `Communication Batch B` | `Promoted after the initial assessment.` |

## Notes

- The bootstrap is idempotent: it seeds only when the database has no application rows.
- The seeded passwords are BCrypt hashes stored in the startup seeder.
- The seed is designed for local development and demo usage, not production.
