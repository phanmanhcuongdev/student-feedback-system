# Backend

## Overview

This module is the Spring Boot API server for the Student Feedback System.

It handles:

- Authentication and JWT issuance
- Student onboarding and email verification
- Student document upload
- Admin approval flow
- Survey listing, detail, submission, and result viewing
- Admin user management queries and mutations
- Admin survey management lifecycle actions and operational queries
- Question Bank and Survey Template administration
- Survey analytics overview and CSV result export
- Admin audit log viewing
- Persisted student notifications with read/unread state
- Feedback reply handling for staff roles

## Stack

- Java 21
- Spring Boot 4
- Spring MVC
- Spring Security
- Spring Data JPA
- Flyway
- Microsoft SQL Server
- Resend API for verification emails

## Project Structure

- [`src/main/java/com/ttcs/backend/adapter/in`](src/main/java/com/ttcs/backend/adapter/in)
  Web controllers and security filters
- [`src/main/java/com/ttcs/backend/application`](src/main/java/com/ttcs/backend/application)
  Domain models, use cases, input ports, output ports
- [`src/main/java/com/ttcs/backend/adapter/out`](src/main/java/com/ttcs/backend/adapter/out)
  Persistence adapters, JWT adapter, Resend adapter
- [`src/main/java/com/ttcs/backend/config`](src/main/java/com/ttcs/backend/config)
  Spring configuration beans
- [`src/main/resources/application.yaml`](src/main/resources/application.yaml)
  Main Spring configuration

## Required Configuration

Variables used by the backend:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=

APP_JWT_SECRET=
APP_JWT_ACCESS_TOKEN_EXPIRATION_MS=86400000

APP_VERIFY_EMAIL_URL_BASE=http://localhost:5173
APP_RESET_PASSWORD_URL_BASE=http://localhost:5173
APP_RESET_PASSWORD_EXPIRATION_MINUTES=30

RESEND_API_KEY=
APP_MAIL_FROM=noreply@cuongdso.id.vn
RESEND_API_URL=https://api.resend.com/emails
APP_WEB_ALLOWED_ORIGINS=http://localhost:5173

MINIO_ACCESS_KEY=
MINIO_SECRET_KEY=
MINIO_BUCKET=student-feedback-bucket
MINIO_URL=http://localhost:9000

APP_STORAGE_MINIO_ACCESS_KEY=
APP_STORAGE_MINIO_SECRET_KEY=
APP_STORAGE_MINIO_ENDPOINT=http://localhost:9000
```

Important notes:

- Flyway manages schema creation and migration before Hibernate validation runs.
- Email verification uses Resend. Registration will not silently fake success if email delivery is unavailable.
- SQL Server is the only database wired in the current configuration.
- `backend/.env.dev` is the canonical env reference for backend variable names in this repo.
- Spring Boot reads MinIO client config from `APP_STORAGE_MINIO_*`, while the same canonical env file also carries `MINIO_*` values for MinIO service/container setup.

## Flyway Integration

Flyway is configured through Spring Boot 4 auto-configuration.

- Dependency path:
  - `spring-boot-starter-flyway`
  - `flyway-core`
  - `flyway-sqlserver`
- Migration scripts live in [`src/main/resources/db/migration/`](src/main/resources/db/migration/)
- Current baseline migration:
  - [`src/main/resources/db/migration/V1__initial_schema.sql`](src/main/resources/db/migration/V1__initial_schema.sql)
- Current notification module migration:
  - [`src/main/resources/db/migration/V2__notification_module.sql`](src/main/resources/db/migration/V2__notification_module.sql)

The active Flyway properties in [`src/main/resources/application.yaml`](src/main/resources/application.yaml) are:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

What this means:

- Spring Boot creates and runs Flyway automatically at startup.
- Flyway uses the main SQL Server datasource from `spring.datasource`.
- `baseline-on-migrate: true` allows the app to adopt an existing database that already has tables.
- On first managed run against an existing schema, Flyway creates `flyway_schema_history` and records the baseline version.
- On a fresh database, Flyway applies the versioned SQL files in order before JPA validation.

There is no custom `FlywayConfig.java` in the project. Manual Flyway bean creation is not part of the supported setup.

## Run Locally

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Backend runs on:

- `http://localhost:8080`

## Key Flows

- `POST /api/auth/register-student`
  Creates a student account in `EMAIL_UNVERIFIED` state and sends a verification email.
- `GET /api/auth/verify-email`
  Validates the token and advances onboarding within the allowed student status vocabulary.
- `POST /api/auth/login`
  Issues a JWT when the account is in a login-eligible state.
- `POST /api/auth/upload-docs`
  Uploads student documents for the authenticated student.
- `GET /api/v1/surveys`
  Returns survey summaries for the authenticated user.
  Student-facing survey visibility is enforced in the backend from `Survey_Recipient`, publish state, hidden flag, and end-date expiry.
- `POST /api/v1/surveys/{surveyId}/submit`
  Submits survey answers for the current student.
- `GET /api/admin/users`
  Returns backend-backed paginated user management results with filter and sort support.
- `GET /api/admin/surveys`
  Returns backend-backed paginated survey management results with filter and sort support.
- `GET /api/admin/question-bank`
  Returns backend-backed paginated reusable question assets with search and filters.
- `GET /api/admin/survey-templates`
  Returns backend-backed paginated survey templates with search and active/archive filtering.
- `GET /api/v1/survey-results`
  Returns survey result summaries enriched with lifecycle, runtime, and audience metadata.
- `GET /api/admin/analytics/overview`
  Returns admin survey health and participation metrics.
- `GET /api/admin/audit-logs`
  Returns paginated privileged-action audit records.
- `GET /api/v1/notifications`
  Returns persisted student notifications with read/unread metadata.

## Development Notes

- Controllers stay in `adapter.in.web`.
- Business logic stays in `application.domain.service`.
- External service calls such as Resend stay in `adapter.out`.
- If you update environment variables, restart the application so Spring picks them up.
- Department lookup endpoints currently support operational filters for user and survey management screens.
- Add future schema changes as new versioned files under `src/main/resources/db/migration`, not by editing an already-applied migration.
- Student survey completion is tracked by `Survey_Recipient.submitted_at`; do not introduce a duplicate boolean unless there is a strong denormalization need.
- Lecturer is the canonical staff role name. Do not introduce product-facing Teacher terminology.
