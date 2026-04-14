# Next Product Expansion Strategy

## 1. Current Product Reality
- What the system currently is:
  - The repository is a university-facing internal web application that combines student onboarding, survey distribution, survey result viewing, basic user administration, and a simple student feedback inbox.
  - The current product footprint is broader than a single survey app. It has:
    - student registration, email verification, document upload, admin approval
    - survey creation, editing, visibility control, manual close, response submission
    - survey result viewing for admin and teacher roles
    - user management and account activation/deactivation
    - student-submitted feedback with staff replies
  - Evidence is visible in the README flow summary, the Spring controllers, the React route tree, and the SQL schema (`README.md:5-15`, `backend/src/main/java/com/ttcs/backend/adapter/in/web`, `frontend/src/App.tsx`, `database/full_schema.sql`).
- What it does well:
  - The backend is structurally better than typical student projects. It uses ports and adapters instead of collapsing everything into controllers (`README.md:172-180`).
  - Authentication and authorization are real enough to matter: JWT, role checks, and student-state-aware access restrictions exist (`backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java:31-74`, `backend/src/main/java/com/ttcs/backend/adapter/in/web/CurrentStudentProvider.java:44-58`).
  - Survey submission validation is serious enough to show engineering discipline. The code enforces complete answers, one answer per question, type-compatible payloads, and duplicate-submission prevention (`backend/src/main/java/com/ttcs/backend/application/domain/service/SubmitSurveyService.java:35-217`).
  - The project already has CI and Docker images, so it is not purely local-demo code (`.github/workflows/ci.yml:12-119`, `backend/Dockerfile`, `frontend/Dockerfile`).
- Why it still feels shallow:
  - The business flows are narrow and stop too early. Registration ends at approval. Surveying ends at submission and static results. Feedback ends at reply messages.
  - Several features are still operator-thin:
    - pending student review is approve/reject with no reasons or reviewer notes
    - admin survey creation uses raw department IDs in the UI
    - notifications are computed from dates instead of managed as a real delivery workflow
    - dashboards are mostly counts, not operational views
  - The result is a system that is academically implemented but not product-closed. It looks like a solid prototype, not like software an institution would rely on daily.

## 2. Biggest Gaps Preventing It From Feeling Like a Real Product
- Business gaps:
  - Onboarding is too binary.
    - The student state model is basically `EMAIL_UNVERIFIED`, `EMAIL_VERIFIED`, `PENDING`, `ACTIVE`, `REJECTED` (`database/full_schema.sql:44-57`, `AuthUseCaseService.java:220-320`).
    - There is no re-submission loop, no rejection reasons, no review evidence, no support flow.
  - Survey programs are too generic.
    - A survey is mostly title, description, time window, hidden flag, questions, and simple recipient scope (`database/full_schema.sql:84-117`, `CreateSurveyService.java:30-61`).
    - There is no survey type, term, campaign purpose, anonymity policy, or target completion objective.
  - Feedback is too informal.
    - `Feedback` plus `Feedback_Response` is a message thread, not a service case (`database/full_schema.sql:150-170`, `StudentFeedbackService.java:115-147`).
- Lifecycle gaps:
  - No onboarding case lifecycle.
    - Real flow should support submission, review, rejection with reason, correction, resubmission, approval, closure.
  - No survey campaign lifecycle.
    - Current survey lifecycle is effectively date-derived `NOT_OPEN`, `OPEN`, `CLOSED`, with manual hide/close (`AdminSurveyManagementService.java:151-189`).
    - There is no draft, review, publish, archive, cancel, or post-close action phase.
  - No participation lifecycle.
    - The system records only final submission, not invited/opened/in-progress/reminded/not-submitted states.
- Governance gaps:
  - Teacher access is not realistically scoped.
    - Teachers can access survey results broadly through role checks, but no ownership or departmental scoping is evident in result retrieval (`SecurityConfig.java:41-44`, `SurveyResultPersistenceAdapter.java:33-84`).
  - Admin actions are not auditable.
    - Approvals, rejections, deactivations, survey closes, and visibility changes do not appear to write audit logs. Not evident in repository.
  - User state semantics are muddy.
    - `User.verify` is overloaded as activation-ish state while student status separately tracks onboarding state (`database/full_schema.sql:4-13`, `AuthUseCaseService.java:170-172`).
- Reporting gaps:
  - Survey result reporting has no denominator.
    - The repo can count submitted responses, but it does not track how many users were targeted, invited, opened, or ignored.
  - No trend reporting.
    - There is no cross-term or cross-survey comparison layer.
  - No operational SLA reporting.
    - The product cannot report approval turnaround, feedback resolution time, or reminder effectiveness.
- Operational gaps:
  - Pending student review is not a real review workstation.
    - The admin page exposes file paths instead of rich previews and a verification checklist (`frontend/src/features/admin/pages/PendingStudentsPage.tsx:139-176`).
  - Notifications are not operational notifications.
    - The current notification service derives reminders at request time from survey dates rather than storing notification events or delivery state (`GetStudentNotificationsService.java:30-97`).
  - Dashboards are cosmetic.
    - The admin dashboard is mostly pending counts and survey counts, not work queues or risk monitoring (`frontend/src/features/dashboard/pages/AdminDashboardPage.tsx:35-149`).
- Technical credibility gaps:
  - The repo is decent structurally, but still narrow technically.
  - Frontend has no test script at all (`frontend/package.json:6-10`).
  - Deployment realism is incomplete.
    - Docker images exist, but the README explicitly says there is no root orchestration (`README.md:166-170`).
  - File handling is not production-ready.
    - Student documents are copied to a local filesystem path with simple naming (`backend/src/main/java/com/ttcs/backend/adapter/out/persistence/LocalStudentDocumentStorageAdapter.java`).
  - Demo seed realism is weak.
    - Seed data stores plaintext passwords (`database/seed_data.sql:11-18`), which undermines the “real system” impression.

## 3. Product Expansion Directions

### Direction A: Student Onboarding & Verification Operations Platform
- What kind of stronger product it would become:
  - A university onboarding operations system focused on identity verification, student activation, document review, and onboarding case management.
- Which current weaknesses it solves:
  - Fixes the shallow onboarding flow.
  - Creates real admin/operator value.
  - Replaces binary approval with review workflow.
- Which new business flows it introduces:
  - registration -> verification -> document submission -> review assignment -> reject with reason / request correction -> resubmission -> approval -> activation -> audit trail
  - stale onboarding case reminders
  - onboarding queue dashboards by status, age, department, reviewer
- Which new technologies would be justified:
  - Object storage for secure document handling
  - Background jobs for reminders and stale-case escalation
  - Audit log persistence
  - Optional OCR/document validation integration if scoped carefully
- Why this direction is good for a student capstone / portfolio:
  - Strong operations workflow.
  - Clear lifecycle depth.
  - Better internal-tool realism.
- Why it is not the best overall balance:
  - It improves one major area deeply, but it does less to elevate the survey/result side, which is central to the repo identity.

### Direction B: Closed-Loop Survey Operations & Institutional Insight Platform
- What kind of stronger product it would become:
  - A serious survey operations platform for universities: planned campaigns, targeted recipients, reminder loops, response-rate management, result review, and follow-up action tracking.
- Which current weaknesses it solves:
  - Fixes shallow survey CRUD.
  - Fixes weak analytics.
  - Fixes missing closed-loop lifecycle.
  - Fixes teacher access realism if ownership scoping is added.
- Which new business flows it introduces:
  - survey template -> draft -> review -> publish -> invite/target tracking -> reminders -> participation monitoring -> close -> segmented reporting -> action planning -> archive
  - response risk detection for low-performing cohorts
  - term-over-term comparison
- Which new technologies would be justified:
  - Scheduled/background jobs for reminders and aggregation
  - Object storage for exports/report attachments
  - Stronger analytics queries or materialized reporting tables
  - Optional search/filtering layer for surveys, comments, and tickets
  - Policy-based authorization for teacher/admin data scoping
- Why this direction is good for a student capstone / portfolio:
  - Best alignment with the existing repo identity.
  - Strongest balance of product depth and justified technical breadth.
  - Produces visible admin, teacher, and student value without changing domain completely.

### Direction C: Student Experience & Feedback Resolution Platform
- What kind of stronger product it would become:
  - A student-facing issue, feedback, and engagement platform where surveys, notifications, and support cases are unified into a service experience.
- Which current weaknesses it solves:
  - Fixes toy-like feedback.
  - Strengthens communication loop and user value.
  - Makes notifications more real.
- Which new business flows it introduces:
  - student submits issue -> categorization -> assignment -> staff response -> resolution -> student confirmation
  - student inbox with survey reminders, onboarding updates, and case updates
  - escalation workflow for unresolved student issues
- Which new technologies would be justified:
  - Notification event model and job runner
  - Email templates and outbound delivery queue
  - Optional chat-like timeline UI patterns
  - Optional search/filtering across tickets
- Why this direction is good for a student capstone / portfolio:
  - Strong UX and service-workflow story.
  - Good for showing empathy-driven product thinking.
- Why it is not the best overall balance:
  - It risks making surveys feel secondary when surveys are still the strongest implemented module in the repo.

## 4. Best Recommended Direction
- Best direction:
  - Direction B: Closed-Loop Survey Operations & Institutional Insight Platform
- Why this is the best balance of feature depth + technical diversity:
  - The current repo already has the most meaningful base in survey creation, submission, and result viewing.
  - Expanding that into real survey operations creates the largest jump in product seriousness without forcing a domain pivot.
  - It naturally justifies a broader but still disciplined technical set:
    - scheduled/background jobs for reminders and aggregation
    - authorization scoping for teacher/admin visibility
    - reporting tables or export workflows
    - object storage for exports and sensitive files
  - It also keeps onboarding and feedback relevant, because those can be integrated as supporting workflows rather than separate products.
- Why it is better than just adding random CRUD or random technologies:
  - Adding more entities without lifecycle logic would only create more thin screens.
  - Adding Kafka, microservices, Elasticsearch, or other heavy tooling right now would be obvious overengineering because the real product gap is survey lifecycle closure and participation tracking, not infrastructure scale.
- What new value it creates for users/admins:
  - Students get clearer assignments, reminders, and better survey timing visibility.
  - Admins get response-rate management instead of passive survey creation.
  - Teachers get scoped, more actionable result insights.
  - The institution gets evidence of participation quality, not just stored responses.

## 5. Recommended Feature Additions

### Must-have business features
- Survey draft/review/publish/archive lifecycle
  - Problem it solves:
    - Current survey flow is too close to direct CRUD.
  - Why it matters:
    - Real survey programs require governance and scheduling.
  - How it fits the existing repo:
    - Extends current admin survey create/edit/close flow.
  - Expected complexity:
    - Medium
  - Product impact:
    - High
- Survey recipient tracking and participation states
  - Problem it solves:
    - No denominator, no open/start/remind visibility.
  - Why it matters:
    - Response quality cannot be managed otherwise.
  - How it fits the existing repo:
    - Extends `Survey_Assignment`, complements `Survey_Response`.
  - Expected complexity:
    - High
  - Product impact:
    - Critical
- Onboarding rejection reasons and resubmission loop
  - Problem it solves:
    - Current approval is binary and dead-end.
  - Why it matters:
    - Real student onboarding must support correction.
  - How it fits the existing repo:
    - Evolves current approval flow, not a rewrite.
  - Expected complexity:
    - Medium
  - Product impact:
    - High
- Feedback ticket lifecycle
  - Problem it solves:
    - Current feedback is a message board, not a support workflow.
  - Why it matters:
    - Student concerns need ownership and closure.
  - How it fits the existing repo:
    - Builds on current `Feedback` and `Feedback_Response`.
  - Expected complexity:
    - Medium
  - Product impact:
    - High

### Must-have governance/operation features
- Scoped teacher authorization by department/course ownership
  - Problem it solves:
    - Current result access is too broad.
  - Why it matters:
    - Institutional realism and data governance.
  - How it fits the existing repo:
    - Builds on existing roles and assignment model.
  - Expected complexity:
    - Medium
  - Product impact:
    - Critical
- Audit log for privileged actions
  - Problem it solves:
    - No traceability for admin operations.
  - Why it matters:
    - Real internal tools need accountability.
  - How it fits the existing repo:
    - Add around approval, user activation, survey lifecycle actions.
  - Expected complexity:
    - Medium
  - Product impact:
    - High
- Review workstation for onboarding documents
  - Problem it solves:
    - Current admin review is path-based and weak.
  - Why it matters:
    - Makes the onboarding flow believable.
  - How it fits the existing repo:
    - Evolves `PendingStudentsPage`.
  - Expected complexity:
    - Medium
  - Product impact:
    - High
- Actionable dashboards
  - Problem it solves:
    - Current dashboard panels are passive.
  - Why it matters:
    - Operators need work queues and alerts.
  - How it fits the existing repo:
    - Extends current dashboard pages.
  - Expected complexity:
    - Medium
  - Product impact:
    - High

### Must-have reporting/analytics features
- Response rate by target cohort
  - Problem it solves:
    - Current analytics only count completed submissions.
  - Why it matters:
    - Participation management depends on denominators.
  - How it fits the existing repo:
    - Survey result layer and admin dashboard.
  - Expected complexity:
    - High
  - Product impact:
    - Critical
- Trend comparison by term or survey run
  - Problem it solves:
    - Current results have no historical context.
  - Why it matters:
    - Single-run averages are weak for decision-making.
  - How it fits the existing repo:
    - Requires adding survey grouping metadata.
  - Expected complexity:
    - Medium
  - Product impact:
    - High
- Operational metrics
  - Problem it solves:
    - No visibility into approval delays or unresolved feedback.
  - Why it matters:
    - Makes the product look like a managed service.
  - How it fits the existing repo:
    - Admin dashboard and reporting endpoints.
  - Expected complexity:
    - Medium
  - Product impact:
    - High

### Optional advanced features
- Survey templates and cloning
  - Problem it solves:
    - Repeated manual survey configuration.
  - Why it matters:
    - Common real-world operator need.
  - How it fits the existing repo:
    - Extends survey creation.
  - Expected complexity:
    - Medium
  - Product impact:
    - Medium
- Comment tagging / theme extraction
  - Problem it solves:
    - Raw text comments do not scale.
  - Why it matters:
    - Makes qualitative data more useful.
  - How it fits the existing repo:
    - Survey result and feedback reporting layer.
  - Expected complexity:
    - Medium-High
  - Product impact:
    - Medium
- Exportable management reports
  - Problem it solves:
    - Stakeholders often need offline review.
  - Why it matters:
    - Increases institutional realism.
  - How it fits the existing repo:
    - Reporting/export endpoints plus admin UI.
  - Expected complexity:
    - Medium
  - Product impact:
    - Medium

## 6. Recommended Technology Additions

### 1. Background jobs / scheduler
- Name of the technology or technical pattern:
  - Spring scheduled jobs or a lightweight job runner pattern
- What exact problem it solves:
  - Reminders, stale-case detection, result aggregation, and notification dispatch should not depend on users loading a page.
- What feature/workflow it enables:
  - survey reminder emails
  - stale onboarding escalation
  - periodic operational metric refresh
- Why it is justified here:
  - The current notification model is computed on read, which is not a real notification system.
- Essential or optional:
  - Essential
- Improves:
  - product realism
  - architecture maturity
  - deploy/ops realism
  - recruiter impression
- Overengineering warning:
  - Do not introduce a distributed queue first. Start with scheduler + database-backed job records if needed.

### 2. Object storage abstraction
- Name of the technology or technical pattern:
  - S3-compatible object storage integration
- What exact problem it solves:
  - Local file copy for student documents is weak and unsafe for a realistic deployment.
- What feature/workflow it enables:
  - secure document storage
  - signed access to onboarding files
  - export/report file storage
- Why it is justified here:
  - The product already handles student identity documents. This is sensitive data.
- Essential or optional:
  - Essential
- Improves:
  - product realism
  - architecture maturity
  - deploy/ops realism
  - recruiter impression
- Overengineering warning:
  - Do not build a separate file service. A storage adapter is enough.

### 3. Authorization policy layer
- Name of the technology or technical pattern:
  - Policy-based authorization over role-only authorization
- What exact problem it solves:
  - Roles alone are too broad for teacher result visibility and delegated administration.
- What feature/workflow it enables:
  - department-scoped lecturer access
  - owned-survey management
  - safer future delegation
- Why it is justified here:
  - Current role checks are too coarse for a real institution.
- Essential or optional:
  - Essential
- Improves:
  - product realism
  - architecture maturity
  - recruiter impression
- Overengineering warning:
  - This does not require a heavy external authorization platform. Start in application code with explicit policy services.

### 4. Audit log model
- Name of the technology or technical pattern:
  - Domain audit log / activity journal
- What exact problem it solves:
  - No traceability for privileged actions.
- What feature/workflow it enables:
  - approval history
  - survey lifecycle trace
  - user account action trace
- Why it is justified here:
  - Admin-heavy internal products need accountability.
- Essential or optional:
  - Essential
- Improves:
  - product realism
  - architecture maturity
  - deploy/ops realism
  - recruiter impression
- Overengineering warning:
  - A simple audit table is enough. No need for event sourcing.

### 5. Master data API + controlled selectors
- Name of the technology or technical pattern:
  - Master-data endpoints and controlled-value UI selectors
- What exact problem it solves:
  - Current admin flows leak raw IDs and manual values.
- What feature/workflow it enables:
  - department dropdowns
  - term/program/course selection
  - cleaner report filters
- Why it is justified here:
  - A real product should not make admins remember database keys.
- Essential or optional:
  - Essential
- Improves:
  - product realism
  - architecture maturity
  - recruiter impression
- Overengineering warning:
  - Keep it simple. This is not MDM software.

### 6. Reporting tables / precomputed aggregates
- Name of the technology or technical pattern:
  - Reporting read models or summary tables
- What exact problem it solves:
  - Operational dashboards and cohort analytics become inefficient or messy when computed ad hoc from transactional tables only.
- What feature/workflow it enables:
  - response-rate dashboards
  - cohort trend comparisons
  - faster admin reporting
- Why it is justified here:
  - The product’s main growth path is reporting depth.
- Essential or optional:
  - Essential once recipient tracking is added
- Improves:
  - product realism
  - architecture maturity
  - recruiter impression
- Overengineering warning:
  - Do not add a separate warehouse yet. Start with SQL views or summary tables updated by jobs.

### 7. Email notification templating
- Name of the technology or technical pattern:
  - Template-driven outbound email workflow
- What exact problem it solves:
  - Current email usage is narrow and not tied to lifecycle communications.
- What feature/workflow it enables:
  - approval/rejection notices
  - reminder emails
  - feedback resolution notices
- Why it is justified here:
  - The repo already uses Resend for verification.
- Essential or optional:
  - Essential
- Improves:
  - product realism
  - deploy/ops realism
  - recruiter impression
- Overengineering warning:
  - Do not add a dedicated notification microservice.

### 8. Observability basics
- Name of the technology or technical pattern:
  - Health checks, structured logs, metrics
- What exact problem it solves:
  - The current repo can build, but operating it would still be blind.
- What feature/workflow it enables:
  - operational monitoring
  - failure diagnosis
  - visibility into reminder jobs and approval backlogs
- Why it is justified here:
  - Necessary for any product claiming deployment realism.
- Essential or optional:
  - Essential
- Improves:
  - architecture maturity
  - deploy/ops realism
  - recruiter impression
- Overengineering warning:
  - Basic metrics and logs are enough. Full distributed tracing is optional.

### 9. Integration and UI tests
- Name of the technology or technical pattern:
  - Backend integration tests and frontend interaction tests
- What exact problem it solves:
  - Current quality evidence is incomplete, especially on the frontend.
- What feature/workflow it enables:
  - confident changes to onboarding, survey lifecycle, and dashboards
- Why it is justified here:
  - The repo is already large enough that regression risk is real.
- Essential or optional:
  - Essential
- Improves:
  - architecture maturity
  - recruiter impression
- Overengineering warning:
  - Do not chase perfect coverage. Cover the business-critical flows.

### 10. Root deployment orchestration
- Name of the technology or technical pattern:
  - Docker Compose or equivalent local environment orchestration
- What exact problem it solves:
  - The repo has container images but no coherent runtime stack.
- What feature/workflow it enables:
  - realistic local demo
  - easier capstone presentation
  - repeatable onboarding/survey/report demos
- Why it is justified here:
  - The README explicitly notes orchestration is missing.
- Essential or optional:
  - Optional but strongly recommended
- Improves:
  - deploy/ops realism
  - recruiter impression
- Overengineering warning:
  - Compose is enough. Kubernetes would be resume decoration here.

Technologies that would currently be overengineering:
- Microservices
- Kafka or large-scale messaging infrastructure
- Elasticsearch before improving SQL filters/reporting
- Kubernetes for this repo size
- Event sourcing
- Full CQRS separation beyond lightweight reporting read models

## 7. Balanced Expansion Plan

### Phase 1: Highest-value additions
- Add onboarding rejection reasons, reviewer notes, and resubmission.
- Add master-data selectors for departments and other controlled values.
- Add teacher access scoping.
- Add audit logging for admin actions.
- Add frontend tests for onboarding, survey submission, and admin review.

### Phase 2: Realism and operations
- Add survey draft/review/publish/archive lifecycle.
- Add recipient tracking and participation states.
- Add scheduled reminders and persistent notifications.
- Add actionable admin dashboards and operational metrics.
- Move document storage to object storage abstraction.

### Phase 3: Advanced but justified enhancements
- Add trend reporting by term/campaign.
- Add exportable management reports.
- Add feedback ticket assignment/status workflow.
- Add reporting summary tables for faster analytics.
- Add stronger observability and local runtime orchestration.

## 8. Final Recommendation
- Top 5 features to add:
  - Survey lifecycle with draft/review/publish/archive
  - Recipient tracking and response-rate analytics
  - Onboarding rejection reasons and resubmission
  - Scoped lecturer access and ownership rules
  - Feedback ticket workflow with status and ownership
- Top 5 technologies/patterns to add:
  - Background jobs / scheduler
  - Object storage abstraction
  - Authorization policy layer
  - Audit log model
  - Reporting read models / summary tables
- The 3 best feature+tech combinations that create the strongest portfolio value:
  - Survey reminder and participation tracking + background jobs
  - Secure onboarding review + object storage + audit logging
  - Scoped result analytics + authorization policy layer + reporting summary tables
