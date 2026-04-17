# Technical Roadmap Evaluation

## 1. Project Understanding
This repository is already more than a basic survey CRUD app. It is an internal university workflow system covering student onboarding, survey lifecycle management, survey participation tracking, staff feedback handling, notifications, and role-aware operational dashboards ([README.md](../README.md), [README.md](../README.md), [README.md](../README.md)).

The backend is a Spring Boot hexagonal application with clear ports/adapters structure ([README.md](../README.md), [backend/README.md](../backend/README.md)). The frontend is a React SPA with shared app shell, role-based navigation, and reusable operational data-view components rather than ad hoc pages ([frontend/README.md](../frontend/README.md), [frontend/src/components/data-view/DataTable.tsx](../frontend/src/components/data-view/DataTable.tsx)).

Operationally, the project is in a solid “production-minded student project” stage: Docker images exist, CI runs tests/lint/build and pushes images, but there is no root Compose stack, no migration tool, no observability stack, no frontend tests, and no security scanning yet ([.github/workflows/ci.yml](../.github/workflows/ci.yml), [README.md](../README.md)).

## 2. Current Technology Baseline
| Area | Current state in repo |
|---|---|
| Business domain | Student onboarding, survey operations, staff feedback, notifications, survey results |
| Backend | Java 21, Spring Boot 4.0.3, Spring MVC, Security, Validation, JPA, JWT, springdoc ([backend/pom.xml](../backend/pom.xml)) |
| Architecture | Hexagonal / Ports and Adapters |
| Frontend | React 19, TypeScript, Vite 8, React Router 7, Axios, Tailwind 4 ([frontend/package.json](../frontend/package.json)) |
| Database | SQL Server, JPA `ddl-auto=validate`, manual SQL schema/migrations ([backend/src/main/resources/application.yaml](../backend/src/main/resources/application.yaml), [database/README.md](../database/README.md)) |
| Storage | Student documents stored on local filesystem now ([backend/src/main/java/com/ttcs/backend/adapter/out/persistence/LocalStudentDocumentStorageAdapter.java](../backend/src/main/java/com/ttcs/backend/adapter/out/persistence/LocalStudentDocumentStorageAdapter.java)) |
| Auth | JWT bearer auth, role-based route security, some domain scoping in services; token stored in browser `localStorage` ([backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java](../backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java), [frontend/src/features/auth/authStorage.ts](../frontend/src/features/auth/authStorage.ts)) |
| Deployment | Backend and frontend Dockerfiles; frontend image runs Nginx and proxies `/api` to backend ([frontend/nginx.conf](../frontend/nginx.conf)) |
| CI/CD | GitHub Actions quality gate + GHCR image build/push ([.github/workflows/ci.yml](../.github/workflows/ci.yml), [.github/workflows/ci.yml](../.github/workflows/ci.yml)) |
| Testing | Backend unit/service tests exist; frontend has no Vitest/RTL/Playwright setup |
| Security posture | JWT, BCrypt, CORS, audit trail for successful privileged actions, but no rate limiting, no secret/image/dependency scanning, no CSP/security headers strategy |
| Observability | No Actuator, no health/readiness, no metrics stack; `show-sql: true` is enabled ([backend/src/main/resources/application.yaml](../backend/src/main/resources/application.yaml)) |

## 3. Roadmap Items Extracted from `docs/technical-roadmap.md`
| Item | Repo status |
|---|---|
| React + TypeScript + Vite + Tailwind | Already baseline |
| Shared UI primitives / shadcn/ui | Custom primitives already exist; shadcn not present |
| TanStack Query | Missing |
| TanStack Table | Missing |
| Recharts | Missing |
| Vitest / React Testing Library | Missing |
| Playwright | Missing |
| Spring Boot / Security / JWT / JPA / Validation / Hexagonal | Already baseline |
| Springdoc OpenAPI | Already present |
| Spring Scheduler / Quartz | Missing |
| Spring AI + Gemini | Missing |
| Spring Boot Actuator | Missing |
| SQL Server | Already baseline |
| Redis | Missing |
| MinIO / S3 storage | Missing |
| Elasticsearch / OpenSearch | Missing |
| Flyway / Liquibase | Missing |
| Docker | Already present |
| Docker Compose | Missing |
| Nginx reverse proxy | Partially present |
| GitHub Actions + GHCR | Already present |
| Trivy / Gitleaks / Dependency scanning | Missing |
| Audit logging | Partially implemented |
| Policy-based authorization | Partially implemented informally |
| Reporting read models / summary tables | Missing |
| SSE / WebSocket | Missing |
| Export pipeline: POI / PDF tooling | Missing |
| OAuth2 / OIDC | Missing |
| Feature flags | Missing |
| RabbitMQ | Missing |
| Prometheus / Grafana | Missing |
| Kubernetes / microservices / Kafka / event sourcing / service mesh | Not present, and should stay that way for now |

## 4. Evaluation of Each Roadmap Item

### React + TypeScript + Vite + Tailwind + Shared UI
- Current relevance to project: Very high; this is already the right frontend foundation.
- Suitable for: Both
- Recommended timing: Now, but as continuity rather than change.
- Why it fits this project: The SPA already has reusable shells, pages, and data-view primitives.
- Expected benefits: Stable feature delivery, low migration cost, good demo velocity.
- Risks / drawbacks: Adding `shadcn/ui` now would duplicate the current custom UI layer.
- Integration difficulty: Low if continuing current approach; medium if introducing a second component system.
- Prerequisites: None.
- Concrete use cases in THIS project: Admin tables, survey pages, account/security area.
- Final verdict: Keep the current stack; do not add `shadcn/ui` unless the team explicitly wants to replace existing primitives.

### TanStack Query
- Current relevance to project: High; the frontend has many server-driven pages and manual Axios hooks.
- Suitable for: Both
- Recommended timing: Now
- Why it fits this project: User management, survey management, notifications, survey results, and future reminders/exports all benefit from cache, invalidation, loading, and retry semantics.
- Expected benefits: Cleaner hooks, fewer duplicated loading/error states, easier optimistic refresh after admin actions.
- Risks / drawbacks: Adds another abstraction the team must learn.
- Integration difficulty: Medium
- Prerequisites: Standardize API error handling first.
- Concrete use cases in THIS project: `/admin/users`, `/admin/surveys`, `/survey-results`, `/notifications`.
- Final verdict: Strong recommendation now.

### TanStack Table
- Current relevance to project: Moderate; the repo already has a reusable hand-rolled table component.
- Suitable for: Existing feature enhancement
- Recommended timing: Near future
- Why it fits this project: Helpful if admin tables need column sorting, server state sync, column visibility, or richer row models.
- Expected benefits: Better table ergonomics for complex operational screens.
- Risks / drawbacks: Replacing the current simple table may cost more than it returns right now.
- Integration difficulty: Medium
- Prerequisites: Decide whether current table requirements are truly outgrowing [DataTable.tsx](../frontend/src/components/data-view/DataTable.tsx).
- Concrete use cases in THIS project: Admin users and admin surveys.
- Final verdict: Useful, but not urgent.

### Recharts
- Current relevance to project: Moderate to high; survey results and dashboard analytics are growing.
- Suitable for: Both
- Recommended timing: Near future
- Why it fits this project: The backend already exposes response counts, targeted/opened/submitted metrics.
- Expected benefits: Better operational dashboards and demo quality.
- Risks / drawbacks: Limited value unless reporting/read models mature.
- Integration difficulty: Low
- Prerequisites: Decide which metrics matter and ensure backend responses stay stable.
- Concrete use cases in THIS project: Survey participation funnel, response rate trends, AI sentiment charts later.
- Final verdict: Good next step, not phase-0 work.

### Vitest + React Testing Library
- Current relevance to project: Very high; frontend tests are currently absent.
- Suitable for: Both
- Recommended timing: Now
- Why it fits this project: The frontend contains route guards, auth flows, submission flows, and stateful admin screens that can regress easily.
- Expected benefits: Faster refactoring, safer adoption of TanStack Query, better confidence in auth and forms.
- Risks / drawbacks: Initial setup time.
- Integration difficulty: Low to medium
- Prerequisites: Decide test targets for critical flows first.
- Concrete use cases in THIS project: Login flow, route protection, survey submission UI, pending-student review UI.
- Final verdict: One of the highest-ROI missing technologies.

### Playwright
- Current relevance to project: High, but after unit/component tests.
- Suitable for: Both
- Recommended timing: Near future
- Why it fits this project: This repo has end-to-end business flows that matter more than isolated screens.
- Expected benefits: Protects login, onboarding, survey submission, admin review, and happy-path smoke tests.
- Risks / drawbacks: Needs stable seeded environment and test data.
- Integration difficulty: Medium
- Prerequisites: Add a reproducible local/demo environment first, ideally with Compose.
- Concrete use cases in THIS project: Register -> verify -> upload docs -> approve; admin create/publish survey; student submit survey.
- Final verdict: Recommended, but after environment reproducibility improves.

### Spring Boot + Security + JWT + JPA + Validation + Hexagonal Architecture
- Current relevance to project: Core foundation already in use.
- Suitable for: Both
- Recommended timing: Continue now
- Why it fits this project: The current architecture is consistent with the project’s scope and keeps business logic separated well.
- Expected benefits: Good maintainability and learning value.
- Risks / drawbacks: JWT in `localStorage` increases XSS exposure; roadmap should be updated because repo is on Spring Boot 4, not 3.x.
- Integration difficulty: N/A
- Prerequisites: None
- Concrete use cases in THIS project: Entire backend.
- Final verdict: Keep. Harden around it rather than replace it.

### Springdoc OpenAPI
- Current relevance to project: Already present and useful.
- Suitable for: Both
- Recommended timing: Now
- Why it fits this project: Backend already ships the dependency and exposes Swagger paths publicly ([backend/pom.xml](../backend/pom.xml), [backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java](../backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java)).
- Expected benefits: Better integration discipline, easier academic documentation.
- Risks / drawbacks: Low; main risk is leaving docs poorly tagged or stale.
- Integration difficulty: Low
- Prerequisites: Add grouping/tags and consistent schema descriptions.
- Concrete use cases in THIS project: Auth, admin users, surveys, results, feedback, notifications.
- Final verdict: Keep and enrich; this is not a future item, it is an active foundation item.

### Spring Scheduler / Quartz
- Current relevance to project: High for reminder, export, and AI workflows.
- Suitable for: Both
- Recommended timing: Scheduler now, Quartz later
- Why it fits this project: Reminder workflow and report generation are credible next features.
- Expected benefits: Decouples long-running/system work from HTTP requests.
- Risks / drawbacks: Adds job lifecycle and failure handling complexity.
- Integration difficulty: Medium
- Prerequisites: Define job log schema and idempotency rules.
- Concrete use cases in THIS project: Survey reminders, onboarding follow-up, report generation, AI analysis.
- Final verdict: Strong recommendation, but start with Spring Scheduler. Quartz is premature unless job orchestration becomes complex.

### Spring AI + Gemini
- Current relevance to project: Real but not immediate.
- Suitable for: New features
- Recommended timing: Later
- Why it fits this project: Survey text comments and feedback analysis are a natural extension, and the roadmap is correct to avoid a generic chatbot.
- Expected benefits: Strong demo value and meaningful analytics.
- Risks / drawbacks: Cost, prompt quality, latency, evaluation complexity, privacy concerns.
- Integration difficulty: Medium to high
- Prerequisites: Stable survey result detail pages, background jobs, persisted analysis records, feature flag.
- Concrete use cases in THIS project: Sentiment/topic summary for open-ended survey responses.
- Final verdict: Good showcase feature later; not a foundation technology.

### Spring Boot Actuator
- Current relevance to project: Very high; it is currently missing.
- Suitable for: Existing feature enhancement
- Recommended timing: Now
- Why it fits this project: CI already builds and ships images, but the runtime has no health/readiness story.
- Expected benefits: Health endpoints, deployment readiness, future Prometheus integration.
- Risks / drawbacks: Minimal, if endpoints are secured.
- Integration difficulty: Low
- Prerequisites: Decide which endpoints are exposed publicly vs internally.
- Concrete use cases in THIS project: Container health checks, future Compose startup dependencies, monitoring.
- Final verdict: Must do now.

### SQL Server
- Current relevance to project: Core and appropriate.
- Suitable for: Both
- Recommended timing: Continue now
- Why it fits this project: The current schema is relational and business-heavy; SQL Server fits well.
- Expected benefits: Mature relational capabilities for onboarding/survey/feedback workflows.
- Risks / drawbacks: SQL Server-specific testing and local setup friction.
- Integration difficulty: N/A
- Prerequisites: None
- Concrete use cases in THIS project: All persistent business state.
- Final verdict: Keep. The priority is migration automation and better test support, not changing databases.

### Redis
- Current relevance to project: High, but only for specific uses.
- Suitable for: Both
- Recommended timing: Now for rate limiting, near future for cache/lock
- Why it fits this project: Public auth endpoints are currently unrestricted, and future jobs/AI/export flows need coarse locking.
- Expected benefits: Brute-force protection, spam reduction, faster dashboard reads, duplicate-job prevention.
- Risks / drawbacks: Extra service to run and operate.
- Integration difficulty: Medium
- Prerequisites: Add Compose or another reproducible way to run Redis locally.
- Concrete use cases in THIS project: `login`, `register`, `forgot-password`, `reset-password`, AI/export trigger endpoints.
- Final verdict: Strong recommendation, but be disciplined: rate limiting first, not generic “session cache”.

### MinIO / S3-Compatible Storage
- Current relevance to project: Very high.
- Suitable for: Both
- Recommended timing: Near future
- Why it fits this project: Student documents are currently written to local disk ([LocalStudentDocumentStorageAdapter.java](../backend/src/main/java/com/ttcs/backend/adapter/out/persistence/LocalStudentDocumentStorageAdapter.java)).
- Expected benefits: Better deployment portability, safer document handling, future report export storage.
- Risks / drawbacks: Requires metadata model, access rules, cleanup, and secure download flow.
- Integration difficulty: Medium
- Prerequisites: File metadata schema, download authorization rules, storage adapter abstraction.
- Concrete use cases in THIS project: Student card/NID uploads, exported Excel/PDF reports.
- Final verdict: One of the most justified roadmap items.

### Elasticsearch / OpenSearch
- Current relevance to project: Low to moderate.
- Suitable for: New features
- Recommended timing: Later
- Why it fits this project: Only valuable when text volume and search requirements exceed SQL-like search.
- Expected benefits: Full-text relevance, keyword highlighting, topic/sentiment search later.
- Risks / drawbacks: Large operational overhead for a student project.
- Integration difficulty: High
- Prerequisites: Real search pain in comments/feedback, indexed sync design, fallback behavior.
- Concrete use cases in THIS project: Comment search across large survey datasets.
- Final verdict: Skip for now. The roadmap is right to keep it optional.

### Flyway / Liquibase
- Current relevance to project: Very high.
- Suitable for: Existing feature enhancement
- Recommended timing: Now
- Why it fits this project: Database changes are currently manual SQL files and JPA validates against pre-existing schema ([database/README.md](../database/README.md), [backend/src/main/resources/application.yaml](../backend/src/main/resources/application.yaml)).
- Expected benefits: Safer schema evolution, reproducible environments, easier CI/demo setup.
- Risks / drawbacks: Need to baseline current schema carefully.
- Integration difficulty: Medium
- Prerequisites: Decide on baseline migration strategy and ownership of DB changes.
- Concrete use cases in THIS project: Ongoing audit, recipient tracking, reminder, report, AI tables.
- Final verdict: Must do now.

### Docker + Docker Compose + Nginx
- Current relevance to project: High
- Suitable for: Both
- Recommended timing: Compose now, Docker/Nginx continue now
- Why it fits this project: Images already exist; Nginx already serves the SPA and proxies `/api`; only the orchestration layer is missing ([frontend/nginx.conf](../frontend/nginx.conf), [README.md](../README.md)).
- Expected benefits: One-command local/demo environment, easier Playwright, easier Redis/MinIO adoption.
- Risks / drawbacks: Extra setup and environment templating work.
- Integration difficulty: Low to medium
- Prerequisites: Decide if SQL Server runs in Compose or remains external.
- Concrete use cases in THIS project: Demo environment with frontend, backend, Redis, MinIO.
- Final verdict: Very worthwhile now. This project is already mature enough to justify Compose.

### GitHub Actions + GHCR
- Current relevance to project: Already good baseline.
- Suitable for: Both
- Recommended timing: Continue now
- Why it fits this project: CI already tests backend, lints/builds frontend, and ships images to GHCR.
- Expected benefits: Reproducible builds and decent delivery hygiene.
- Risks / drawbacks: Current pipeline stops short of security and runtime verification.
- Integration difficulty: Low
- Prerequisites: None
- Concrete use cases in THIS project: PR quality gates and release image publishing.
- Final verdict: Keep; extend rather than replace.

### Trivy + Gitleaks + Dependency Scanning
- Current relevance to project: Very high
- Suitable for: Existing feature enhancement
- Recommended timing: Now
- Why it fits this project: CI currently has no security scanning despite building container images ([.github/workflows/ci.yml](../.github/workflows/ci.yml)).
- Expected benefits: Better DevSecOps story, catches accidental secrets and vulnerable dependencies early.
- Risks / drawbacks: Noise from false positives until tuned.
- Integration difficulty: Low
- Prerequisites: CI budget and baseline suppression policy.
- Concrete use cases in THIS project: Scan backend/frontend images, scan repo for leaked keys, enable Dependabot alerts/PRs.
- Final verdict: Must do now.

### Audit Logging
- Current relevance to project: High and already partially implemented.
- Suitable for: Existing feature enhancement
- Recommended timing: Now
- Why it fits this project: `Audit_Log` already exists and captures important privileged actions, but not denied attempts or full review history ([database/README.md](../database/README.md)).
- Expected benefits: Better governance, clearer admin accountability.
- Risks / drawbacks: Over-logging or sensitive data leakage if expanded carelessly.
- Integration difficulty: Low to medium
- Prerequisites: Decide whether to add access-denied and document-review history separately.
- Concrete use cases in THIS project: Onboarding review, survey lifecycle actions, user activation/deactivation.
- Final verdict: Keep and expand. This is already one of the strongest enterprise-like parts of the repo.

### Policy-Based Authorization
- Current relevance to project: High
- Suitable for: Both
- Recommended timing: Now
- Why it fits this project: The repo already has some service-layer scoping for teacher survey-result access, but it is ad hoc rather than a reusable policy layer ([backend/src/main/java/com/ttcs/backend/application/domain/service/GetSurveyResultService.java](../backend/src/main/java/com/ttcs/backend/application/domain/service/GetSurveyResultService.java)).
- Expected benefits: More consistent security decisions, better maintainability as roles/scope rules grow.
- Risks / drawbacks: Can become over-engineered if turned into a mini authorization framework too early.
- Integration difficulty: Medium
- Prerequisites: Identify repeated access rules and centralize them in domain/application policies.
- Concrete use cases in THIS project: Survey results, document access, pending student review, feedback response permissions.
- Final verdict: Recommended now, but implement as simple policy services, not as a heavy external policy engine.

### Reporting Read Models / Summary Tables
- Current relevance to project: High
- Suitable for: Both
- Recommended timing: Near future
- Why it fits this project: Survey results already expose targeted/opened/submitted counts, and dashboards/exports will multiply read-heavy queries.
- Expected benefits: Faster analytics, simpler dashboards, cleaner export logic.
- Risks / drawbacks: Event/update consistency and duplicate data management.
- Integration difficulty: Medium
- Prerequisites: Identify expensive repeated reporting queries first.
- Concrete use cases in THIS project: Participation funnel, overdue onboarding counts, unresolved feedback summary.
- Final verdict: Good next step after Scheduler and migration tooling.

### SSE / WebSocket
- Current relevance to project: Moderate
- Suitable for: New features / existing enhancement
- Recommended timing: Later
- Why it fits this project: More useful for job status and notifications than for core CRUD.
- Expected benefits: Better UX for AI/export/reminder status.
- Risks / drawbacks: Auth, reconnect, and event lifecycle complexity.
- Integration difficulty: Medium
- Prerequisites: Background jobs and async workflows must exist first.
- Concrete use cases in THIS project: Export completed, AI analysis finished, new notification.
- Final verdict: Prefer SSE later; do not prioritize now.

### Export Pipeline: Apache POI / PDF Tooling
- Current relevance to project: High
- Suitable for: Both
- Recommended timing: Near future
- Why it fits this project: Survey results are already meaningful enough to justify offline reporting.
- Expected benefits: Real operational value and good demo/storytelling value.
- Risks / drawbacks: Formatting effort, async job handling, file retention policy.
- Integration difficulty: Medium
- Prerequisites: Background jobs, MinIO, and a report status table.
- Concrete use cases in THIS project: Survey results export, feedback queue export, onboarding queue export.
- Final verdict: Valuable and realistic once storage + scheduler exist.

### OAuth2 / OIDC
- Current relevance to project: Low to moderate
- Suitable for: New features
- Recommended timing: Later
- Why it fits this project: Enterprise-like, but only if the team has access to a real IdP or a credible demo provider.
- Expected benefits: Cleaner institutional login story.
- Risks / drawbacks: Setup complexity and possible distraction from core workflows.
- Integration difficulty: Medium to high
- Prerequisites: Real IdP decision, user provisioning model, token/session redesign.
- Concrete use cases in THIS project: Faculty/admin login with school accounts.
- Final verdict: Keep optional. Not a current priority.

### Feature Flags
- Current relevance to project: Moderate
- Suitable for: Both
- Recommended timing: Later
- Why it fits this project: Helpful once AI/export/reminders are added and need safe rollout.
- Expected benefits: Safer demos and incremental release control.
- Risks / drawbacks: Adds configuration surface area.
- Integration difficulty: Low
- Prerequisites: More optional features must exist first.
- Concrete use cases in THIS project: Toggle AI analysis, report export, SSE, OpenSearch integration.
- Final verdict: Reasonable later; simple DB/config flags are enough.

### RabbitMQ
- Current relevance to project: Low
- Suitable for: New features
- Recommended timing: Not recommended now
- Why it fits this project: The async workload described in the roadmap is not yet large enough to justify queue infrastructure.
- Expected benefits: Better decoupling for larger async systems.
- Risks / drawbacks: Significant operational overhead for limited immediate value.
- Integration difficulty: High
- Prerequisites: Scheduler-based async flows must first hit real limits.
- Concrete use cases in THIS project: None that cannot be handled by Scheduler + DB + Redis right now.
- Final verdict: Skip for now.

### Prometheus + Grafana
- Current relevance to project: Moderate
- Suitable for: Existing feature enhancement
- Recommended timing: Later
- Why it fits this project: Useful only after Actuator and meaningful metrics are in place.
- Expected benefits: Better operational visibility and dashboards.
- Risks / drawbacks: More infrastructure than the repo currently needs.
- Integration difficulty: Medium
- Prerequisites: Actuator, Micrometer metrics, containerized runtime.
- Concrete use cases in THIS project: API latency, job failures, auth failures, reminder counts.
- Final verdict: Good later-stage observability layer, not a phase-1 priority.

### Kubernetes / Microservices / Kafka / Event Sourcing / Service Mesh
- Current relevance to project: Very low
- Suitable for: Neither now
- Recommended timing: Not recommended
- Why it fits this project: The repo is a single bounded application with understandable scale and a student-project ops budget.
- Expected benefits: Mostly academic signaling, not practical improvement.
- Risks / drawbacks: Severe complexity inflation and weaker demo reliability.
- Integration difficulty: High
- Prerequisites: Real scale pain, multiple teams, or hard reliability constraints that do not exist here.
- Concrete use cases in THIS project: None justified by current repo state.
- Final verdict: The roadmap is correct to reject these for now.

## 5. Missing but Valuable Technologies

### Testcontainers
- Why it is missing: Backend tests are mostly service-layer/unit oriented; SQL Server behavior is not exercised realistically.
- What problem it solves: Validates JPA mappings, SQL Server-specific queries, migrations, and persistence behavior in CI.
- Add now or later: Now

### OpenAPI client generation (`openapi-generator` or `orval`)
- Why it is missing: Frontend types and API clients are hand-maintained even though springdoc already exists.
- What problem it solves: Keeps frontend DTOs aligned with backend contracts and reduces drift.
- Add now or later: Near future

### Structured JSON logging + correlation IDs
- Why it is missing: Observability is currently minimal and `show-sql: true` is not a real logging strategy.
- What problem it solves: Better debugging, traceability, and safer ops compared with ad hoc console logs.
- Add now or later: Now, alongside Actuator

## 6. Priority Recommendation
- Must do now: Flyway/Liquibase, Actuator, CI security scanning, Redis-backed rate limiting, Vitest/RTL, Testcontainers.
- Good next step: Docker Compose, MinIO, Spring Scheduler reminder jobs, TanStack Query, reporting read models, structured logging.
- Valuable later: Recharts, export pipeline, Spring AI + Gemini, SSE, Prometheus/Grafana, simple feature flags.
- Skip for now: OpenSearch, OAuth2/OIDC unless a real IdP exists, Quartz, RabbitMQ, Kubernetes, Kafka, microservices.

## 7. Suggested Evolution Path
- Phase 1: Add Flyway, Actuator, security scanning, frontend tests, Testcontainers, and auth/rate-limit hardening.
- Phase 2: Add Compose, Redis, MinIO, Scheduler-based reminders, policy services, and reporting summaries.
- Phase 3: Add exports, charts, AI insight workflows, SSE for job status, and optional monitoring stack.

## 8. Final Conclusion
The roadmap is directionally strong. Its best parts are the emphasis on workflow depth, governance, async operations, and realistic deployment hardening. It correctly avoids trendy overengineering.

Its main weakness is timing discipline. Several items are already implemented in some form and should be reframed as “expand/harden”, not “adopt”: springdoc, audit logging, recipient tracking, Docker, GitHub Actions, GHCR, and Nginx. The repo’s actual highest-value gaps are more basic: migration automation, runtime observability, frontend tests, rate limiting, object storage, and reproducible local orchestration.

Overall, the roadmap is realistic if phased properly. It becomes unrealistic only when later-stage showcase technologies like AI, OpenSearch, OAuth, or RabbitMQ are treated as prerequisites instead of optional upgrades.

## 9. Best Technology Bets
- Flyway or Liquibase: highest operational return because schema evolution is currently manual and fragile.
- Spring Boot Actuator + structured logging: immediate runtime visibility for almost no architectural cost.
- Redis for rate limiting first: directly hardens the most exposed public endpoints.
- MinIO / S3-compatible storage: fixes the clearest production-readiness gap in current onboarding document handling.
- Vitest/RTL plus a small Playwright suite: best protection against regressions across the project’s many user flows.

