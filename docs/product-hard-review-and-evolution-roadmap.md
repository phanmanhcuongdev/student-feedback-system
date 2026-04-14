# Product Hard Review & Evolution Roadmap

## 1. Executive Summary
- Based on the repository, this project is a university-facing student feedback and onboarding system with four implemented capability clusters:
  - student onboarding: registration, email verification, document upload, admin approval
  - survey operations: survey creation, visibility control, submission, result viewing
  - user administration: list, edit, activate/deactivate users
  - basic feedback inbox: student submits feedback, staff replies
- The implementation is broader than a single CRUD demo, but it is still mainly an academic prototype. The repo already has many screens and endpoints, yet most flows stop after the first successful transaction instead of reaching a real operational outcome.
- Blunt verdict: this is not a real product yet. It is a polished feature demo with fragments of an internal tool. It looks more complete than a typical student assignment, but it still lacks business closure, operational control, lifecycle depth, and product realism.

## 2. What the Product Appears to Be
- Intended product vision inferred from code and docs:
  - A university internal platform for onboarding student accounts, distributing institutional or course feedback surveys, collecting student responses, and giving admins and lecturers some visibility into results.
- Evidence:
  - The root README defines the system as "collecting student survey responses and reviewing onboarding requests" and lists onboarding, survey submission, and survey result review as core flows (`README.md:5-15`, `README.md:134-162`).
  - The schema includes `Student`, `Teacher`, `Survey`, `Survey_Assignment`, `Survey_Response`, `Feedback`, and onboarding token tables (`database/full_schema.sql:44-187`).
  - Frontend routing exposes separate student, lecturer, and admin surfaces (`frontend/src/App.tsx`).
- Primary users:
  - Students: register, verify email, upload documents, take surveys, send feedback.
  - Admins: approve students, manage surveys, manage users, review results.
- Secondary users:
  - Teachers/lecturers: view survey results and respond to feedback.
- Core user goals:
  - Students want a verified account, clear action queue, accessible assigned surveys, and evidence their feedback was received.
  - Admins want controlled onboarding, targeted survey distribution, response monitoring, and the ability to manage accounts without touching the database.
  - Teachers want actionable results scoped to the classes or departments they actually own.
- Organization/business value:
  - Reduce manual onboarding friction.
  - Increase response collection for institutional/course feedback.
  - Provide a channel for student concerns and improvement requests.
- Product vision clarity:
  - Partially clear, but inconsistent.
  - The schema hints at a broader evaluator/subject model with `teacher_id`, `evaluator_type`, and `subject_type` (`database/full_schema.sql:108-133`), but the application only uses student evaluators and all-student or department targeting (`backend/src/main/java/com/ttcs/backend/application/domain/service/CreateSurveyService.java:56-84`, `backend/src/main/java/com/ttcs/backend/application/domain/service/GetSurveyService.java:68-82`).
  - That mismatch makes the domain feel unfinished rather than intentionally scoped.

## 3. Current Strengths
- Architecture/technical strengths:
  - The backend is organized with a real ports-and-adapters structure instead of controller-heavy spaghetti (`README.md:172-180`).
  - Security is not fake. The app uses JWT, role-based endpoint guards, and state-aware student access checks (`backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java:31-74`, `backend/src/main/java/com/ttcs/backend/adapter/in/web/CurrentStudentProvider.java:44-58`).
  - Survey submission validation is stricter than typical student CRUD work: every question must be answered once, rating/text shape is enforced, duplicate submission is blocked (`backend/src/main/java/com/ttcs/backend/application/domain/service/SubmitSurveyService.java:35-217`).
  - CI exists and at least runs backend tests plus frontend lint/build (`.github/workflows/ci.yml:12-52`).
  - Container images exist for backend and frontend (`backend/Dockerfile:1-37`, `frontend/Dockerfile:1-35`).
- Product/feature strengths:
  - Student onboarding is not just registration. It includes verification and document upload before activation (`README.md:136-149`, `backend/src/main/java/com/ttcs/backend/application/domain/service/AuthUseCaseService.java:90-320`).
  - Admin survey management supports edit/close/hide, not just create (`backend/src/main/java/com/ttcs/backend/application/domain/service/AdminSurveyManagementService.java:83-189`).
  - There is a separate feedback channel outside surveys, which is a good instinct for a real institution-facing product (`backend/src/main/java/com/ttcs/backend/application/domain/service/StudentFeedbackService.java:51-147`).
- Documentation/process strengths:
  - The repo has a reasonably accurate README, environment notes, API contract, schema, and seed data (`README.md`, `API_CONTRACT.md`, `database/full_schema.sql`, `database/seed_data.sql`).
  - The docs are explicit about real email dependency and schema validation, which is more honest than most student repos (`README.md:70-75`, `README.md:166-170`).

## 4. Hard Review: Where the System Is Still “Student Project-Like”

| Category | Current Repo Evidence | Why It Is Weak In Real Product Terms | Impact |
|---|---|---|---|
| Shallow business logic | Student lifecycle is only `EMAIL_UNVERIFIED -> EMAIL_VERIFIED -> PENDING -> ACTIVE/REJECTED` (`AuthUseCaseService.java:220-320`, `AdminStudentApprovalService.java:48-84`, `database/full_schema.sql:44-57`) | This is a thin approval funnel, not a real onboarding lifecycle. There is no resubmission loop, no expiry, no manual review notes, no risk flags, no appeal, no partial approval, no document verification result. | Admin review becomes binary and opaque; students have no recoverable path after rejection; operations cannot explain decisions. |
| Disconnected feature flows | Survey flow ends at submission, result flow ends at viewing, feedback flow ends at reply stream (`SubmitSurveyService.java:92-105`, `SurveyResultPersistenceAdapter.java:33-138`, `StudentFeedbackService.java:115-147`) | Real products connect actions to outcomes: response rates, follow-up actions, course improvement tasks, closure communications, archived decisions. | Users provide input, but the system shows no institutional response loop. It feels extractive rather than useful. |
| No closed-loop lifecycle | Admin can create, hide, and close surveys, but there is no draft/approval/publish/archive lifecycle (`AdminSurveyManagementService.java:83-189`) | Production survey programs need governance. Someone should be able to draft, review, approve, schedule, monitor, close, archive, and compare runs. | Surveys can be launched too early, edited with weak control, and disappear into static result pages. |
| Unrealistic role model | Teachers can see survey results globally because `/api/v1/survey-results/**` is allowed for any teacher and result loading is unscoped (`SecurityConfig.java:41-44`, `SurveyResultPersistenceAdapter.java:33-84`) | In a university, a lecturer should not automatically see all survey results across departments or courses. | Major governance and privacy problem. This would not pass a real institutional review. |
| Weak assignment model | The schema supports generic evaluator/subject combinations and `teacher_id`, but the application only creates student assignments with `ALL` or `DEPARTMENT` targeting (`database/full_schema.sql:108-133`, `CreateSurveyService.java:56-84`, `GetSurveyService.java:68-82`) | The domain model advertises richer targeting than the product actually delivers. This looks half-built. | Evaluators and reviewers will see architectural ambition without product completion. |
| Weak validation/governance | Survey creation has almost no business validation beyond non-empty title and question presence in the frontend; backend create path does not validate against department existence, missing dates, empty question set, duplicate content, or conflicting windows (`CreateSurveyService.java:30-61`, `frontend/src/features/admin/pages/CreateSurveyPage.tsx:79-112`) | Real survey ops need validation rules or they create garbage data and operator pain. | Admins can create invalid or meaningless survey campaigns. |
| Missing operational visibility | Admin dashboard is mostly counts and top response volume (`frontend/src/features/dashboard/pages/AdminDashboardPage.tsx:35-149`) | A real operations dashboard should show overdue approvals, stuck onboarding, pending document reviews, response rate by cohort, surveys at risk, rejection reasons, and unresolved feedback backlog. | Dashboard is cosmetic. It does not help staff run the service. |
| No exception handling in business flows | Pending student approval is a raw approve/reject button with no reason, no reviewer notes, and only file path inspection (`frontend/src/features/admin/pages/PendingStudentsPage.tsx:77-177`, `AdminStudentApprovalService.java:58-84`) | Real onboarding always has exceptions: unreadable ID, mismatched name, expired document, duplicate student code, partial mismatch, escalation. | Staff cannot document why they acted. Students cannot recover constructively. |
| Low data completeness | Survey, feedback, and notification entities are minimal. `Notification` stores only `content`; survey has no category, owner scope, template, anonymous mode, response target, or academic term (`database/full_schema.sql:84-187`) | Thin tables usually mean thin product thinking. Important reporting and workflow dimensions are absent from the data model. | Future analytics, filtering, governance, and integrations become awkward or impossible. |
| Missing role boundaries | `CurrentStudentProvider.currentStudentId()` simply returns `currentUserId()` (`CurrentStudentProvider.java:23-25`), which is safe only because the caller pattern is disciplined. Teacher/admin-specific identity models are weak. | This is the kind of shortcut that survives in student work because the role matrix is still small. | Harder to grow into class-scoped teacher features or delegated operators safely. |
| No real notification/escalation loop | Notification tables exist, but active notification delivery is generated in memory from survey dates rather than persisted or event-driven (`database/full_schema.sql:173-187`, `GetStudentNotificationsService.java:30-97`) | That is not a notification system. It is a computed convenience view. No read state, no history, no reminders, no retry, no escalation, no delivery channel. | Students do not actually get prompted; admins cannot prove outreach happened. |
| Weak reporting/analytics | Survey results are basic aggregates and raw comments only (`SurveyResultPersistenceAdapter.java:87-137`) | Real survey programs need cohort slicing, trend comparison, response-rate denominators, benchmark periods, and action tracking. | Result pages look informative but are operationally shallow. |
| No audit/compliance thinking | Approvals, user activation, survey closure, and feedback responses do not create an audit trail in the data model or UI. Not evident in the repository. | For any institutional workflow, auditability matters. Who approved? When? Why? Based on what evidence? | This would fail a serious internal tool review. |
| Adoption and UX are still admin-centric and raw | The admin survey page uses raw department IDs, the user page edits raw department IDs, and pending student review exposes file paths instead of document previews (`CreateSurveyPage.tsx:322-330`, `PendingStudentsPage.tsx:148-155`, `frontend/src/features/admin/pages/UserDetailPage.tsx`) | Real products remove internal database trivia from UI. Operators should pick from controlled options, not memorize IDs. | The system looks like a thin CRUD shell over tables instead of a product built for humans. |
| Technical realism gaps | There is CI, but frontend has no test script at all (`frontend/package.json:6-10`), backend runtime images skip tests in Docker build (`backend/Dockerfile:17-24`), and there is no root deployment orchestration (`README.md:166-170`) | This is acceptable for coursework, not for a production-minded portfolio system. | Reliability claims are weak. Deployment story is incomplete. |
| Security semantics are muddy | The `User.verify` flag is used as both "email verified / account active / deactivated" state (`database/full_schema.sql:4-13`, `AuthUseCaseService.java:170-172`) | Real products separate email verification, account active state, and user suspension. Conflating them creates bad edge cases and bad admin semantics. | Users can be "inactive" for multiple unrelated reasons, and the system cannot explain which one. |
| Demo-seed quality is weak | Seed data inserts plaintext passwords directly into `User.pass_word` (`database/seed_data.sql:11-18`) even though application login expects encoded passwords (`AuthUseCaseService.java:166-168`) | That is demo data, not credible product seeding. | It weakens trust in the repo’s realism and can break actual local behavior depending on data path. |

Additional blunt points:
- The repo has many screens, but several are thin wrappers around a single endpoint. More screens did not create more product depth.
- Notifications are toy-like.
- Feedback is toy-like.
- Survey management is the strongest area, but even there it is still campaign CRUD, not survey operations.

## 5. Missing End-to-End Business Lifecycles

### 5.1 Student onboarding
- Core entity:
  - `Student`
- Current implemented flow:
  - register -> email token issued -> email verified -> upload 2 images -> admin approves/rejects -> student can log in (`AuthUseCaseService.java:90-320`, `AdminStudentApprovalService.java:48-84`)
- Where the flow stops too early:
  - No re-upload after rejection.
  - No reviewer notes.
  - No explicit document review result per document.
  - No expiry of pending submissions.
  - No student-facing status timeline.
  - No reminder or escalation if approval is delayed.
  - No evidence capture of reviewer, timestamp, or reason.
- Real closed-loop lifecycle:
  - Draft registration -> email verification pending -> profile completion pending -> documents submitted -> automated validation checks -> manual review in queue -> approved / rejected with reason / needs re-submission -> student notified -> student resubmits if needed -> activated -> onboarding archived with audit trail
- What should change:
  - Replace the single `status` field with a richer onboarding case model or at least separate fields:
    - `account_status`
    - `email_verified_at`
    - `document_submission_status`
    - `review_decision`
    - `review_reason`
    - `reviewed_by`
    - `reviewed_at`
    - `resubmission_count`
  - Add rework loop and reason codes.
  - Persist document metadata and verification outcome, not just file paths.

### 5.2 Survey campaign management
- Core entities:
  - `Survey`
  - `Question`
  - `Survey_Assignment`
- Current implemented flow:
  - admin creates survey -> adds questions -> chooses all students or one department -> can edit until responses exist -> can hide or close -> students can submit -> admins/teachers can view results (`CreateSurveyService.java:30-61`, `AdminSurveyManagementService.java:83-189`)
- Where the flow stops too early:
  - No draft state.
  - No review/approval before publication.
  - No scheduled publication action.
  - No response target or response-rate denominator.
  - No reminder cycle during open window.
  - No archive or versioning after closure.
  - No post-survey action plan.
- Real closed-loop lifecycle:
  - Draft -> internal review -> approved -> scheduled -> published -> participation tracked -> reminders sent -> closed -> results reviewed -> actions assigned -> action completion tracked -> archived -> comparable with previous runs
- What should change:
  - Add `survey_status` values beyond date-derived `OPEN/CLOSED/NOT_OPEN`.
  - Add publication metadata: `draft`, `scheduled`, `published`, `closed`, `archived`, `cancelled`.
  - Add ownership and governance metadata: `created_by`, `approved_by`, `approved_at`, `academic_term`, `survey_type`, `anonymity_mode`, `target_population_size`.
  - Add monitoring metrics: invite count, started count, completed count, completion rate, non-response rate.

### 5.3 Survey participation and response tracking
- Core entities:
  - `Survey_Response`
  - `Response_Detail`
- Current implemented flow:
  - active student opens assigned survey -> submits once -> results aggregate later (`SubmitSurveyService.java:35-105`)
- Where the flow stops too early:
  - No invite record.
  - No started-but-not-submitted state.
  - No partial save.
  - No response reminder logic.
  - No evidence whether survey is anonymous or identified.
  - No denominator for participation reporting.
- Real closed-loop lifecycle:
  - assignment/invitation -> seen -> started -> partially completed -> reminder -> submitted -> validated -> counted in reporting -> locked -> retained/archived
- What should change:
  - Introduce `SurveyParticipation` or `SurveyRecipient` records per target user/cohort.
  - Track `invited_at`, `first_opened_at`, `last_reminded_at`, `submitted_at`, `status`.
  - Support draft save for longer surveys if the product aims beyond trivial forms.

### 5.4 Survey results and actionability
- Core entities:
  - survey result views
- Current implemented flow:
  - admin/teacher sees response count, averages, breakdown, raw comments (`SurveyResultPersistenceAdapter.java:33-138`)
- Where the flow stops too early:
  - Results are descriptive only.
  - No segmentation by department, course, term, cohort.
  - No comparison against previous runs.
  - No threshold alerts.
  - No conversion from result to action item.
- Real closed-loop lifecycle:
  - results generated -> anomalies detected -> reviewer commentary added -> improvement actions assigned -> progress tracked -> closure reported back to stakeholders
- What should change:
  - Add comment tagging, topic clustering, threshold rules, trend history, and action plans tied to result items.

### 5.5 Student feedback / issue inbox
- Core entities:
  - `Feedback`
  - `Feedback_Response`
- Current implemented flow:
  - student submits title/content -> staff replies in one thread (`StudentFeedbackService.java:51-147`)
- Where the flow stops too early:
  - No category.
  - No priority.
  - No status.
  - No assignment owner.
  - No SLA.
  - No close/reopen.
  - No internal note vs external reply separation.
- Real closed-loop lifecycle:
  - feedback submitted -> classified -> triaged -> assigned -> responded -> resolved -> confirmed by student -> closed -> reported in service metrics
- What should change:
  - Convert feedback from a comment board into a lightweight case management workflow.

## 6. Missing Features Required for a Real Product

### 6.1 Core business features
- Survey campaign drafts and approvals
  - What it does: separates design from publication.
  - Why it matters: avoids accidental launch and supports governance.
  - Connection: extends current create/edit/close flow.
  - Phase: MVP
- Response tracking by target population
  - What it does: tracks who was invited and who responded.
  - Why it matters: response count without denominator is weak management data.
  - Connection: closes the gap between `Survey_Assignment` and `Survey_Response`.
  - Phase: MVP
- Student onboarding re-submission workflow
  - What it does: lets rejected students correct and resubmit documents with reasons.
  - Why it matters: binary reject is unrealistic and wasteful.
  - Connection: builds on current approval flow.
  - Phase: MVP
- Feedback ticket states and assignment
  - What it does: adds category, status, owner, and resolution.
  - Why it matters: current feedback thread is too informal for institutional use.
  - Connection: evolves current `Feedback` and `Feedback_Response`.
  - Phase: MVP
- Survey templates
  - What it does: saves reusable question sets and campaign presets.
  - Why it matters: real admins repeat surveys, they do not rebuild each one manually.
  - Connection: extends current survey create/edit UI.
  - Phase: Next-phase
- Academic term / campaign grouping
  - What it does: groups surveys and results by semester/year/program.
  - Why it matters: without this, analytics remain shallow.
  - Connection: applies to survey management and reporting.
  - Phase: Next-phase

### 6.2 Admin/governance features
- Approval reason codes and reviewer notes
  - What it does: stores why onboarding was approved/rejected.
  - Why it matters: creates accountability and rework guidance.
  - Connection: current pending student review is blind.
  - Phase: MVP
- Scoped teacher access
  - What it does: restricts teachers to departments/courses they own.
  - Why it matters: global result access is institutionally unsafe.
  - Connection: fixes current survey result access model.
  - Phase: MVP
- Survey ownership and delegated operators
  - What it does: defines who owns each survey and who may operate it.
  - Why it matters: governance requires ownership, not only role.
  - Connection: current survey is only `created_by`.
  - Phase: Next-phase
- Policy-based retention and archival
  - What it does: archives old onboarding files and survey data.
  - Why it matters: production systems need lifecycle after closure.
  - Connection: current repo has none.
  - Phase: Advanced-phase

### 6.3 User experience features
- Department selector and master data dropdowns
  - What it does: removes raw department ID entry from admin UI.
  - Why it matters: current UI leaks database internals (`CreateSurveyPage.tsx:322-330`).
  - Connection: improves survey/user admin flows.
  - Phase: MVP
- Document preview and review UI
  - What it does: shows actual images with zoom, verification checklist, and decision panel.
  - Why it matters: current file-path review is not credible (`PendingStudentsPage.tsx:148-155`).
  - Connection: student approval flow.
  - Phase: MVP
- Student status timeline
  - What it does: shows where the student is in onboarding and what remains.
  - Why it matters: reduces confusion and support load.
  - Connection: current onboarding is multi-step but poorly surfaced.
  - Phase: MVP
- Actionable dashboards
  - What it does: turns dashboards into work queues and alerts, not passive totals.
  - Why it matters: current dashboards are cosmetic (`AdminDashboardPage.tsx:35-149`).
  - Connection: admin/lecturer surfaces.
  - Phase: Next-phase

### 6.4 Reporting/analytics features
- Response rate analytics
  - What it does: shows completion rate by survey, department, cohort, and time.
  - Why it matters: response count alone is weak.
  - Connection: requires target population tracking.
  - Phase: MVP
- Trend comparison across runs
  - What it does: compares this semester vs previous semester.
  - Why it matters: results become meaningful only in trend context.
  - Connection: survey result pages.
  - Phase: Next-phase
- Comment analysis workflow
  - What it does: tag, classify, and summarize text responses.
  - Why it matters: raw comment dumps do not scale.
  - Connection: result details and feedback inbox.
  - Phase: Advanced-phase
- Operational service metrics
  - What it does: pending approvals by age, average review time, feedback resolution time.
  - Why it matters: proves the product is helping run a service.
  - Connection: onboarding and feedback workflows.
  - Phase: MVP

### 6.5 Notification/reminder/escalation features
- Persistent in-app notifications
  - What it does: stores notification events, read state, and action links.
  - Why it matters: current notification logic is computed on demand, not delivered (`GetStudentNotificationsService.java:30-97`).
  - Connection: reuse existing notification tables properly.
  - Phase: MVP
- Email reminders for pending surveys
  - What it does: nudges students before deadline.
  - Why it matters: improves response rate and realism.
  - Connection: extends current Resend integration.
  - Phase: MVP
- Admin escalation for stale onboarding cases
  - What it does: flags approvals waiting too long.
  - Why it matters: prevents silent backlog.
  - Connection: onboarding queue.
  - Phase: Next-phase
- Resolution notifications for feedback tickets
  - What it does: closes the loop with students when staff acts.
  - Why it matters: current feedback model has no closure.
  - Connection: feedback case management.
  - Phase: Next-phase

### 6.6 Audit/security/compliance features
- Audit log for privileged actions
  - What it does: records who approved, rejected, deactivated, edited, hid, or closed.
  - Why it matters: essential for institutional workflows.
  - Connection: admin endpoints.
  - Phase: MVP
- Separate account state fields
  - What it does: splits email verification, active state, and suspension.
  - Why it matters: current `verify` boolean is overloaded and misleading.
  - Connection: auth and user management.
  - Phase: MVP
- File handling hardening
  - What it does: size limits, MIME checks, virus-scan hook, signed URLs, non-public storage.
  - Why it matters: student ID documents are sensitive.
  - Connection: current local file copy is weak (`LocalStudentDocumentStorageAdapter.java`).
  - Phase: MVP
- Rate limiting and abuse controls
  - What it does: protects auth, registration, reset-password.
  - Why it matters: current repo shows no such controls. Not evident in the repository.
  - Connection: public endpoints.
  - Phase: Next-phase

### 6.7 Data quality and master data features
- Master data management for departments, terms, programs, courses
  - What it does: creates reliable selectors and reporting dimensions.
  - Why it matters: hardcoded names and IDs do not scale.
  - Connection: registration, user management, survey targeting.
  - Phase: MVP
- Seed strategy with realistic hashed accounts and demo scenarios
  - What it does: gives evaluators usable demo data and credible credentials.
  - Why it matters: current seed data uses plaintext passwords (`database/seed_data.sql:11-18`).
  - Connection: local setup and portfolio demo quality.
  - Phase: MVP

## 7. Third-Party Integrations That Would Make the Product More Real

| Integration | Problem Solved | Why It Improves Realism | Recommended Direction | Essential / Optional |
|---|---|---|---|---|
| University SSO / Microsoft / Google Workspace | Avoids separate local credentials for staff and students | Makes the product look like institutional software, not a classroom island | Add OIDC for staff first, then student identity if university identity source exists | Essential |
| Email delivery service | Already partly used for verification | Extend from one-off verification to reminder, approval, rejection, and feedback closure flows | Reuse Resend for short term; abstract notification channel service | Essential |
| Object storage | Secure document storage and scalable result exports | Local `uploads/student-docs` is not production-grade | Move student docs and exports to S3-compatible storage with signed access URLs | Essential |
| Queue / background job runner | Send reminders and notifications asynchronously | Real systems do not block business transactions on outbound delivery | Start with Spring scheduled jobs; evolve to Redis/RabbitMQ-backed workers when needed | Essential |
| Monitoring/logging | Operational support and incident analysis | Makes the repo look run-able, not just build-able | Add structured logs, health checks, metrics, and error monitoring | Essential |
| BI/report export | Stakeholders often need CSV/Excel/PDF summaries | A serious survey platform supports external reporting workflows | Add CSV export first; optional Power BI/Tableau connector later | Optional |
| Search | Finding feedback tickets, users, surveys at scale | Improves operator workflow realism | Start with SQL filtering; only add search engine if dataset justifies it | Optional |
| Captcha / anti-abuse | Protects registration and password reset | Public endpoints in production need abuse controls | Add rate limiting first, captcha only where needed | Optional |
| Calendar integration | Publish survey windows and deadlines to university calendars | Useful but not essential for first realistic milestone | Generate iCal links or Outlook/Google calendar handoff | Optional |

Integration notes:
- Do not add Kafka just to look modern. The repo does not need it yet.
- The highest-value integrations here are identity, object storage, monitoring, and job execution.

## 8. Teacher / Evaluator Perspective: What Makes a “Real Product”
- From a demanding lecturer’s perspective, this repo currently feels academically complete but product-incomplete because it demonstrates breadth of implementation without equivalent depth of use.
- Why it still feels like a student submission:
  - Many flows stop at the first success state.
  - Lifecycle handling is binary instead of operational.
  - Admin screens expose database concepts like raw department IDs and file paths.
  - There is no convincing evidence of auditability, service metrics, or governance constraints.
  - Dashboards mostly summarize data rather than help someone do their job.
- Criteria that distinguish a serious product from a student submission:
  - The system handles failure, rework, delay, and exception states.
  - Roles are scoped by ownership, not only by broad labels.
  - Actions leave traceable audit evidence.
  - Reports support decisions, not just screenshots.
  - Data model supports business reality, not only demo UI.
  - Deployment and runtime story show how the product would actually operate.
- Repo evidence supporting that distinction:
  - Binary onboarding approval with no reasons (`AdminStudentApprovalService.java:48-84`).
  - Teacher result access is effectively global (`SecurityConfig.java:41-44`, `SurveyResultPersistenceAdapter.java:33-84`).
  - Notification tables exist, but active notifications are computed on the fly rather than managed as events (`database/full_schema.sql:173-187`, `GetStudentNotificationsService.java:30-97`).
  - The frontend has no automated tests at all (`frontend/package.json:6-10`).
  - Dockerfiles exist, but the repo itself admits there is no composed environment (`README.md:166-170`).
- Additions that would most increase evaluation quality:
  - A reworkable onboarding case flow with notes, reason codes, document preview, and audit trail.
  - Scoped lecturer access and target-population-based response analytics.
  - Actionable dashboards showing backlog, SLA, and response risk, not only counts.
  - A production-minded deployment story with object storage, background reminders, and environment separation.

## 9. Recommended Product Evolution Roadmap

### Phase 1: Fix product foundation
- Goals:
  - Remove obvious demo-grade weaknesses.
  - Make current flows operable by real users.
- Features:
  - Approval/rejection reason codes and reviewer notes.
  - Department selectors instead of raw IDs.
  - Document preview and metadata review.
  - Separate account active state from email verification.
  - Hashed, credible seed/demo users.
- Technical changes:
  - Extend schema for review fields and active-state semantics.
  - Add master-data endpoints for departments.
  - Harden file validation and storage abstraction.
- Why this phase comes first:
  - The current product breaks realism in basic daily operations. Fixing that is higher value than adding new features.

### Phase 2: Complete business closure
- Goals:
  - Turn fragmented flows into full lifecycles.
- Features:
  - Onboarding resubmission workflow.
  - Survey draft/review/publish/archive lifecycle.
  - Recipient tracking and response-rate denominator.
  - Feedback ticket statuses, ownership, and resolution.
  - Persistent notifications and reminder jobs.
- Technical changes:
  - Add participation/invitation tables.
  - Add richer state models and status transition rules.
  - Add scheduled/background jobs.
- Why this phase comes before the next one:
  - Without lifecycle closure, integrations and advanced reporting only decorate a shallow product.

### Phase 3: Add realism with integration and governance
- Goals:
  - Make the system behave like institutional software.
- Features:
  - Lecturer scoping by department/course ownership.
  - Email reminders and approval outcome notifications.
  - SSO for staff.
  - Object storage for documents and exports.
  - CSV exports and trend reports.
- Technical changes:
  - Identity integration.
  - Storage integration.
  - Authorization policy layer beyond static roles.
- Why this phase comes before the next one:
  - Governance and integration make the product believable before deep engineering polish.

### Phase 4: Move toward production-quality engineering
- Goals:
  - Improve maintainability, reliability, and operational confidence.
- Features:
  - Observability.
  - Structured audit log.
  - Better test pyramid.
  - Environment orchestration and deployment automation.
- Technical changes:
  - Health checks, metrics, tracing/logging.
  - Integration tests and frontend tests.
  - Docker Compose or equivalent deployment manifests.
  - Background worker separation if job load grows.
- Why this phase comes after the others:
  - Engineering polish is most useful after the product model is no longer shallow.

## 10. Technology Direction for a Better-Looking Project

| Recommendation | Current State | Better Direction | Useful Or Resume Decoration? |
|---|---|---|---|
| Architecture maturity | Hexagonal structure is present (`README.md:172-180`) | Keep it. Improve domain state modeling instead of rewriting architecture. | Truly useful |
| Modularization | Domain modules exist but still small and generic | Split onboarding, survey operations, feedback case management, and reporting more clearly as product depth grows | Useful |
| Async/background jobs | Not evident in the repository | Add scheduled reminders, notification dispatch, cleanup, token expiry handling | Truly useful |
| Caching | Not evident in the repository | Only add for reference/master data and reporting-heavy views if needed | Useful later, not urgent |
| Message queue/event-driven patterns | Not evident in the repository | Use only if notifications and jobs outgrow simple scheduler + DB-backed outbox | Could become useful; avoid early buzzwording |
| Search/reporting | Basic SQL-backed lists and aggregates | Add server-side filters, exports, trend reporting before adopting Elasticsearch | Useful if done incrementally |
| Observability | Not evident beyond logs | Add health endpoints, metrics, structured logs, error monitoring | Truly useful |
| Security hardening | JWT and role guards exist | Add rate limiting, file scanning hooks, signed file access, separated account-state semantics, audit logging | Truly useful |
| Deployment maturity | Dockerfiles exist; no composed stack (`README.md:166-170`) | Add local compose stack with backend, frontend, SQL Server/dev alternative, object storage stub, mail sandbox | Truly useful |
| CI/CD maturity | Backend tests and frontend lint/build run (`ci.yml:38-52`) | Add integration test stage, frontend test stage, image scan, env-specific deploy gates | Useful |
| Environment strategy | Manual env export in README (`README.md:72-95`) | Add `.env` loading strategy, sample deploy manifests, clearer env separation | Useful |
| API quality/versioning | Mixed `/api/auth`, `/api/admin`, `/api/v1/*` patterns | Standardize versioning and resource conventions; add filtering/pagination endpoints | Truly useful |
| Test strategy | Some backend tests; frontend no tests (`frontend/package.json:6-10`) | Add service integration tests, controller contract tests, frontend interaction tests for core flows | Truly useful |
| Seed/demo data strategy | Basic seed exists but low realism (`database/seed_data.sql:11-18`) | Build demo scenarios with valid hashed credentials, multiple departments, open/closed/stalled cases | Truly useful |
| File handling | Local filesystem storage (`LocalStudentDocumentStorageAdapter.java`) | Move to object storage abstraction with secure access and metadata | Truly useful |
| Containerization/runtime separation | Frontend and backend images only | Add composed runtime and separate job runner if reminder/audit workload appears | Useful |

Specific guidance:
- Do not replace Spring Boot or React just to make the stack look more advanced.
- The stack already looks acceptable. The problem is not the stack. The problem is domain closure and operational realism.
- The most valuable technical additions are:
  - background job execution
  - object storage
  - observability
  - authorization scoping
  - stronger test coverage

## 11. Most Valuable High-Impact Improvements

| Rank | Title | Problem Solved | Expected Impact | Implementation Difficulty | Priority |
|---|---|---|---|---|---|
| 1 | Reworkable onboarding case workflow | Binary approve/reject is unrealistic | Transforms onboarding from demo to service workflow | Medium | Critical |
| 2 | Recipient tracking and response-rate analytics | Current survey stats lack denominator | Makes survey operations meaningful and management-grade | High | Critical |
| 3 | Scoped teacher authorization | Teachers can currently view too broadly | Fixes major governance/privacy weakness | Medium | Critical |
| 4 | Survey lifecycle states beyond date-only status | Current campaign flow is CRUD-like | Makes survey operations believable | Medium | High |
| 5 | Feedback ticket triage and resolution workflow | Current feedback is toy-like | Converts feedback into a real support/process channel | Medium | High |
| 6 | Persistent notifications plus reminder jobs | Current notifications are computed, not delivered | Adds real user engagement loop | Medium | High |
| 7 | Audit log for admin actions | No accountability for privileged changes | Strong institutional realism and safety | Medium | High |
| 8 | Object storage and secure document handling | Local file-path storage is weak | Greatly improves production credibility | Medium | High |
| 9 | Actionable admin dashboard | Current dashboard is cosmetic | Improves operator value immediately | Low-Medium | Medium |
| 10 | Frontend and integration test coverage | Engineering proof is incomplete | Improves recruiter and reviewer confidence | Medium | Medium |

## 12. Concrete Next Step Recommendation
- If only 3 things can be improved next, they should be:
  - Build a real onboarding review workflow with rejection reasons, reviewer notes, document preview, and resubmission.
  - Add survey recipient tracking so results show response rates by cohort, not just raw counts.
  - Restrict lecturer result access to owned scope and add audit logging for privileged actions.
- If only 1 month is available, build:
  - onboarding rework loop
  - survey participation tracking with reminders
  - actionable admin dashboard for pending approvals and low-response surveys
- If the goal is to impress a lecturer, prioritize:
  - full lifecycle closure
  - auditability
  - role scoping
  - stronger reporting with denominator-based metrics
- If the goal is to impress recruiters, prioritize:
  - the same product-closure work first
  - then show production-minded execution with object storage, background jobs, observability, and a believable local deployment stack
