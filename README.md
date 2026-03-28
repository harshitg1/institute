# Institute

Backend service for a multi-tenant institute or academy platform built with Spring Boot 3, Spring Security, JPA, PostgreSQL, Flyway, and JWT authentication.

## Overview

The application models organizations, users, students, batches, courses, attendance, enrollments, and payment orders.

Implemented functional areas:

- Organization registration with admin bootstrap
- Login with access token and refresh token cookie
- Super-admin organization management
- Organization student management
- Batch creation, membership, transfers, and attendance
- Course catalog and organization course management
- Payment initiation, verification, webhook handling, and revenue summaries
- Basic debug and database health endpoints

## Tech Stack

- Java 21
- Spring Boot 3.3.0
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- MapStruct
- ModelMapper

## Project Layout

- `src/main/java/com/institute/Institue/controller` REST controllers
- `src/main/java/com/institute/Institue/service` service interfaces
- `src/main/java/com/institute/Institue/service/impl` service implementations
- `src/main/java/com/institute/Institue/model` JPA entities
- `src/main/java/com/institute/Institue/repository` Spring Data repositories
- `src/main/java/com/institute/Institue/security` JWT and Spring Security integration
- `src/main/java/com/institute/Institue/payment` payment abstraction and adapters
- `src/main/resources/application.yml` runtime configuration
- `src/main/resources/db/migration` Flyway migrations
- `src/test/java` unit tests

## Domain Model

Primary entities:

- `Organization`: tenant boundary for most business data
- `User`: all user types, with one role and optional `studentStatus`
- `Role`: backed by `UserRole` enum values such as `SUPER_ADMIN`, `ORG_ADMIN`, `INSTRUCTOR`, `STUDENT`
- `Batch`: belongs to an organization and may have an instructor
- `BatchStudent`: active and historical batch membership
- `BatchTransferLog`: transfer history between batches
- `Course`: purchasable or assignable course owned by an organization
- `Enrollment`: student-course relationship, including purchased vs assigned
- `Attendance`: batch attendance records per date
- `PaymentOrder`: payment lifecycle and provider metadata

## Security Model

Authentication is stateless and JWT-based.

- `POST /api/auth/login` returns access and refresh tokens
- Refresh token is also set as an HTTP-only cookie named `refreshToken`
- Access token claims include:
  - `sub`: user email
  - `organization_id`: tenant UUID when present
  - `role_id`: role UUID
  - `roles`: role names list
  - `token_type`: `ACCESS`
- Refresh token includes `sub` and `token_type=REFRESH`

`JwtAuthFilter` validates the bearer token, loads the full `User` when possible, populates Spring Security, and sets tenant context from `organization_id`.

Public routes configured in `SecurityConfig`:

- `/api/auth/**`
- `/actuator/health`
- `/api/payments/webhook/**`
- `/api/superadmin/seed`
- `/api/superadmin/reset-seed`

Important note: the seed endpoints are allowed by security config but are not implemented by any controller in this repository.

## Tenant Handling

Tenant resolution uses `TenantContext` backed by `ThreadLocal`.

- Normal path: extracted from JWT claim `organization_id`
- Fallback path: `X-ORG-ID` request header when no bearer token is present

Controllers under tenant-scoped flows commonly reject requests with `403` if no organization is present in `TenantContext`.

## Configuration

Default configuration lives in `src/main/resources/application.yml`.

Key settings:

- Server port: `8080`
- Default datasource: `jdbc:postgresql://localhost:5432/institute`
- Default DB username: `postgres`
- Default DB password: `admin123`
- JPA schema mode: `ddl-auto: update`
- Flyway: enabled
- JWT secret: `JWT_SECRET` env var with a development fallback
- Razorpay keys:
  - `RAZORPAY_KEY_ID`
  - `RAZORPAY_KEY_SECRET`
- Stripe keys:
  - `STRIPE_SECRET_KEY`
  - `STRIPE_WEBHOOK_SECRET`

## Running Locally

### Prerequisites

- Java 21
- PostgreSQL running locally
- A database named `institute`

### Start the application

```powershell
.\mvnw.cmd spring-boot:run
```

Or build first:

```powershell
.\mvnw.cmd package
java -jar target\Institute-0.0.1-SNAPSHOT.jar
```

The app starts on `http://localhost:8080`.

### Health check

- Actuator: `GET /actuator/health`
- DB probe: `GET /api/health/db`

## Database and Migrations

Flyway migrations:

- `V1__init.sql`: creates the schema and drops existing tables first
- `V2__rename_roles_name_to_role.sql`: renames `roles.name` to `roles.role`

Important note: `V1__init.sql` is destructive because it drops tables before recreating them. Treat it as development-only migration behavior unless rewritten.

Also note that Flyway is enabled while JPA is configured with `ddl-auto: update`, so schema management currently relies on both mechanisms.

## Payments

Payments are implemented through an adapter-based abstraction:

- `PaymentGateway` interface
- `PaymentGatewayFactory`
- `RazorpayGatewayAdapter`
- `StripeGatewayAdapter`

Current behavior:

- Order creation is simulated
- Payment verification is simulated
- Webhook signature verification is effectively permissive in development adapters
- Successful payment verification auto-enrolls the student in the purchased course

This is useful for local integration work, but it is not production-grade gateway integration yet.

## Testing

There are unit tests for:

- JWT service
- payment gateway factory
- auth service
- student service
- batch service
- attendance service
- payment service

Current status from `maven_test_output.log`:

- Test compilation is failing
- The failure is caused by tests still calling `Role.builder().name(...)` after the model changed to `role`

So the repository currently does not have a clean `mvn test` run.

## Known Gaps and Mismatches

These are important if you are using this repository as-is:

- `API_DOCUMENTATION.md` and the old README were out of sync with the codebase
- `SecurityConfig` references seed endpoints that do not exist
- `OrganizationController` is implemented, but `UserServiceImpl` returns `null` or empty lists, so `/api/org/users` is effectively a stub
- Course management endpoints are mounted under `/api/courses`, not `/api/admin/courses`
- Payment admin endpoints are mounted under `/api/payments/admin`, not `/api/admin/payments`
- Some endpoint authorization is looser than the controller naming suggests because the security rules are path-based
- Registration creates an organization whose name is set from `firstName`, because `RegisterRequest` does not include an organization name field
- Student creation sets a hard-coded initial password: `defaultPassword123`
- Refresh tokens are generated and returned, but the `refresh_tokens` table is not used in the auth flow
- `Dockerfile` exists but is empty

## Documentation

- General project guide: `README.md`
- Endpoint reference: `API_DOCUMENTATION.md`

For the actual request and response shapes, rely on the DTOs and controllers in `src/main/java/com/institute/Institue`.
