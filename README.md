# Institute

Small summary and developer guide for the Institute project.

## What this service does (high level)
- Backend for a multi-tenant institute/academy platform.
- Core entities: User, Role, Organization, Course, Lesson, Enrollment, VideoProgress.
- Authentication: JWT-based. JWT contains subject (`sub`) as the user's email, optional `organization_id`, and `roles` (list of role names).
- Tenant context: per-request TenantContext (ThreadLocal) is set by `JwtAuthFilter` from JWT `organization_id` claim or from `X-ORG-ID` header.

## High level architecture
- Spring Boot application
- Spring Security with a `JwtAuthFilter` which validates tokens, sets `TenantContext`, and establishes an Authentication in `SecurityContext`.
- JPA/Hibernate for data access and entities under `com.institute.Institue.model`.
- Flyway migrations in `src/main/resources/db/migration`.

## JWT contents
- `sub` - the user's email (string)
- `organization_id` - the tenant UUID (string) when applicable
- `roles` - array of role names (e.g. `["SUPER_ADMIN","ORG_ADMIN"]`)
- `iat` and `exp` timestamps

The `JwtService` generates tokens with these claims; `JwtAuthFilter` reads them and sets a UsernamePasswordAuthenticationToken with `sub` as principal and `ROLE_<name>` authorities.

## Tenant context
- `com.institute.Institue.tenant.TenantContext` uses a ThreadLocal to store current request's organization id.
- Controllers/services should call `TenantContext.getCurrentOrg()` to determine the current tenant where necessary. This is set automatically by `JwtAuthFilter`.

## Important endpoints
- `POST /api/auth/login` — accepts `{ "email": "...", "password": "..." }` (see `AuthRequest`) and returns a token.
- `GET /api/superadmin/seed` — seeds the database with a default organization and users. Secured to `ROLE_SUPER_ADMIN` in `SecurityConfig`. Seeded users get password `ChangeMe123!` (development only) — the endpoint returns this plaintext password in the response.

## Database migrations
- Migrations are in `src/main/resources/db/migration`. There is:
  - `V1__init.sql` - initial schema
  - `V2__add_organizations.sql` - organization table
  - `V3__create_user_roles.sql` - creates `roles` table and `user_roles` join table for ManyToMany mapping

Notes: The project uses Flyway; ensure `spring.flyway.enabled=true` in `application.properties` when running against a real Postgres cluster. If you are using Postgres 18.1 and Flyway has trouble, run migrations manually or use a compatible Flyway version.

## How to run locally

### Quick Start (Windows)
Use the provided helper script that automatically handles port conflicts:
```powershell
.\start.ps1
```

### Manual Start

1. Configure `src/main/resources/application.properties` with your Postgres connection. Example in repo uses:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/institute
spring.datasource.username=postgres
spring.datasource.password=admin123
```

2. Build and run:

```powershell
# Using Maven wrapper
.\mvnw.cmd spring-boot:run

# Or build and run JAR
.\mvnw.cmd package
java -jar target/Institute-0.0.1-SNAPSHOT.jar
```

3. Seed data (run once):
- Create a `SUPER_ADMIN` user manually in DB with email `super@local` or run the seed endpoint. If seeded via the endpoint, it returns default password in response (`ChangeMe123!`).

### Troubleshooting Port Conflicts

**Error: "Port 8080 was already in use"**

Option 1 - Kill the process using port 8080:
```powershell
# Find process using port 8080
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess

# Kill the process (replace PID with actual process ID)
Stop-Process -Id <PID> -Force
```

Option 2 - Use a different port:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

Option 3 - Use the provided `start.ps1` script that handles this automatically.

## Security notes
- Change `JWT_SECRET` in `application.properties`/env for production.
- Do NOT use the default seed password in production.
- For production use, create robust migrations that remove the legacy `username` column (if present) and migrate any necessary data.

## Next steps / To do
- Add integration tests for authentication flows
- Harden migrations and remove `username` column
- Add password reset and email verification flows
- Centralize tenant-scoped data access (optional use of Hibernate filters)

