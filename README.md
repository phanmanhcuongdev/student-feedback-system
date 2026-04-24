# Student Feedback System

## Project Overview

Student Feedback System is a web-based client-server application for student onboarding, survey operations, and internal feedback handling.

The current implementation supports:

- Student account registration
- Email verification through Resend
- Student document upload after verification
- Admin approval or rejection of pending student accounts
- Governed survey lifecycle with draft, publish, close, and archive states
- Reusable Question Bank and Survey Template management for admins
- Survey recipient tracking with denominator-based response metrics
- Scoped lecturer survey-result access for department-targeted surveys
- Authenticated survey listing, detail view, and submission
- Student survey visibility enforced in the backend using recipient rows, publish state, hidden flag, and end-date expiry
- Survey result viewing for admin and lecturer roles
- CSV survey result export for admins
- Admin analytics dashboard with lifecycle, participation, and attention metrics
- Admin audit log viewer for privileged actions
- Persisted student notifications with read/unread state
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
- [`docs/reporting-architecture.md`](docs/reporting-architecture.md)
  Reporting and export boundaries for analytics queries and survey result exports.
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
APP_RESET_PASSWORD_URL_BASE=http://localhost:5173
APP_RESET_PASSWORD_EXPIRATION_MINUTES=30
RESEND_API_KEY=re_...
APP_MAIL_FROM=noreply@cuongdso.id.vn
RESEND_API_URL=https://api.resend.com/emails
APP_WEB_ALLOWED_ORIGINS=http://localhost:5173
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=student-feedback-bucket
MINIO_URL=http://localhost:9000
APP_STORAGE_MINIO_ACCESS_KEY=your-access-key
APP_STORAGE_MINIO_SECRET_KEY=your-secret-key
APP_STORAGE_MINIO_ENDPOINT=http://localhost:9000
```

Backend runtime variables currently used in [`backend/.env.dev`](backend/.env.dev):

```env
DB_URL=jdbc:sqlserver://192.168.10.211;databaseName=SURVEY_SYSTEM_DEV;encrypt=true;trustServerCertificate=true
DB_USERNAME=sa
DB_PASSWORD=TTCS@1234

APP_JWT_SECRET=...
APP_JWT_ACCESS_TOKEN_EXPIRATION_MS=86400000

APP_VERIFY_EMAIL_URL_BASE=https://survey.cuongdso.id.vn
APP_RESET_PASSWORD_URL_BASE=https://survey.cuongdso.id.vn
APP_RESET_PASSWORD_EXPIRATION_MINUTES=30

RESEND_API_KEY=re_...
APP_MAIL_FROM=noreply@cuongdso.id.vn
RESEND_API_URL=https://api.resend.com/emails

APP_WEB_ALLOWED_ORIGINS=https://survey.cuongdso.id.vn

MINIO_ACCESS_KEY=...
MINIO_SECRET_KEY=...
MINIO_BUCKET=student-feedback-bucket
MINIO_URL=http://minio:9000
APP_STORAGE_MINIO_ACCESS_KEY=...
APP_STORAGE_MINIO_SECRET_KEY=...
APP_STORAGE_MINIO_ENDPOINT=http://minio:9000
```

Frontend variables from [`frontend/.env.example`](frontend/.env.example):

```env
VITE_API_BASE_URL=/api
VITE_API_PROXY_TARGET=http://localhost:8080
```

Notes:

- Spring Boot in this repo does not auto-load `.env` files. Export variables in your shell or configure them in your IDE run configuration.
- `backend/.env.dev` is the canonical backend env reference in this repo. Root `.env.example`, `.env.dev`, and `.env.prod` should follow its variable names and ordering.
- The backend document storage configuration is read from `app.storage.minio.*` in [`backend/src/main/resources/application.yaml`](backend/src/main/resources/application.yaml), which means the Spring application needs `APP_STORAGE_MINIO_ENDPOINT`, `APP_STORAGE_MINIO_ACCESS_KEY`, and `APP_STORAGE_MINIO_SECRET_KEY`.
- `backend/.env.dev` also keeps `MINIO_*` values alongside `APP_STORAGE_MINIO_*` so MinIO service/container config and Spring Boot client config stay aligned from one source of truth.
- `spring.jpa.hibernate.ddl-auto=validate` is enabled, so Flyway is responsible for creating or updating schema state before Hibernate validates entities.
- Flyway is enabled through `spring.flyway.enabled=true` and scans SQL migrations from `classpath:db/migration`, which maps to `backend/src/main/resources/db/migration/`.
- `spring.flyway.baseline-on-migrate=true` is enabled so an existing SQL Server database can be brought under Flyway management without replaying the initial schema migration.
- `backend/src/main/resources/db/migration/V1__initial_schema.sql` is the baseline schema migration used for new environments.
- `backend/src/main/resources/db/migration/V2__notification_module.sql` extends notification storage with type, title, survey metadata, delivery timestamp, and read state.
- For a new database, let Flyway apply the versioned scripts at startup. The legacy `database/full_schema.sql` and `database/migrations/` files are still useful as reference SQL, but the application now runs migrations from the Flyway folder.
- Resend must be configured if you want registration to send a real verification email. If `RESEND_API_KEY` is missing, registration email delivery will fail by design.

## MinIO Document Storage

Student onboarding documents are stored in MinIO through the backend storage adapter at [MinioStudentDocumentStorageAdapter.java](backend/src/main/java/com/ttcs/backend/adapter/out/persistence/MinioStudentDocumentStorageAdapter.java). The database keeps the bucket/object path, while the binary file itself lives in MinIO.

Variables involved:

- `MINIO_ACCESS_KEY`: access key passed to the MinIO server/container.
- `MINIO_SECRET_KEY`: secret key passed to the MinIO server/container.
- `MINIO_BUCKET`: bucket name used for student documents.
- `MINIO_URL`: MinIO service URL, typically `http://minio:9000` inside Docker networking.
- `APP_STORAGE_MINIO_ENDPOINT`: Spring Boot endpoint for the MinIO SDK. In most deployments this matches `MINIO_URL`.
- `APP_STORAGE_MINIO_ACCESS_KEY`: Spring Boot credential used by the backend MinIO client. In most deployments this matches `MINIO_ACCESS_KEY`.
- `APP_STORAGE_MINIO_SECRET_KEY`: Spring Boot credential used by the backend MinIO client. In most deployments this matches `MINIO_SECRET_KEY`.

Recommended mapping in deployment:

```env
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=student-feedback-bucket
MINIO_URL=http://minio:9000

APP_STORAGE_MINIO_ENDPOINT=${MINIO_URL}
APP_STORAGE_MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
APP_STORAGE_MINIO_SECRET_KEY=${MINIO_SECRET_KEY}
```

Operational notes:

- The backend only talks to MinIO through the `APP_STORAGE_MINIO_*` variables.
- The MinIO container/service itself only needs the `MINIO_*` variables.
- If the backend starts without the `APP_STORAGE_MINIO_*` values, document upload and document review endpoints cannot initialize correctly.
- `MINIO_BUCKET` is still part of the canonical env file because the MinIO service setup needs it, even though Spring Boot reads credentials/endpoint from `APP_STORAGE_MINIO_*`.

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
$env:APP_RESET_PASSWORD_URL_BASE="http://localhost:5173"
$env:APP_RESET_PASSWORD_EXPIRATION_MINUTES="30"
$env:RESEND_API_KEY="re_..."
$env:APP_MAIL_FROM="noreply@cuongdso.id.vn"
$env:RESEND_API_URL="https://api.resend.com/emails"
$env:APP_WEB_ALLOWED_ORIGINS="http://localhost:5173"
$env:MINIO_ACCESS_KEY="your-access-key"
$env:MINIO_SECRET_KEY="your-secret-key"
$env:MINIO_BUCKET="student-feedback-bucket"
$env:MINIO_URL="http://localhost:9000"
$env:APP_STORAGE_MINIO_ACCESS_KEY="your-access-key"
$env:APP_STORAGE_MINIO_SECRET_KEY="your-secret-key"
$env:APP_STORAGE_MINIO_ENDPOINT="http://localhost:9000"

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
4. Submit answers for all required questions on surveys that have been published, are assigned to the current student, are not hidden, and have not expired

### Survey administration

1. Sign in with an admin account
2. Open `/admin/surveys`
3. Create a survey draft
4. Optionally manage reusable questions in `/admin/question-bank`
5. Optionally manage or apply reusable templates in `/admin/survey-templates`
6. Edit the draft until dates, questions, and recipient scope are ready
7. Publish the survey
8. Monitor targeted, opened, submitted, and response-rate metrics
9. Export result CSV reports from survey result detail as admin
10. Close it when collection should stop, then archive it when the run is complete

### Analytics, audit, and notifications

1. Sign in with an admin account
2. Open `/dashboard/admin` for survey lifecycle, participation, department, and attention metrics
3. Open `/admin/audit-logs` to inspect successful privileged actions with filters and pagination
4. Sign in as a student and open `/notifications` to review survey and onboarding notifications, filter unread items, and mark items as read

### User administration

1. Sign in with an admin account
2. Open `/admin/users`
3. Use role segmentation, keyword search, filters, pagination, and sort controls
4. Open a user detail record and perform supported status changes

### Survey result review

1. Sign in with an admin or lecturer account
2. Open `/survey-results`
3. Inspect survey statistics, participation metrics, and question-level breakdowns

### Feedback and review queues

1. Sign in with an admin or lecturer account
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
- Question Bank, Survey Templates, pending-student review, staff feedback review, student surveys, student feedback history, notifications, audit logs, and survey results now use backend-backed pagination.
- Survey results also use backend-backed filtering, sorting, and metrics while exposing lifecycle, runtime status, and audience scope.
- Student survey completion state is derived from `Survey_Recipient.submitted_at`, not a frontend-only flag.
- Student survey list/detail responses prefer bilingual survey title and description columns when translated content exists.
- Student survey text responses publish `SURVEY_RESPONSE` translation tasks after submit and persist bilingual comment columns on `Response_Detail`.
- Notification deadline reminders are generated lazily when the student opens the notification center; no scheduler platform is used in the current implementation.
- Seed accounts in `database/seed_data.sql` are BCrypt-compatible and can be used directly after import:
  - `admin@university.edu` / `admin123`
  - `lecturer@university.edu` / `lecturer123`
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
- Reporting follows the same boundary. Controllers do not own reporting SQL or export formatting; reporting queries live behind output ports in persistence adapters, and survey result export renders through a `SurveyReportRenderer`.
- See [`docs/reporting-architecture.md`](docs/reporting-architecture.md) for the current admin analytics and survey export flow.
