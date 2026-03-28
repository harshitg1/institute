# Institute API Documentation

This document reflects the API surface implemented in the current codebase.

Base URL:

```text
http://localhost:8080
```

## Authentication

Most endpoints require:

```http
Authorization: Bearer <access-token>
```

Refresh flow also reads an HTTP-only cookie:

```text
refreshToken=<token>
```

JWT access token claims currently include:

- `sub`: user email
- `organization_id`: tenant UUID when the user belongs to an organization
- `role_id`: role UUID
- `roles`: array of role names
- `token_type`: `ACCESS`

## Roles

The codebase uses these role names:

- `SUPER_ADMIN`
- `ORG_ADMIN`
- `INSTRUCTOR`
- `STUDENT`

## Response Format

Many controller methods return the shared `ApiResponse<T>` envelope:

### Success

```json
{
  "status": "SUCCESS",
  "data": {},
  "message": "Optional message",
  "timestamp": "2026-03-28T10:00:00Z"
}
```

### Error

```json
{
  "status": "ERROR",
  "message": "Human-readable message",
  "code": "ERROR_CODE",
  "timestamp": "2026-03-28T10:00:00Z"
}
```

### Validation Error

```json
{
  "status": "ERROR",
  "code": "VALIDATION_FAILED",
  "errors": {
    "fieldName": "validation message"
  },
  "timestamp": "2026-03-28T10:00:00Z"
}
```

Some controllers do not use `ApiResponse<T>` and return raw entities or DTOs directly. Those cases are noted below.

## Security Notes

The route authorization implemented in `SecurityConfig` is path-based. A few controllers are mounted on paths that are less strict than their names suggest.

Current behavior to be aware of:

- `/api/admin/**` requires `ROLE_ORG_ADMIN` or `ROLE_SUPER_ADMIN`
- `/api/superadmin/**` requires `ROLE_SUPER_ADMIN`
- `/api/student/**` requires `ROLE_STUDENT`
- `/api/payments/initiate` requires `ROLE_STUDENT`
- `/api/courses/**` requires any authenticated user
- `/api/payments/admin/**` is not covered by `/api/admin/**`, so it falls through to general authentication
- `/api/org/**` also falls through to general authentication

## 1. Auth

Base path:

```text
/api/auth
```

### POST `/api/auth/register`

Creates a new organization and an admin user, then returns access and refresh tokens.

Auth: public

Request body:

```json
{
  "email": "admin@example.com",
  "password": "StrongPassword123",
  "firstName": "Alice",
  "lastName": "Admin"
}
```

Response body:

```json
{
  "organizationId": "8ed5d7bc-8ec8-4e63-9f17-808d9a802d64",
  "adminUserId": "0b55c0bf-a37c-4180-bafe-b0dfb6b145fd",
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "role": "ORG_ADMIN"
}
```

Important implementation detail:

- `RegisterRequest` has no organization name field
- the service currently creates the organization with `name = firstName`

### POST `/api/auth/login`

Authenticates by email and password.

Auth: public

Request body:

```json
{
  "email": "admin@example.com",
  "password": "StrongPassword123"
}
```

Response body:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "organizationId": "8ed5d7bc-8ec8-4e63-9f17-808d9a802d64",
  "role": "ORG_ADMIN"
}
```

Additional behavior:

- sets `refreshToken` as an HTTP-only cookie
- cookie is marked `Secure`
- cookie max age is 7 days

### POST `/api/auth/refresh`

Reads the refresh token from cookies and issues a new access token.

Auth: public

Request body: none

Required cookie:

```text
refreshToken=<token>
```

Response body:

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "same-or-rotated-refresh-token",
  "organizationId": "8ed5d7bc-8ec8-4e63-9f17-808d9a802d64",
  "role": "ORG_ADMIN"
}
```

## 2. Super Admin Organization Management

Base path:

```text
/api/superadmin/organizations
```

Auth: `ROLE_SUPER_ADMIN`

These endpoints return raw entities or Spring `Page` objects, not `ApiResponse<T>`.

### POST `/api/superadmin/organizations`

Creates an organization and its admin user.

Request body:

```json
{
  "name": "Alpha Academy",
  "adminEmail": "owner@alpha.com",
  "adminPassword": "StrongPassword123",
  "adminFirstName": "Owner",
  "adminLastName": "Admin"
}
```

Response body:

```json
{
  "id": "organization-uuid",
  "name": "Alpha Academy",
  "active": true,
  "createdAt": "2026-03-28T10:00:00Z",
  "updatedAt": "2026-03-28T10:00:00Z",
  "createdBy": null
}
```

### GET `/api/superadmin/organizations`

Returns paginated organizations.

Query params:

- `page`
- `size`
- `sort`

### GET `/api/superadmin/organizations/admins`

Returns paginated organization admins as `UserResponse`.

### GET `/api/superadmin/organizations/{id}`

Returns a single organization or `404`.

### PUT `/api/superadmin/organizations/{id}`

Updates organization name using the raw `Organization` payload.

### PUT `/api/superadmin/organizations/{id}/activate`

Sets `active=true`.

### PUT `/api/superadmin/organizations/{id}/deactivate`

Sets `active=false`.

### DELETE `/api/superadmin/organizations/{id}`

Deletes the organization.

Response: `204 No Content`

## 3. Organization User Management

Base path:

```text
/api/org
```

Auth: currently any authenticated user can reach the path, but the controller requires tenant context and is clearly intended for organization-scoped administration.

Important implementation note:

- `UserServiceImpl` is currently a stub
- create returns `null`
- list returns an empty list

### POST `/api/org/users`

Request body:

```json
{
  "email": "teacher@example.com",
  "roles": "INSTRUCTOR"
}
```

Behavior:

- returns `403` if no tenant context is present
- returns `400` because the service currently returns `null`

### GET `/api/org/users`

Behavior:

- returns `403` if no tenant context is present
- otherwise returns an empty array with the current implementation

## 4. Student Management

Base path:

```text
/api/admin/students
```

Auth: `ROLE_ORG_ADMIN` or `ROLE_SUPER_ADMIN`

### POST `/api/admin/students`

Creates a student, assigns an active batch membership, and optionally assigns courses.

Request body:

```json
{
  "email": "student@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "batchId": "batch-uuid",
  "courseIds": ["course-uuid-1", "course-uuid-2"]
}
```

Response:

```json
{
  "status": "SUCCESS",
  "data": {
    "id": "student-uuid",
    "email": "student@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "status": "ACTIVE",
    "batchId": "batch-uuid",
    "batchName": "Morning Batch",
    "courses": [
      {
        "id": "course-uuid-1",
        "title": "Trading Basics"
      }
    ],
    "createdAt": "2026-03-28T10:00:00Z"
  },
  "timestamp": "2026-03-28T10:00:00Z"
}
```

Important implementation detail:

- newly created students get the hard-coded password `defaultPassword123`

### GET `/api/admin/students`

Lists students in the current tenant.

### GET `/api/admin/students/{id}`

Returns one student.

### PUT `/api/admin/students/{id}`

Updates first name, last name, and optionally email.

The controller accepts `CreateStudentRequest`, but the service only updates student profile fields. `batchId` and `courseIds` are not processed here.

### DELETE `/api/admin/students/{id}`

Marks the student as inactive and disables the account.

Response message:

```json
{
  "status": "SUCCESS",
  "data": null,
  "message": "Student deactivated successfully"
}
```

### PUT `/api/admin/students/{id}/status`

Request body:

```json
{
  "status": "SUSPENDED"
}
```

Allowed values:

- `ACTIVE`
- `INACTIVE`
- `SUSPENDED`
- `GRADUATED`

### POST `/api/admin/students/{id}/transfer`

Transfers the student to a different batch.

Request body:

```json
{
  "targetBatchId": "new-batch-uuid",
  "reason": "Schedule conflict"
}
```

Response data:

```json
{
  "studentId": "student-uuid",
  "studentName": "John Doe",
  "previousBatchId": "old-batch-uuid",
  "previousBatchName": "Morning Batch",
  "newBatchId": "new-batch-uuid",
  "newBatchName": "Evening Batch",
  "reason": "Schedule conflict",
  "transferredAt": "2026-03-28T10:00:00Z"
}
```

### POST `/api/admin/students/{id}/courses`

Assigns one or more courses.

Request body:

```json
{
  "courseIds": ["course-uuid-1", "course-uuid-2"]
}
```

### DELETE `/api/admin/students/{studentId}/courses/{courseId}`

Removes a course enrollment from the student.

## 5. Batch Management

Base path:

```text
/api/admin/batches
```

Auth: `ROLE_ORG_ADMIN` or `ROLE_SUPER_ADMIN`

### POST `/api/admin/batches`

Request body:

```json
{
  "name": "Morning Batch",
  "instructorId": "instructor-uuid",
  "duration": "3 months",
  "startTime": "09:00",
  "endTime": "12:00"
}
```

Response data:

```json
{
  "id": "batch-uuid",
  "name": "Morning Batch",
  "instructorId": "instructor-uuid",
  "instructorName": "Jane Teacher",
  "duration": "3 months",
  "startTime": "09:00",
  "endTime": "12:00",
  "studentCount": 0,
  "active": true,
  "createdAt": "2026-03-28T10:00:00Z",
  "students": null
}
```

### GET `/api/admin/batches`

Lists tenant batches.

### GET `/api/admin/batches/{id}`

Returns full batch details and active students.

### PUT `/api/admin/batches/{id}`

Updates batch fields.

### DELETE `/api/admin/batches/{id}`

Deletes a batch only when it has zero active students.

Error code on non-empty batch:

```json
{
  "status": "ERROR",
  "message": "Cannot delete batch with 3 active students. Transfer them first.",
  "code": "BATCH_NOT_EMPTY"
}
```

### GET `/api/admin/batches/{id}/students`

Lists active students in the batch.

## 6. Attendance

Attendance is implemented inside `BatchController`, not under a separate `/api/admin/attendance` controller.

Base path:

```text
/api/admin/batches
```

Auth: `ROLE_ORG_ADMIN` or `ROLE_SUPER_ADMIN`

### POST `/api/admin/batches/{batchId}/attendance`

Marks attendance for the batch.

Request body:

```json
{
  "batchId": "batch-uuid",
  "date": "2026-03-28",
  "records": [
    {
      "studentId": "student-uuid-1",
      "status": "PRESENT",
      "remarks": "On time"
    },
    {
      "studentId": "student-uuid-2",
      "status": "LATE",
      "remarks": "10 min late"
    }
  ]
}
```

Allowed status values:

- `PRESENT`
- `ABSENT`
- `LATE`
- `EXCUSED`

### GET `/api/admin/batches/{batchId}/attendance`

Returns attendance summary for the batch.

### GET `/api/admin/batches/{batchId}/attendance/date/{date}`

Returns attendance for a specific date.

`date` format:

```text
YYYY-MM-DD
```

### GET `/api/admin/batches/attendance/student/{studentId}`

Returns a list of attendance records for one student.

### PUT `/api/admin/batches/attendance/{id}`

Updates an attendance record.

Query parameters:

- `status` optional
- `remarks` optional

Example:

```http
PUT /api/admin/batches/attendance/attendance-uuid?status=EXCUSED&remarks=Medical
```

## 7. Courses

Base path:

```text
/api/courses
```

Auth: any authenticated user according to security config

Important note:

- the API is split between public-style course browsing and organization course management
- organization admin actions are not under `/api/admin/courses`; they live under `/api/courses`

### GET `/api/courses`

Returns published courses.

### GET `/api/courses/{id}`

Returns a single course.

### GET `/api/courses/student/my-courses`

Returns courses enrolled by the authenticated user.

Despite the name, the path is under `/api/courses`, not `/api/student`.

### POST `/api/courses`

Creates a course using the authenticated user organization.

Request body:

```json
{
  "title": "Advanced Trading Strategies",
  "description": "Master risk and execution",
  "price": 4999.00,
  "thumbnailUrl": "https://example.com/course.png",
  "durationHours": 40,
  "published": true
}
```

### GET `/api/courses/admin`

Lists all courses for the authenticated user's organization.

### PUT `/api/courses/{id}`

Updates a course.

Important implementation detail:

- `published` always gets overwritten from the request boolean
- if omitted by the client, it defaults to `false` in the DTO builder path

### DELETE `/api/courses/{id}`

Deletes a course if it has no enrollments.

Error code on active enrollments:

```json
{
  "status": "ERROR",
  "message": "Cannot delete course with 2 active enrollments",
  "code": "COURSE_HAS_ENROLLMENTS"
}
```

## 8. Payments

Base path:

```text
/api/payments
```

### POST `/api/payments/initiate`

Creates a payment order for a course purchase.

Auth: `ROLE_STUDENT`

Request body:

```json
{
  "courseId": "course-uuid",
  "provider": "RAZORPAY"
}
```

Supported provider values:

- `RAZORPAY`
- `STRIPE`

Response data:

```json
{
  "orderId": "payment-order-uuid",
  "providerOrderId": "order_abcd1234",
  "provider": "RAZORPAY",
  "amount": 4999.00,
  "currency": "INR",
  "courseId": "course-uuid",
  "courseTitle": "Advanced Trading Strategies",
  "studentId": "student-uuid",
  "studentName": "John Doe",
  "paymentLink": "https://rzp.io/i/order_abcd1234",
  "status": "CREATED",
  "failureReason": null,
  "createdAt": "2026-03-28T10:00:00Z"
}
```

Behavior:

- rejects non-student users
- rejects unpublished courses
- rejects duplicate enrollments
- rejects duplicate pending payments

Possible business error codes:

- `NOT_A_STUDENT`
- `COURSE_NOT_PUBLISHED`
- `ALREADY_ENROLLED`
- `PAYMENT_PENDING`
- `UNSUPPORTED_PROVIDER`

### GET `/api/payments/verify/{orderId}`

Verifies payment with the selected provider and updates the order.

Auth: any authenticated user

On successful capture:

- payment order status becomes `CAPTURED`
- enrollment is automatically created with `purchased=true`

On failure:

- payment order status becomes `FAILED`
- `failureReason` may be populated

### POST `/api/payments/webhook/{provider}`

Processes payment provider webhooks.

Auth: public

Path parameter examples:

- `razorpay`
- `stripe`

Response:

```text
OK
```

Current implementation note:

- webhook signature checks are permissive in the development adapters
- webhook payload parsing is also simulated

### GET `/api/payments/admin`

Returns payment orders for the authenticated user's organization.

Auth: currently any authenticated user can reach this path

### GET `/api/payments/admin/{id}`

Returns a single payment order.

Auth: currently any authenticated user can reach this path

### GET `/api/payments/admin/summary`

Returns organization revenue summary.

Auth: currently any authenticated user can reach this path

Response data:

```json
{
  "totalRevenue": 149970.00,
  "totalTransactions": 30,
  "successfulTransactions": 28,
  "failedTransactions": 2,
  "revenueByMonth": [
    {
      "month": "2026-03",
      "revenue": 74985.00,
      "count": 15
    }
  ],
  "revenueByCourse": [
    {
      "courseId": "course-uuid",
      "courseTitle": "Trading Basics",
      "revenue": 99980.00,
      "enrollments": 20
    }
  ]
}
```

## 9. Debug

Base path:

```text
/api/debug
```

Auth: any authenticated user

### GET `/api/debug/whoami`

Returns current principal, granted authorities, and tenant context.

Example response:

```json
{
  "principal": "student@example.com",
  "roles": ["ROLE_STUDENT"],
  "organizationId": "organization-uuid"
}
```

If the full `User` entity is loaded as principal, `principal` may serialize as an object string representation rather than plain email.

## 10. Health

### GET `/actuator/health`

Auth: public

Standard Spring Boot actuator health endpoint.

### GET `/api/health/db`

Auth: authenticated

Checks the datasource with `SELECT 1`.

Success response:

```json
{
  "status": "UP",
  "result": 1
}
```

Failure response:

```json
{
  "status": "DOWN",
  "error": "error message"
}
```

## Known Implementation Gaps

These are worth knowing while integrating against this backend:

- `SecurityConfig` permits `/api/superadmin/seed` and `/api/superadmin/reset-seed`, but those endpoints do not exist
- `OrganizationController` exists, but its service is not implemented
- payment adapters are simulated rather than using real provider SDKs
- both Flyway migrations and `spring.jpa.hibernate.ddl-auto=update` are enabled
- the initial Flyway migration drops tables before recreating them
- repository tests are currently not passing compilation according to `maven_test_output.log`
