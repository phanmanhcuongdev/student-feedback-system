# Feature-Tech Mapping Plan

## 1. Current Feature Baseline
- Current implemented feature groups:
  - Student onboarding
    - registration
    - email verification through Resend
    - document upload
    - admin approval or rejection
    - Evidence: `README.md:7-14`, `backend/src/main/java/com/ttcs/backend/application/domain/service/AuthUseCaseService.java`, `backend/src/main/java/com/ttcs/backend/application/domain/service/AdminStudentApprovalService.java`
  - Survey operations
    - survey creation
    - question setup
    - recipient scope by all students or department
    - survey visibility control
    - survey close
    - survey submission
    - Evidence: `backend/src/main/java/com/ttcs/backend/application/domain/service/CreateSurveyService.java`, `AdminSurveyManagementService.java`, `SubmitSurveyService.java`
  - Results and dashboards
    - survey result summaries
    - question-level breakdowns
    - student/admin/lecturer dashboards
    - Evidence: `backend/src/main/java/com/ttcs/backend/adapter/out/persistence/surveyresult/SurveyResultPersistenceAdapter.java`, `frontend/src/features/dashboard/pages/*`
  - User management
    - list users
    - edit user basics
    - activate/deactivate user
    - Evidence: `backend/src/main/java/com/ttcs/backend/application/domain/service/AdminUserManagementService.java`
  - Student feedback inbox
    - student submits feedback
    - admin/teacher replies
    - Evidence: `backend/src/main/java/com/ttcs/backend/application/domain/service/StudentFeedbackService.java`
  - Notifications
    - survey-related notification feed derived from survey dates
    - Evidence: `backend/src/main/java/com/ttcs/backend/application/domain/service/GetStudentNotificationsService.java`
- Which ones are shallow and why:
  - Onboarding is shallow because it is a binary funnel.
    - Student states exist, but there is no reviewer note, rejection reason, correction loop, or case timeline (`database/full_schema.sql:44-69`, `AdminStudentApprovalService.java:48-84`).
  - Survey management is shallow because it is still campaign CRUD.
    - There is no draft, approval, publish, archive, or participation tracking lifecycle (`AdminSurveyManagementService.java:83-189`).
  - Results are shallow because they are descriptive only.
    - The system counts responses and computes averages, but does not know the denominator, response rate, or cross-run trends (`SurveyResultPersistenceAdapter.java:33-138`).
  - Feedback is shallow because it is a message thread rather than a managed case.
    - No category, assignee, priority, status, SLA, or closure path is evident (`StudentFeedbackService.java:115-147`, `database/full_schema.sql:150-170`).
  - Notifications are shallow because they are not real notifications.
    - They are computed on request from survey windows, not stored, delivered, tracked, or escalated (`GetStudentNotificationsService.java:30-97`, `database/full_schema.sql:173-187`).
  - Dashboards are shallow because they are mostly counts and cards.
    - They summarize state but do not help operators work queues or manage exceptions (`frontend/src/features/dashboard/pages/AdminDashboardPage.tsx:35-149`).

## 2. Proposed New Features

### Business workflow features
- Onboarding review workflow with correction loop
  - Description:
    - Replace plain approve/reject with a full case flow: submitted -> under review -> rejected with reason -> corrected -> resubmitted -> approved.
  - User value:
    - Students can recover from mistakes instead of getting stuck after rejection.
    - Admins can review consistently and explain decisions.
  - Why current system needs it:
    - Current approval flow is a dead-end binary decision with no reviewer context.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Critical
- Survey lifecycle workflow
  - Description:
    - Add draft, review, publish, active monitoring, close, archive, and optional cancel states for surveys.
  - User value:
    - Admins can safely prepare and govern surveys before exposing them.
  - Why current system needs it:
    - Current survey state is mostly date-derived plus hide/close controls.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Critical
- Recipient tracking and participation status
  - Description:
    - Track who was targeted, who opened, who started, who submitted, and who ignored a survey.
  - User value:
    - Admins and teachers can measure participation quality rather than just raw response counts.
  - Why current system needs it:
    - The repo currently knows only about completed responses.
  - Implementation difficulty:
    - High
  - Priority:
    - Critical
- Feedback ticket workflow
  - Description:
    - Convert student feedback into categorized tickets with assignment, status, and resolution.
  - User value:
    - Students get clearer follow-up; staff can manage issues instead of replying ad hoc.
  - Why current system needs it:
    - The current feedback feature is too informal for real service management.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High
- Survey templates and reuse
  - Description:
    - Save reusable question sets and create new survey runs from templates.
  - User value:
    - Admins avoid rebuilding recurring survey structures.
  - Why current system needs it:
    - The current create flow is manual and repetitive.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Medium

### Admin / governance features
- Rejection reasons, reviewer notes, and decision audit for onboarding
  - Description:
    - Every approval or rejection stores who acted, why, and when.
  - User value:
    - Creates accountability and transparency.
  - Why current system needs it:
    - Current admin review is opaque.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Critical
- Teacher/lecturer access scoping
  - Description:
    - Restrict result access by department, course ownership, or assigned survey scope.
  - User value:
    - Prevents oversharing and aligns with institutional data boundaries.
  - Why current system needs it:
    - Current result access is role-based and too broad.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Critical
- Survey ownership and approval model
  - Description:
    - Add explicit survey owner, reviewer, approver, and lifecycle responsibility.
  - User value:
    - Governance becomes traceable and realistic.
  - Why current system needs it:
    - `created_by` alone is not sufficient operational ownership.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High
- Controlled master data for departments and future academic dimensions
  - Description:
    - Add managed reference data and UI selectors instead of raw IDs.
  - User value:
    - Cleaner admin workflows, fewer mistakes.
  - Why current system needs it:
    - Admin UI currently leaks department IDs.
  - Implementation difficulty:
    - Low-Medium
  - Priority:
    - High

### Reporting / analytics features
- Response-rate reporting
  - Description:
    - Show invites, opens, submissions, completion rate, and non-response rate.
  - User value:
    - Makes survey operations meaningful and measurable.
  - Why current system needs it:
    - Current analytics do not have a denominator.
  - Implementation difficulty:
    - High
  - Priority:
    - Critical
- Trend comparison by term/campaign
  - Description:
    - Compare current survey runs with previous runs.
  - User value:
    - Helps interpret whether results are improving or declining.
  - Why current system needs it:
    - Single-run averages are weak decision support.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High
- Operational backlog metrics
  - Description:
    - Report pending approvals by age, average review time, unresolved feedback counts, overdue tickets.
  - User value:
    - Makes dashboards operational rather than cosmetic.
  - Why current system needs it:
    - Current dashboards do not help run the service.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High
- Exportable reports
  - Description:
    - CSV/Excel exports for management and lecturer review.
  - User value:
    - Useful for presentations, governance meetings, and offline review.
  - Why current system needs it:
    - The current result pages are view-only.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Medium

### Operational / notification features
- Persistent in-app notifications
  - Description:
    - Store notifications with read state, type, link, and delivery timestamp.
  - User value:
    - Students see reliable reminders and status changes.
  - Why current system needs it:
    - Current notifications are recomputed and non-persistent.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High
- Email reminder workflow
  - Description:
    - Send reminders for pending surveys, onboarding decisions, and feedback resolution.
  - User value:
    - Improves participation and closes the loop proactively.
  - Why current system needs it:
    - Resend is only used for verification/reset flows today.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High
- Stale-case escalation
  - Description:
    - Flag overdue onboarding cases or unresolved feedback tickets.
  - User value:
    - Prevents silent backlog accumulation.
  - Why current system needs it:
    - No active operational escalation exists.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Medium

### Security / audit features
- Audit trail for privileged actions
  - Description:
    - Record admin/teacher lifecycle actions with actor, target, timestamp, and payload summary.
  - User value:
    - Supports accountability and troubleshooting.
  - Why current system needs it:
    - No audit trail is evident for important administrative decisions.
  - Implementation difficulty:
    - Medium
  - Priority:
    - Critical
- Secure file validation and handling
  - Description:
    - Validate MIME, size, naming, and storage access for uploaded documents.
  - User value:
    - Safer handling of sensitive student identity files.
  - Why current system needs it:
    - Current storage flow is a simple filesystem copy.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High
- Split account activation from verification semantics
  - Description:
    - Separate email verified, account enabled, and onboarding status.
  - User value:
    - More consistent login behavior and clearer admin management.
  - Why current system needs it:
    - Current `verify` flag is overloaded.
  - Implementation difficulty:
    - Medium
  - Priority:
    - High

## 3. Technology/Pattern Candidates
- Object storage
  - What it is:
    - Externalized storage for uploaded files and exports via an S3-compatible API or similar.
  - What exact problem it solves here:
    - Replaces local `uploads/student-docs` storage for identity files.
  - Why it is not just resume decoration:
    - The product already handles sensitive files. This solves a real security and deployment problem.
- Background job scheduler / lightweight queue pattern
  - What it is:
    - Scheduled jobs or queued tasks executed outside request/response.
  - What exact problem it solves here:
    - Sends reminders, computes stale-case flags, refreshes reporting summaries.
  - Why it is not just resume decoration:
    - Current notification logic is page-driven, which is operationally weak.
- Server-side caching
  - What it is:
    - Caching for low-volatility reference data or frequently requested report summaries.
  - What exact problem it solves here:
    - Reduces repeated reads for departments, dashboard counters, and reporting summaries if they grow.
  - Why it is not just resume decoration:
    - Only justified after reporting and master-data endpoints expand. Not a first move.
- Audit logging
  - What it is:
    - Persistent logging of business actions, not just console logs.
  - What exact problem it solves here:
    - Tracks who approved, rejected, closed, hid, deactivated, or changed critical records.
  - Why it is not just resume decoration:
    - This repo already contains sensitive admin actions that should be traceable.
- Structured logging / observability
  - What it is:
    - Structured application logs, health checks, and metrics.
  - What exact problem it solves here:
    - Makes reminder jobs, file operations, email delivery, and approval flows diagnosable.
  - Why it is not just resume decoration:
    - The repo already has CI and Docker images; without observability, runtime realism is incomplete.
- Email workflow expansion
  - What it is:
    - Broader lifecycle email handling using the existing Resend integration.
  - What exact problem it solves here:
    - Supports reminders, approval/rejection notifications, and closure messages.
  - Why it is not just resume decoration:
    - The repo already depends on email for verification, so extending it is a natural product improvement.
- Search/filtering improvements
  - What it is:
    - Better SQL-backed filtering, sorting, and server-side query support for users, surveys, tickets, and results.
  - What exact problem it solves here:
    - Current screens will not scale once data becomes more realistic.
  - Why it is not just resume decoration:
    - Can be built with the current database before introducing any separate search engine.
- Export/report pipeline
  - What it is:
    - Report generation and downloadable exports.
  - What exact problem it solves here:
    - Turns result pages into artifacts usable in real meetings and governance workflows.
  - Why it is not just resume decoration:
    - Reporting is a real product gap in this repo.
- Role scoping / policy layer
  - What it is:
    - Authorization logic based on ownership and scope, not only broad roles.
  - What exact problem it solves here:
    - Fixes teacher visibility and future delegated admin boundaries.
  - Why it is not just resume decoration:
    - The repo’s current role model is too coarse for realistic institutional data access.
- File validation/security hardening
  - What it is:
    - Validation rules, access controls, and storage hardening for uploaded files.
  - What exact problem it solves here:
    - Reduces risk around student document uploads.
  - Why it is not just resume decoration:
    - The repo already stores identity documents.
- Integration testing
  - What it is:
    - End-to-end backend flow tests across service, persistence, and security boundaries.
  - What exact problem it solves here:
    - Protects onboarding, survey submission, and lifecycle changes from regression.
  - Why it is not just resume decoration:
    - The repo is already big enough that change risk is real.
- Frontend testing
  - What it is:
    - UI interaction tests for critical flows.
  - What exact problem it solves here:
    - The frontend currently has no testing story.
  - Why it is not just resume decoration:
    - It strengthens capstone quality and protects multi-step workflows.
- Deployment orchestration
  - What it is:
    - Root-level orchestration for frontend, backend, database, and optional supporting services.
  - What exact problem it solves here:
    - The repo has images but no coherent runtime stack.
  - Why it is not just resume decoration:
    - Makes demos repeatable and the repo look operable.

## 4. Feature-to-Tech Mapping Matrix

| Feature | Business problem solved | Tech/pattern needed | Why this tech is justified | Can be done with current stack only? | Priority |
|---|---|---|---|---|---|
| Onboarding correction loop | Rejected students currently have no recovery path | Audit logging, file validation hardening, master-data-backed review UI | Review decisions and document handling need traceability and safer file flow | Partially | Critical |
| Reviewer notes and rejection reasons | Admin decisions are opaque | Audit logging, controlled workflow states | These actions should be explainable and recorded | Yes | Critical |
| Survey draft/review/publish/archive | Survey creation is too close to CRUD | Workflow state model, integration tests | Lifecycle expansion is core business logic, not infrastructure-heavy | Yes | Critical |
| Recipient tracking and participation states | No response denominator or participation insight | Reporting read models, background jobs | Participation tracking becomes more useful when metrics and reminders are automated | Partially | Critical |
| Response-rate reporting | Current analytics only count submissions | Reporting read models / summary tables | Raw transactional queries will become clumsy and slow as reporting expands | Partially | Critical |
| Teacher access scoping | Result access is too broad | Policy layer / scoped authorization | Role-only checks are not enough for institutional realism | Partially | Critical |
| Persistent in-app notifications | Current notifications are computed, not managed | Background jobs, notification persistence model | Reminders and state-change alerts need stored delivery events | Partially | High |
| Email reminder workflow | No proactive reminder or decision emails | Email workflow expansion, background jobs | Outbound lifecycle communication is already adjacent to current Resend usage | No | High |
| Operational dashboard for backlog and risk | Current dashboards are passive | Reporting read models, structured metrics | Operational views need precomputed metrics and measurable statuses | Partially | High |
| Feedback ticket workflow | Feedback is a message stream, not a managed service process | Workflow states, search/filtering improvements, audit logging | Ticket handling needs categorization, assignment, visibility, and action trace | Yes | High |
| Document review workstation | Admin sees file paths instead of a review tool | Object storage, file hardening, frontend interaction tests | Sensitive document review should use secure access and usable UI | No | High |
| Exportable management reports | Results are view-only | Export/report pipeline, object storage for generated files | Management reporting is a real use case beyond on-screen stats | No | Medium |
| Survey templates and cloning | Repeated survey configuration is manual | Current stack plus integration tests | Mostly domain logic and UI reuse, no special tech required | Yes | Medium |
| Trend reporting by term/campaign | Single-run results are weak context | Reporting read models, master data dimensions | Cross-run analytics need stable grouping metadata and summary views | Partially | Medium |
| Stale-case escalation | Backlogs can silently accumulate | Background jobs, structured logging/metrics | Time-based operational rules need asynchronous evaluation | No | Medium |
| Secure document storage | Local filesystem handling is weak | Object storage, file validation hardening | Identity files are sensitive and should not stay tied to local disk paths | No | High |
| Frontend regression protection | No frontend testing exists | Frontend testing | Multi-step admin and student workflows are already complex enough to break | No | High |
| Runtime credibility | Images exist but no cohesive runtime | Deployment orchestration, observability basics | Makes the repo operable and demoable as a system | No | Medium |

## 5. Best Combinations
- Survey participation tracking + background jobs
  - Why it is strong:
    - It closes one of the biggest product gaps: the system can finally manage participation instead of merely collecting completed responses.
  - What it demonstrates technically:
    - Lifecycle modeling
    - asynchronous processing
    - reporting-oriented design
  - What it improves in the product:
    - Reminder loop
    - response-rate analytics
    - operational survey management
- Onboarding review workflow + object storage + audit logging
  - Why it is strong:
    - It converts a toy-like approval flow into a believable admin workflow.
  - What it demonstrates technically:
    - secure file handling
    - admin workflow design
    - traceable business actions
  - What it improves in the product:
    - student recovery path
    - reviewer accountability
    - production realism
- Teacher result scoping + policy layer
  - Why it is strong:
    - It addresses a concrete governance flaw, not just a code-style issue.
  - What it demonstrates technically:
    - authorization design beyond static roles
    - domain-aware access control
  - What it improves in the product:
    - institutional realism
    - safer data boundaries
- Operational dashboards + reporting read models
  - Why it is strong:
    - It upgrades dashboards from decorative to operational.
  - What it demonstrates technically:
    - reporting architecture
    - read-model design
    - metric-driven product thinking
  - What it improves in the product:
    - backlog management
    - response risk detection
    - admin value
- Feedback ticket workflow + search/filtering improvements + audit logging
  - Why it is strong:
    - It turns a weak side-feature into a real service-management function.
  - What it demonstrates technically:
    - workflow state handling
    - admin usability thinking
    - support-style product design
  - What it improves in the product:
    - clearer issue ownership
    - follow-up visibility
    - resolution tracking

## 6. Recommended Final Scope

### Small expansion
- Features to add:
  - rejection reasons and reviewer notes
  - teacher access scoping
  - department selectors/master-data UI
  - audit trail for admin actions
- Technologies to add:
  - audit logging
  - policy layer
  - frontend testing
- What not to add yet:
  - object storage migration
  - reporting read models
  - export pipeline
  - queues or heavy async infrastructure

### Medium expansion
- Features to add:
  - onboarding correction loop
  - survey lifecycle states
  - persistent in-app notifications
  - operational dashboard metrics
  - feedback ticket statuses
- Technologies to add:
  - background jobs
  - audit logging
  - policy layer
  - frontend testing
  - integration testing
- What not to add yet:
  - separate search engine
  - Kubernetes
  - microservices

### Strong expansion
- Features to add:
  - recipient tracking and participation states
  - response-rate reporting
  - onboarding review workstation
  - email reminder workflow
  - trend reporting
  - exportable reports
  - feedback ticket workflow with assignment
- Technologies to add:
  - background jobs
  - object storage
  - policy layer
  - audit logging
  - reporting read models
  - structured observability
  - deployment orchestration
- What not to add yet:
  - Kafka
  - Elasticsearch
  - microservices
  - event sourcing
  - Kubernetes

## 7. Final Recommendation
- Top 7 features:
  - Onboarding correction loop
  - Reviewer notes and rejection reasons
  - Survey draft/review/publish/archive lifecycle
  - Recipient tracking and participation states
  - Response-rate reporting
  - Teacher access scoping
  - Feedback ticket workflow
- Top 7 technologies/patterns:
  - Background jobs / scheduler
  - Object storage
  - Policy-based authorization layer
  - Audit logging
  - Reporting read models / summary tables
  - Frontend testing
  - Deployment orchestration
- Top 5 feature-tech pairs I should actually build:
  - Recipient tracking and participation states + background jobs
  - Onboarding correction loop + object storage
  - Reviewer notes/rejection reasons + audit logging
  - Teacher access scoping + policy-based authorization
  - Response-rate reporting + reporting read models / summary tables

Overengineering risks to avoid:
- Do not introduce distributed messaging unless scheduled/background jobs are clearly insufficient.
- Do not add a separate search platform before improving SQL-backed filtering and reporting.
- Do not split into microservices. The repo does not have the product complexity or team scale to justify it.
- Do not add Kubernetes just to improve portfolio optics. A well-orchestrated local/runtime stack is enough here.
