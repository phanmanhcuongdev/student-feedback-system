# Student Feedback System

## Project Overview

Student Feedback System is a web-based client-server application for student onboarding, survey operations, and internal feedback handling.

The current implementation supports:

- Student account registration
- Email verification through Resend
- Student document upload after verification
- Admin approval or rejection of pending student accounts
- Governed survey lifecycle with draft, publish, close, and archive states
- Survey recipient tracking with denominator-based response metrics
- Scoped teacher survey-result access for department-targeted surveys
- Authenticated survey listing, detail view, and submission
- Survey result viewing for admin and teacher roles
- Shared authenticated frontend shell with role-aware navigation
- Account area for current-user overview and password management
- Admin user management with backend-backed search, filter, pagination, and sort
- Admin survey management with backend-backed search, filter, pagination, and sort
- Operational queue views for survey results, staff feedback review, and pending student review

## Tech Stack

- Backend: Java 21, Spring Boot 4, Spring MVC, Spring Security, Spring Data JPA, Flyway
- Database: Microsoft SQL Server
- Authentication: JWT bearer tokens
- Email delivery: Resend API
- Frontend: React 19, TypeScript, Vite, React Router, Axios, Tailwind CSS
- CI: GitHub Actions
- Container images: Docker for backend and frontend

## Repository Structure

- [`backend/`](backend/)
  Spring Boot API server. Organized around Hexagonal Architecture / Ports and Adapters.
- [`frontend/`](frontend/)
  React web client that calls the backend API. Uses a shared authenticated `AppShell`, role-aware navigation, shared UI primitives, and reusable operational data-view components.
- [`API_CONTRACT.md`](API_CONTRACT.md)
  Implemented API slices and payload expectations.
- [`database/README.md`](database/README.md)
  Fresh setup, migration path, and demo credential notes.
- [`.env.example`](.env.example)
  Backend environment variable template.
- [`frontend/.env.example`](frontend/.env.example)
  Frontend environment variable template.

## Prerequisites

- Java 21
- Node.js 22 and npm
- Microsoft SQL Server with the application schema already created
- A Resend account and API key if you want to test the real email verification flow
- A reachable MinIO instance for document storage

## Environment Variables

Backend variables from [`.env.example`](.env.example):

```env
DB_URL=jdbc:sqlserver://localhost;databaseName=SURVEY_SYSTEM_DEV;encrypt=true;trustServerCertificate=true
DB_USERNAME=sa
DB_PASSWORD=your-password
APP_JWT_SECRET=change-me-local-dev-secret-key-32b
APP_JWT_ACCESS_TOKEN_EXPIRATION_MS=86400000
APP_VERIFY_EMAIL_URL_BASE=http://localhost:5173
RESEND_API_KEY=re_...
APP_MAIL_FROM=noreply@cuongdso.id.vn
APP_WEB_ALLOWED_ORIGINS=http://localhost:5173
RESEND_API_URL=https://api.resend.com/emails
APP_STORAGE_MINIO_ENDPOINT=http://localhost:9000
APP_STORAGE_MINIO_ACCESS_KEY=minioadmin
APP_STORAGE_MINIO_SECRET_KEY=minioadmin
APP_STORAGE_MINIO_BUCKET=student-documents
```

Frontend variables from [`frontend/.env.example`](frontend/.env.example):

```env
VITE_API_BASE_URL=/api
VITE_API_PROXY_TARGET=http://localhost:8080
```

Notes:

- Spring Boot in this repo does not auto-load `.env` files. Export variables in your shell or configure them in your IDE run configuration.
- `spring.jpa.hibernate.ddl-auto=validate` is enabled, so Flyway is responsible for creating or updating schema state before Hibernate validates entities.
- Flyway is enabled through `spring.flyway.enabled=true` and scans SQL migrations from `classpath:db/migration`, which maps to `backend/src/main/resources/db/migration/`.
- `spring.flyway.baseline-on-migrate=true` is enabled so an existing SQL Server database can be brought under Flyway management without replaying the initial schema migration.
- `backend/src/main/resources/db/migration/V1__initial_schema.sql` is the baseline schema migration used for new environments.
- For a new database, let Flyway apply the versioned scripts at startup. The legacy `database/full_schema.sql` and `database/migrations/` files are still useful as reference SQL, but the application now runs migrations from the Flyway folder.
- Resend must be configured if you want registration to send a real verification email. If `RESEND_API_KEY` is missing, registration email delivery will fail by design.

## Database Migrations

Flyway is the authoritative migration mechanism for the backend.

- Maven dependencies:
  - `spring-boot-starter-flyway`
  - `flyway-core`
  - `flyway-sqlserver`
- Runtime configuration in [`backend/src/main/resources/application.yaml`](backend/src/main/resources/application.yaml):
  - `spring.flyway.enabled=true`
  - `spring.flyway.baseline-on-migrate=true`
  - `spring.flyway.locations=classpath:db/migration`
- Migration directory:
  - [`backend/src/main/resources/db/migration/`](backend/src/main/resources/db/migration/)
- Naming convention:
  - `V1__initial_schema.sql`
  - `V2__...sql`
  - `V3__...sql`

How it works in this project:

- On backend startup, Spring Boot 4 auto-configures Flyway through the Flyway starter.
- Flyway connects to the same SQL Server datasource configured by `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- On a fresh database, Flyway creates `flyway_schema_history` and applies the migration files in version order.
- On an existing database, Flyway baselines the current schema first, then manages subsequent versioned migrations from that point forward.
- Hibernate runs in `validate` mode after schema migration, so entity validation happens against the Flyway-managed schema.

## How to Run Locally

### 1. Start the backend

From [`backend/`](backend/):

```powershell
$env:DB_URL="jdbc:sqlserver://localhost;databaseName=SURVEY_SYSTEM_DEV;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="your-password"
$env:APP_JWT_SECRET="change-me-local-dev-secret-key-32b"
$env:APP_JWT_ACCESS_TOKEN_EXPIRATION_MS="86400000"
$env:APP_VERIFY_EMAIL_URL_BASE="http://localhost:5173"
$env:RESEND_API_KEY="re_..."
$env:APP_MAIL_FROM="noreply@cuongdso.id.vn"
$env:APP_WEB_ALLOWED_ORIGINS="http://localhost:5173"
$env:APP_STORAGE_MINIO_ENDPOINT="http://localhost:9000"
$env:APP_STORAGE_MINIO_ACCESS_KEY="minioadmin"
$env:APP_STORAGE_MINIO_SECRET_KEY="minioadmin"
$env:APP_STORAGE_MINIO_BUCKET="student-documents"

cd backend
.\mvnw.cmd spring-boot:run
```

Backend default URL:

- `http://localhost:8080`

### 2. Start the frontend

From [`frontend/`](frontend/):

```powershell
cd frontend
npm ci
npm run dev
```

Frontend default URL:

- `http://localhost:5173`

The Vite dev server proxies `/api` requests to the backend target from [`frontend/vite.config.ts`](frontend/vite.config.ts).

### 3. Optional verification commands

Backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend lint and build:

```powershell
cd frontend
npm run lint
npm run build
```

## Main User Flows to Test

### Student onboarding

1. Open `/register`
2. Create a student account with a department that already exists in the database
3. Check the real email inbox and click the `/verify-email?token=...` link sent by Resend
4. Sign in as that student
5. Upload student card and national ID documents
6. If admin rejects the onboarding request, sign in again, review the rejection feedback, and resubmit corrected documents
7. Sign in again after admin approval

### Admin approval

1. Sign in with an admin account that already exists in the database
2. Open `/admin/students/pending`
3. Approve a pending student with optional reviewer notes, or reject with a required reason and optional reviewer notes

### Account and security

1. Sign in with any authenticated role
2. Open `/account`
3. Review the account overview based on the current session data
4. Open `/account/security`
5. Change the current password

Compatibility note:

- `/change-password` now redirects to `/account/security`

### Survey submission

1. Sign in with an active student account
2. Open `/surveys`
3. View survey detail
4. Submit answers for all required questions on surveys that have been published and are currently open

### Survey administration

1. Sign in with an admin account
2. Open `/admin/surveys`
3. Create a survey draft
4. Edit the draft until dates, questions, and recipient scope are ready
5. Publish the survey
6. Monitor targeted, opened, submitted, and response-rate metrics
7. Close it when collection should stop, then archive it when the run is complete

### User administration

1. Sign in with an admin account
2. Open `/admin/users`
3. Use role segmentation, keyword search, filters, pagination, and sort controls
4. Open a user detail record and perform supported status changes

### Survey result review

1. Sign in with an admin or teacher account
2. Open `/survey-results`
3. Inspect survey statistics, participation metrics, and question-level breakdowns

### Feedback and review queues

1. Sign in with an admin or teacher account
2. Open `/feedback/manage`
3. Search and filter the feedback queue, open a queue item, and send a response
4. As admin, open `/admin/students/pending` and work the review queue

## Notes / Troubleshooting

- No root Docker Compose setup exists in this repo. Dockerfiles build images only.
- Registration depends on real email delivery. If verification emails are not arriving, check `RESEND_API_KEY`, `APP_MAIL_FROM`, and that the sender domain is verified in Resend.
- The frontend does not generate backend URLs on its own. Use `VITE_API_BASE_URL` and `VITE_API_PROXY_TARGET` instead of hardcoding API hosts.
- Student onboarding relies on SQL Server lookup data. In particular, department names must exist in the `Department` table before registration succeeds.
- If the backend fails on startup with Flyway errors, inspect `backend/src/main/resources/db/migration/` and the `flyway_schema_history` table first.
- If the backend fails after Flyway succeeds with schema validation errors, the migrated schema still does not match the JPA mappings.
- Current account overview uses the authenticated session data already available to the frontend. It does not fabricate unsupported profile fields from a separate profile API.
- User management search, filter, pagination, and sort are backend-backed.
- Survey management search, filter, pagination, and sort are backend-backed.
- Survey results now expose richer metadata such as lifecycle, runtime status, and audience scope.
- Feedback management and pending-student queues still use frontend-side filtering and pagination in the current implementation.
- Seed accounts in `database/seed_data.sql` are BCrypt-compatible and can be used directly after import:
  - `admin@university.edu` / `admin123`
  - `teacher@university.edu` / `teacher123`
  - seeded student accounts / `student123`

## Architecture Summary

- Frontend is a React + Vite + TypeScript SPA with:
  - shared authenticated `AppShell`
  - role-aware navigation groups
  - `/account` and `/account/security`
  - shared page, state, badge, and data-view primitives
  - operational admin pages built around tables and queues instead of flat card walls
- Frontend calls backend REST APIs over HTTP.
- Backend follows a Ports and Adapters structure:
  - `adapter.in`: web and security entry points
  - `application`: use cases, domain models, input and output ports
  - `adapter.out`: persistence adapters, security token service, external integrations such as Resend
- Main request flow:
  frontend page -> frontend API client -> backend controller -> use case service -> output port -> persistence adapter -> SQL Server
