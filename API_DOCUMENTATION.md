# API Documentation

Current endpoint reference for the Institute backend. Paths and payloads below are taken from the controller classes in `src/main/java/com/institute/Institue/controller`.

## Common Response Shape

Most application endpoints return:

```json
{
  "status": "SUCCESS",
  "data": {},
  "message": null,
  "code": null,
  "errors": null,
  "timestamp": "2026-04-04T00:00:00Z"
}
```

Validation failures use `ApiResponse.validationError(...)`, and error responses use `ApiResponse.error(...)`.

## Authentication

### `POST /api/auth/register`

Creates a new organization and its first org admin.

Request body:

```json
{
  "email": "admin@example.com",
  "password": "ChangeMe123!",
  "firstName": "NorthStar",
  "lastName": "Admin"
}
```

Response body:

```json
{
  "organizationId": "uuid",
  "adminUserId": "uuid",
  "accessToken": "...",
  "refreshToken": "...",
  "role": "ORG_ADMIN"
}
```

### `POST /api/auth/login`

Authenticates a user and returns access and refresh tokens.

Request body:

```json
{
  "email": "admin@northstar.local",
  "password": "OrgAdmin@123"
}
```

Response body:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "organizationId": "uuid",
  "role": "ORG_ADMIN"
}
```

The refresh token is also set as an HTTP-only cookie named `refreshToken`.

### `POST /api/auth/refresh`

Reads the `refreshToken` cookie and returns a new access token.

No request body.

Response body:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "organizationId": "uuid",
  "role": "ORG_ADMIN"
}
```

## Super Admin

Base path: `/api/superadmin/organizations`

### `POST /api/superadmin/organizations`

Creates an organization and its admin user.

Request body:

```json
{
  "name": "NorthStar Institute",
  "adminEmail": "admin@northstar.local",
  "adminPassword": "OrgAdmin@123",
  "adminFirstName": "Nisha",
  "adminLastName": "Sharma"
}
```

Response body: `Organization`

```json
{
  "id": "uuid",
  "name": "NorthStar Institute",
  "active": true,
  "createdBy": "uuid",
  "createdAt": "2026-04-04T00:00:00Z",
  "updatedAt": "2026-04-04T00:00:00Z"
}
```

### `GET /api/superadmin/organizations`

Returns a paginated list of organizations.

Query params are standard Spring pageable params such as `page`, `size`, and `sort`.

Response body: `Page<Organization>`

### `GET /api/superadmin/organizations/{id}`

Returns one organization by UUID.

### `PUT /api/superadmin/organizations/{id}`

Updates the organization name and/or active fields using the `Organization` model.

Request body: `Organization`

```json
{
  "name": "NorthStar Institute",
  "active": true
}
```

### `PUT /api/superadmin/organizations/{id}/activate`

Activates an organization.

### `PUT /api/superadmin/organizations/{id}/deactivate`

Deactivates an organization.

### `DELETE /api/superadmin/organizations/{id}`

Deletes an organization.

### `GET /api/superadmin/organizations/admins`

Returns paginated org admins across the platform.

Response body: `Page<UserResponse>`

## Organization Admin Users

Base path: `/api/org`

Tenant is resolved from the authenticated user context.

### `POST /api/org/users`

Creates a user inside the current organization.

Request body:

```json
{
  "email": "tutor@northstar.local",
  "roles": "TUTOR",
  "firstName": "Arjun",
  "lastName": "Mehta",
  "password": "Tutor@123"
}
```

If `password` is omitted, the service uses `ChangeMe123!`.

### `GET /api/org/users`

Lists all users in the current organization.

Response body: `List<UserResponse>`

## Admin Students

Base path: `/api/admin/students`

### `POST /api/admin/students`

Creates a student in the current organization.

Request body:

```json
{
  "email": "student1@northstar.local",
  "firstName": "Rahul",
  "lastName": "Verma",
  "batchId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "courseIds": [
    "77777777-7777-7777-7777-777777777777"
  ]
}
```

Response body:

```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "email": "student1@northstar.local",
    "firstName": "Rahul",
    "lastName": "Verma",
    "status": "ACTIVE",
    "batchId": "uuid",
    "batchName": "Mathematics Batch A",
    "courses": [],
    "createdAt": "2026-04-04T00:00:00Z"
  }
}
```

### `GET /api/admin/students`

Lists students in the current organization.

### `GET /api/admin/students/{id}`

Gets one student by UUID.

### `PUT /api/admin/students/{id}`

Updates a student.

Request body: same as `CreateStudentRequest`

### `DELETE /api/admin/students/{id}`

Deactivates a student.

### `PUT /api/admin/students/{id}/status`

Updates student status.

Request body:

```json
{
  "status": "ACTIVE"
}
```

Allowed values: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `GRADUATED`

### `POST /api/admin/students/{id}/transfer`

Transfers a student to another batch.

Request body:

```json
{
  "targetBatchId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "reason": "Promoted after assessment"
}
```

### `POST /api/admin/students/{id}/courses`

Assigns courses to a student.

Request body:

```json
{
  "courseIds": [
    "77777777-7777-7777-7777-777777777777",
    "88888888-8888-8888-8888-888888888888"
  ]
}
```

### `DELETE /api/admin/students/{studentId}/courses/{courseId}`

Removes a course from a student.

## Batches

Base path: `/api/admin/batches`

### `POST /api/admin/batches`

Creates a batch in the current organization.

Request body:

```json
{
  "name": "Mathematics Batch A",
  "instructorId": "33333333-3333-3333-3333-333333333333",
  "duration": "3 months",
  "startTime": "09:00",
  "endTime": "11:00"
}
```

### `GET /api/admin/batches`

Lists batches in the current organization.

### `GET /api/admin/batches/{id}`

Gets one batch.

### `PUT /api/admin/batches/{id}`

Updates a batch.

Request body: same as `BatchRequest`

### `DELETE /api/admin/batches/{id}`

Deletes a batch.

### `GET /api/admin/batches/{id}/students`

Lists students assigned to a batch.

### `POST /api/admin/batches/{batchId}/attendance`

Marks attendance for a batch.

Request body:

```json
{
  "batchId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "date": "2026-04-01",
  "records": [
    {
      "studentId": "44444444-4444-4444-4444-444444444444",
      "status": "PRESENT",
      "remarks": "On time"
    }
  ]
}
```

Allowed attendance values: `PRESENT`, `ABSENT`, `LATE`, `EXCUSED`

### `GET /api/admin/batches/{batchId}/attendance/date/{date}`

Returns attendance for a batch on a single date.

`date` must be `YYYY-MM-DD`.

### `GET /api/admin/batches/{batchId}/attendance`

Returns attendance summary for a batch.

### `GET /api/admin/batches/attendance/student/{studentId}`

Returns attendance history for one student.

### `PUT /api/admin/batches/attendance/{id}`

Updates one attendance record.

Query params:

- `status` optional
- `remarks` optional

Example:

`PUT /api/admin/batches/attendance/{id}?status=PRESENT&remarks=Late%20arrival%20excused`

## Courses

Base path: `/api/courses`

### `GET /api/courses`

Returns all published courses.

### `GET /api/courses/{id}`

Returns a course by UUID.

### `GET /api/courses/student/my-courses`

Returns the authenticated student’s enrolled courses.

### `POST /api/courses`

Creates a course for the current organization.

Request body:

```json
{
  "title": "Foundations of Mathematics",
  "description": "Core math course",
  "price": 24999.0,
  "thumbnailUrl": "https://cdn.example.com/course.png",
  "durationHours": 120,
  "published": true
}
```

### `GET /api/courses/admin`

Lists courses for the admin’s organization.

### `PUT /api/courses/{id}`

Updates a course.

Request body: same as `CourseRequest`

### `DELETE /api/courses/{id}`

Deletes a course.

## Payments

Base path: `/api/payments`

### `POST /api/payments/initiate`

Creates a payment order for the authenticated student.

Request body:

```json
{
  "courseId": "77777777-7777-7777-7777-777777777777",
  "provider": "RAZORPAY"
}
```

Allowed providers: `RAZORPAY`, `STRIPE`

### `GET /api/payments/verify/{orderId}`

Verifies a payment order for the authenticated user.

No request body.

### `POST /api/payments/webhook/{provider}`

Handles provider webhooks.

No authentication header is required by the controller.

### `GET /api/payments/admin`

Returns all payment orders for the current organization.

### `GET /api/payments/admin/{id}`

Returns one payment order.

### `GET /api/payments/admin/summary`

Returns revenue and transaction summary.

## Debug and Health

### `GET /api/debug/whoami`

Returns the current authentication principal, authorities, and tenant organization ID.

### `GET /api/health/db`

Returns database health.

Example success response:

```json
{
  "status": "UP",
  "result": 1
}
```

## Seed and Runtime Notes

- Public auth endpoints: `/api/auth/**`
- Public webhook endpoint: `/api/payments/webhook/**`
- Seed endpoints are configured in security but are not implemented in controllers
- Tenant-scoped endpoints depend on `TenantContext` being set from the JWT or request context
- Some controllers use `ApiResponse<T>` while others return plain entities or pages
