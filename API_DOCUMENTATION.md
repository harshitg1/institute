# 📚 Institute Management System — API Documentation

## Base URL: `http://localhost:8080/api`

---

## 🔐 Authentication  
All endpoints (except `/api/auth/**`) require a valid JWT in the `Authorization: Bearer <token>` header.

### Roles
| Role | Description |
|------|-------------|
| `SUPER_ADMIN` | Platform-level admin, manages organizations |
| `ORG_ADMIN` | Organization-level admin, manages students/batches/courses |
| `INSTRUCTOR` | Instructor assigned to batches |
| `STUDENT` | Student assigned to batches, can purchase courses |

---

## 1️⃣ Auth Endpoints (existing)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new organization + admin | Public |
| POST | `/api/auth/login` | Login, returns access + refresh tokens | Public |
| POST | `/api/auth/refresh` | Refresh access token via cookie | Public |

---

## 2️⃣ Admin → Student Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/admin/students` | Add a new student (status=ACTIVE, assign batch) | ORG_ADMIN |
| GET | `/api/admin/students` | List all students in the organization | ORG_ADMIN |
| GET | `/api/admin/students/{id}` | Get student details by ID | ORG_ADMIN |
| PUT | `/api/admin/students/{id}` | Update student details | ORG_ADMIN |
| DELETE | `/api/admin/students/{id}` | Deactivate/remove student | ORG_ADMIN |
| PUT | `/api/admin/students/{id}/status` | Toggle student active/inactive | ORG_ADMIN |

### Request: `POST /api/admin/students`
```json
{
  "email": "student@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "batchId": "uuid-of-batch",
  "courseIds": ["uuid-course-1", "uuid-course-2"]
}
```

### Response: `201 Created`
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "email": "student@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "status": "ACTIVE",
    "batchId": "uuid",
    "batchName": "Morning Batch A",
    "courses": [
      { "id": "uuid", "title": "Trading 101" }
    ],
    "createdAt": "2026-02-10T18:00:00Z"
  }
}
```

---

## 3️⃣ Batch Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/admin/batches` | Create a new batch | ORG_ADMIN |
| GET | `/api/admin/batches` | List all batches in the organization | ORG_ADMIN |
| GET | `/api/admin/batches/{id}` | Get batch details (with students) | ORG_ADMIN |
| PUT | `/api/admin/batches/{id}` | Update batch details | ORG_ADMIN |
| DELETE | `/api/admin/batches/{id}` | Delete batch (only if empty) | ORG_ADMIN |
| GET | `/api/admin/batches/{id}/students` | List students in a batch | ORG_ADMIN |

### Request: `POST /api/admin/batches`
```json
{
  "name": "Morning Batch A",
  "instructorId": "uuid-of-instructor",
  "duration": "3 months",
  "startTime": "09:00",
  "endTime": "12:00"
}
```

### Response: `201 Created`
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "name": "Morning Batch A",
    "instructorId": "uuid",
    "instructorName": "Prof. XYZ",
    "duration": "3 months",
    "startTime": "09:00",
    "endTime": "12:00",
    "studentCount": 0,
    "createdAt": "2026-02-10T18:00:00Z"
  }
}
```

---

## 4️⃣ Batch Transfer

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/admin/students/{id}/transfer` | Transfer student to another batch | ORG_ADMIN |

### Request: `POST /api/admin/students/{id}/transfer`
```json
{
  "targetBatchId": "uuid-of-new-batch",
  "reason": "Schedule conflict"
}
```

### Response: `200 OK`
```json
{
  "status": "SUCCESS",
  "data": {
    "studentId": "uuid",
    "previousBatchId": "uuid",
    "previousBatchName": "Morning Batch A",
    "newBatchId": "uuid",
    "newBatchName": "Evening Batch B",
    "transferredAt": "2026-02-10T18:00:00Z",
    "reason": "Schedule conflict"
  }
}
```

---

## 5️⃣ Attendance System

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/admin/attendance` | Mark attendance for a batch | ORG_ADMIN / INSTRUCTOR |
| GET | `/api/admin/attendance/batch/{batchId}` | Get attendance records for a batch | ORG_ADMIN / INSTRUCTOR |
| GET | `/api/admin/attendance/student/{studentId}` | Get attendance history for a student | ORG_ADMIN / INSTRUCTOR |
| GET | `/api/admin/attendance/batch/{batchId}/date/{date}` | Get attendance for batch on a specific date | ORG_ADMIN / INSTRUCTOR |
| PUT | `/api/admin/attendance/{id}` | Update an attendance record | ORG_ADMIN |

### Request: `POST /api/admin/attendance`
```json
{
  "batchId": "uuid-of-batch",
  "date": "2026-02-10",
  "records": [
    { "studentId": "uuid-1", "status": "PRESENT" },
    { "studentId": "uuid-2", "status": "ABSENT" },
    { "studentId": "uuid-3", "status": "LATE" }
  ]
}
```

### Response: `201 Created`
```json
{
  "status": "SUCCESS",
  "data": {
    "batchId": "uuid",
    "date": "2026-02-10",
    "totalStudents": 3,
    "present": 1,
    "absent": 1,
    "late": 1,
    "records": [
      { "id": "uuid", "studentId": "uuid-1", "studentName": "John Doe", "status": "PRESENT" }
    ]
  }
}
```

> **Note**: After a batch transfer, attendance is recorded against the student's **current batch** only. Historical attendance from the previous batch is preserved.

---

## 6️⃣ Course Management (Admin)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/admin/courses` | Create a new course | ORG_ADMIN |
| GET | `/api/admin/courses` | List all courses in the organization | ORG_ADMIN |
| GET | `/api/admin/courses/{id}` | Get course details | ORG_ADMIN |
| PUT | `/api/admin/courses/{id}` | Update course details | ORG_ADMIN |
| DELETE | `/api/admin/courses/{id}` | Delete/archive a course | ORG_ADMIN |
| POST | `/api/admin/students/{id}/courses` | Assign courses to a student | ORG_ADMIN |
| DELETE | `/api/admin/students/{studentId}/courses/{courseId}` | Remove course from student | ORG_ADMIN |

### Request: `POST /api/admin/courses`
```json
{
  "title": "Advanced Trading Strategies",
  "description": "Master the art of technical analysis and risk management",
  "price": 4999.00,
  "thumbnailUrl": "https://example.com/img.jpg",
  "durationHours": 40,
  "published": true
}
```

### Response: `201 Created`
```json
{
  "status": "SUCCESS",
  "data": {
    "id": "uuid",
    "title": "Advanced Trading Strategies",
    "description": "Master the art of...",
    "price": 4999.00,
    "thumbnailUrl": "https://example.com/img.jpg",
    "durationHours": 40,
    "published": true,
    "enrollmentCount": 0,
    "createdAt": "2026-02-10T18:00:00Z"
  }
}
```

---

## 7️⃣ Public Course Catalog (Students)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/courses` | Browse published courses | Authenticated |
| GET | `/api/courses/{id}` | View course details | Authenticated |
| GET | `/api/student/my-courses` | View my enrolled/purchased courses | STUDENT |

---

## 8️⃣ Payment Gateway (Adapter Pattern)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/payments/initiate` | Create a payment order for a course | STUDENT |
| POST | `/api/payments/webhook/{provider}` | Handle payment provider webhooks | Public (signature verified) |
| GET | `/api/payments/verify/{orderId}` | Verify payment status | STUDENT |
| GET | `/api/admin/payments` | View all payment transactions | ORG_ADMIN |
| GET | `/api/admin/payments/summary` | Revenue summary & analytics | ORG_ADMIN |
| GET | `/api/admin/payments/{id}` | View payment transaction detail | ORG_ADMIN |

### Request: `POST /api/payments/initiate`
```json
{
  "courseId": "uuid-of-course",
  "provider": "RAZORPAY"
}
```

### Response: `200 OK`
```json
{
  "status": "SUCCESS",
  "data": {
    "orderId": "order_uuid",
    "providerOrderId": "order_LkjHgF1234",
    "provider": "RAZORPAY",
    "amount": 4999.00,
    "currency": "INR",
    "courseTitle": "Advanced Trading Strategies",
    "paymentLink": "https://razorpay.com/pay/...",
    "status": "CREATED"
  }
}
```

### `POST /api/payments/webhook/razorpay` (Webhook from Razorpay)
```json
{
  "event": "payment.captured",
  "payload": {
    "payment": {
      "entity": {
        "id": "pay_XYZ",
        "order_id": "order_LkjHgF1234",
        "amount": 499900,
        "currency": "INR",
        "status": "captured"
      }
    }
  }
}
```

### `GET /api/admin/payments/summary` — Revenue Summary
```json
{
  "status": "SUCCESS",
  "data": {
    "totalRevenue": 149970.00,
    "totalTransactions": 30,
    "successfulTransactions": 28,
    "failedTransactions": 2,
    "revenueByMonth": [
      { "month": "2026-01", "revenue": 74985.00, "count": 15 },
      { "month": "2026-02", "revenue": 74985.00, "count": 15 }
    ],
    "revenueByCourse": [
      { "courseId": "uuid", "courseTitle": "Trading 101", "revenue": 99980.00, "enrollments": 20 }
    ]
  }
}
```

---

## 📐 Adapter Pattern — Payment Gateway Architecture

```
┌──────────────────────────────────────────────────────┐
│                   PaymentController                   │
│              POST /api/payments/initiate              │
└─────────────────────────┬────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────┐
│                    PaymentService                     │
│       (orchestrates order creation, enrollment)       │
└─────────────────────────┬────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────┐
│            PaymentGateway (Interface)                 │
│  ┌──────────────────────────────────────────────┐    │
│  │  + createOrder(PaymentRequest): PaymentOrder  │    │
│  │  + verifyPayment(String orderId): Status      │    │
│  │  + verifyWebhookSignature(headers, body)      │    │
│  │  + getProviderName(): String                  │    │
│  └──────────────────────────────────────────────┘    │
└─────────────┬──────────────────────┬─────────────────┘
              │                      │
              ▼                      ▼
  ┌───────────────────┐   ┌───────────────────┐
  │ RazorpayGateway   │   │  StripeGateway    │
  │ (implements)      │   │  (implements)     │
  └───────────────────┘   └───────────────────┘
              │                      │
              ▼                      ▼
┌──────────────────────────────────────────────────────┐
│           PaymentGatewayFactory                       │
│   getGateway("RAZORPAY") → RazorpayGateway           │
│   getGateway("STRIPE")   → StripeGateway             │
└──────────────────────────────────────────────────────┘
```

> Adding a new payment provider is as simple as:
> 1. Implement `PaymentGateway` interface
> 2. Register it with `PaymentGatewayFactory`
> 3. No changes to `PaymentService` or `PaymentController`

---

## 🗃️ Entity Relationship Summary

```
Organization ─┬── User (ORG_ADMIN, INSTRUCTOR, STUDENT)
               ├── Batch ─── BatchStudent (join table)
               ├── Course
               └── PaymentOrder

Batch ──── Attendance (batchId + studentId + date)

Student ──┬── Enrollment (studentId + courseId)
           ├── BatchStudent (studentId + batchId)
           └── BatchTransferLog (history)

PaymentOrder ── links Course + Student + Provider details
```

---

## ⚙️ Error Response Format (Standardized)
All error responses follow this format:
```json
{
  "status": "ERROR",
  "message": "Human-readable error description",
  "code": "STUDENT_NOT_FOUND",
  "timestamp": "2026-02-10T18:00:00Z"
}
```

Validation errors:
```json
{
  "status": "ERROR",
  "code": "VALIDATION_FAILED",
  "errors": {
    "email": "must not be blank",
    "batchId": "must not be null"
  }
}
```
