# Implementation Expansion Plan

## 1. What Should Actually Be Built Next
- Blunt recommendation:
  - Build the next version of this repository around three concrete upgrades:
    - a real onboarding review workflow
    - a real survey lifecycle with participation tracking
    - a real governance/reporting layer for admin and lecturer users
- What that means in practice:
  - Replace binary onboarding approval with rejection reasons, reviewer notes, document review, and resubmission.
  - Replace survey CRUD-plus-submit with draft/publish/close/archive plus recipient tracking, reminders, and response-rate reporting.
  - Replace role-only access and cosmetic dashboards with scoped authorization, actionable admin screens, and operational metrics.
- Why these additions matter more than random expansion:
  - They directly strengthen the weakest parts already visible in the repo:
    - onboarding is currently just `EMAIL_UNVERIFIED -> EMAIL_VERIFIED -> PENDING -> ACTIVE/REJECTED`
    - surveys have only minimal lifecycle control
    - results lack denominator-based reporting
    - notifications are computed on page load rather than delivered operationally
  - These additions also justify broader technology in a credible way:
    - object storage for student documents
    - scheduled/background jobs for reminders and stale-case scans
    - audit logging for admin actions
    - reporting read models for real dashboards
  - This is better than adding new unrelated modules because it deepens the product you already have instead of diluting it.

## 2. Proposed Expansion Scope

### Essential additions
- Onboarding review workflow
  - Problem solved:
    - Current approve/reject flow is too shallow and has no correction loop.
  - Why now:
    - This is the weakest real-world flow in the repo and one of the easiest places to raise product realism fast.
  - Expected impact:
    - High product realism
    - stronger admin/operator workflow
    - better capstone credibility
- Survey lifecycle states
  - Problem solved:
    - Current survey process is close to CRUD with manual hide/close.
  - Why now:
    - Surveys are the repo’s strongest module, so this is the best place to deepen the product.
  - Expected impact:
    - High user value
    - stronger governance
    - better admin workflow
- Recipient tracking and response-rate reporting
  - Problem solved:
    - Current analytics know only submitted responses, not target population or non-response.
  - Why now:
    - This is the single most important reporting gap.
  - Expected impact:
    - Critical reporting improvement
    - stronger dashboards
    - better lecturer/recruiter impression
- Lecturer access scoping
  - Problem solved:
    - Current lecturer result access is too broad for a real institution.
  - Why now:
    - Governance realism is currently weak and obvious.
  - Expected impact:
    - High realism
    - better security posture
    - stronger architectural credibility
- Audit logging for privileged actions
  - Problem solved:
    - No evident audit trail for approvals, deactivation, survey closure, or visibility changes.
  - Why now:
    - Once workflows deepen, traceability becomes mandatory.
  - Expected impact:
    - High governance value
    - better internal-tool realism

### High-value additions
- Persistent notifications plus reminder workflow
  - Problem solved:
    - Current notifications are calculated on read and do not behave like a real notification system.
  - Why now:
    - Needed after recipient tracking exists.
  - Expected impact:
    - Better participation rates
    - better product closure
- Document storage hardening
  - Problem solved:
    - Current local file storage is weak for sensitive identity documents.
  - Why now:
    - Once onboarding review becomes more serious, file handling should become more credible too.
  - Expected impact:
    - Better production realism
    - stronger technical breadth
- Feedback ticket lifecycle
  - Problem solved:
    - Current feedback is too much like a comment thread.
  - Why now:
    - Good second-wave workflow after onboarding and survey lifecycles are improved.
  - Expected impact:
    - Better student-service realism
    - stronger admin experience
- Operational dashboards
  - Problem solved:
    - Current dashboards show counts but not work queues, risk, or backlog age.
  - Why now:
    - Reporting and workflow changes will create meaningful operational metrics to display.
  - Expected impact:
    - Better demo value
    - more realistic product feel

### Optional advanced additions
- Survey templates and cloning
  - Problem solved:
    - Rebuilding similar surveys is repetitive.
  - Why now:
    - Useful once survey governance is stable.
  - Expected impact:
    - Medium product value
- Trend reporting by term/campaign
  - Problem solved:
    - Current results lack historical context.
  - Why now:
    - Best added after participation tracking and term metadata exist.
  - Expected impact:
    - High presentation value
    - medium implementation complexity
- Export/report pipeline
  - Problem solved:
    - Result pages are currently screen-bound and not management-friendly.
  - Why now:
    - Useful after reporting becomes richer.
  - Expected impact:
    - Good portfolio value
    - medium realism boost

## 3. Backend Changes

### A. Onboarding review workflow
- Domain model changes:
  - Extend the current student onboarding model beyond plain `Status`.
  - Keep the existing student status if needed for compatibility, but add review-specific fields:
    - `reviewStatus`
    - `reviewReason`
    - `reviewNotes`
    - `reviewedBy`
    - `reviewedAt`
    - `resubmissionCount`
  - Stronger option:
    - add a separate onboarding case entity instead of overloading `Student`.
- Database changes:
  - Add columns or a new `Student_Onboarding_Case` table.
  - Add fields for review metadata and correction loop.
  - Add document metadata fields if not using raw path-only storage.
- Service/use case changes:
  - Extend `AuthUseCaseService` upload flow so rejected students can resubmit.
  - Extend `AdminStudentApprovalService` with:
    - approve with notes
    - reject with reason code
    - request resubmission
  - Add a query use case for onboarding case detail/history.
- API changes:
  - Replace current simple approve/reject endpoints with request bodies.
  - Add endpoint to fetch onboarding case detail.
  - Add endpoint to resubmit documents after rejection.
- Background processing changes if needed:
  - Add scheduled check for stale pending reviews later, not necessarily in first commit.
- Security/authorization changes if needed:
  - Keep admin-only action for decisions.
  - Ensure students can view only their own onboarding state and resubmit only when allowed.

### B. Survey lifecycle states
- Domain model changes:
  - Add explicit survey workflow state rather than relying only on date-derived status.
  - Current repo has `SurveyStatus` as `OPEN`, `CLOSED`, `NOT_OPEN`; this is insufficient for product governance.
  - Add lifecycle states such as:
    - `DRAFT`
    - `READY_FOR_REVIEW`
    - `PUBLISHED`
    - `CLOSED`
    - `ARCHIVED`
    - `CANCELLED`
- Database changes:
  - Add lifecycle state column to `Survey`.
  - Add optional governance fields:
    - `approved_by`
    - `approved_at`
    - `archived_at`
    - `survey_type`
    - `term_code`
- Service/use case changes:
  - Refactor `CreateSurveyService` and `AdminSurveyManagementService` to enforce transitions rather than simple edits.
  - Add use cases:
    - submit survey draft
    - publish survey
    - archive survey
    - cancel survey
- API changes:
  - Add lifecycle action endpoints.
  - Change admin survey detail response to include lifecycle metadata.
- Background processing changes if needed:
  - Scheduled publication can be optional later.
- Security/authorization changes if needed:
  - Publishing and archiving should be admin-only or owner+admin depending on scope model.

### C. Recipient tracking and response-rate reporting
- Domain model changes:
  - Add participation/invitation entity, for example:
    - `SurveyRecipient`
    - fields: `surveyId`, `userId` or `studentId`, `status`, `invitedAt`, `firstOpenedAt`, `submittedAt`, `lastRemindedAt`
  - Keep `Survey_Response` as final submission record.
- Database changes:
  - Create `Survey_Recipient` or equivalent table.
  - Add indexes for survey+student and status queries.
  - Consider reporting summary table later for dashboard/report performance.
- Service/use case changes:
  - Extend survey publication flow to create recipient records from assignment rules.
  - Extend survey detail fetch to mark first open.
  - Extend submit flow to update recipient state to submitted.
  - Add reporting use cases for:
    - invite count
    - open count
    - submit count
    - response rate
- API changes:
  - Add participation stats to admin survey endpoints and survey result endpoints.
  - Add filters for cohort/department if supported.
- Background processing changes if needed:
  - Scheduled reminder generation will depend on this data.
- Security/authorization changes if needed:
  - Ensure only scoped roles see cohort metrics relevant to them.

### D. Lecturer access scoping
- Domain model changes:
  - Introduce ownership/scope rules:
    - by department
    - by survey owner
    - possibly by course later if added
- Database changes:
  - Minimal version can derive scope from lecturer department and survey assignment.
  - Stronger version can add an explicit `Survey_View_Permission` or owner mapping table.
- Service/use case changes:
  - Refactor survey result query path to filter results by the authenticated user’s scope.
  - Do not rely on `hasRole("LECTURER")` alone.
- API changes:
  - No major contract change required, but returned data should be filtered.
- Background processing changes if needed:
  - None.
- Security/authorization changes if needed:
  - Add a policy/service layer used by controllers or use cases to enforce data scope.

### E. Audit logging
- Domain model changes:
  - Add an `AuditLog` entity:
    - actorUserId
    - actionType
    - targetType
    - targetId
    - payloadSummary
    - createdAt
- Database changes:
  - New `Audit_Log` table with queryable indexes.
- Service/use case changes:
  - Write audit entries on:
    - onboarding review decision
    - user activate/deactivate
    - survey publish/close/hide/archive
    - feedback ticket resolution later
- API changes:
  - Optional read-only audit view endpoint for admin later.
- Background processing changes if needed:
  - None initially.
- Security/authorization changes if needed:
  - Restrict audit viewing to admin only.

### F. Persistent notifications and reminders
- Domain model changes:
  - Either upgrade current notification tables into a real notification model or add a cleaner notification event model with:
    - type
    - title
    - message
    - userId
    - entityType/entityId
    - deliveredAt
    - readAt
- Database changes:
  - Add columns to existing `Notification` and `Notification_User` tables or introduce new event tables.
- Service/use case changes:
  - Replace `GetStudentNotificationsService` “compute on read” logic with persisted events for reminder-worthy actions.
  - Add mark-as-read use case.
- API changes:
  - Add read/unread state endpoints.
  - Keep list endpoint but serve persisted records.
- Background processing changes if needed:
  - Scheduled reminder jobs for:
    - survey not yet opened
    - survey opened but not submitted
    - stale onboarding cases
- Security/authorization changes if needed:
  - Standard user ownership checks.

### G. Document storage hardening
- Domain model changes:
  - Store object key, content type, uploadedAt, maybe checksum, not just raw relative path.
- Database changes:
  - Add metadata columns or separate `Student_Document` table.
- Service/use case changes:
  - Replace `LocalStudentDocumentStorageAdapter` with storage abstraction backed by object storage.
  - Validate MIME type, size, and allowed extensions before save.
- API changes:
  - Add preview/download endpoints or signed URL generation path.
- Background processing changes if needed:
  - Optional scan/cleanup jobs later.
- Security/authorization changes if needed:
  - Protect access so only admins reviewing onboarding and the owning student can access files.

## 4. Frontend Changes

### A. Onboarding review workflow
- Pages/screens to add or change:
  - Update `PendingStudentsPage`
  - Add admin onboarding detail/review screen
  - Add student onboarding status page
  - Update document upload page for resubmission path
- UI workflow changes:
  - Admin reviews actual documents and uses reasoned actions:
    - approve
    - reject with reason
    - request correction
  - Student sees reason and can resubmit.
- Validation / UX changes:
  - Require reject reason.
  - Show current onboarding state clearly.
  - Prevent upload when state disallows it.
- Admin/operator usability improvements:
  - Remove raw file-path-only review.
  - Add preview, decision notes, and status history.

### B. Survey lifecycle and participation tracking
- Pages/screens to add or change:
  - Update `CreateSurveyPage`
  - Update `AdminSurveysPage`
  - Add publish/archive actions
  - Update survey detail and result pages with participation metrics
- UI workflow changes:
  - Admin works through draft -> publish instead of immediate launch.
  - Admin sees invite count, open count, submission count, response rate.
- Validation / UX changes:
  - Enforce required publish conditions before activation.
  - Show lifecycle badges and locked/unlocked edit rules.
- Admin/operator usability improvements:
  - Replace raw department ID input with selector.
  - Show participation funnel, not only response count.

### C. Lecturer access scoping
- Pages/screens to add or change:
  - Update lecturer dashboard
  - Update survey result list/detail to show only scoped data
- UI workflow changes:
  - Lecturer sees only relevant surveys/results.
- Validation / UX changes:
  - Better empty-state messaging when lecturer has no scoped surveys.
- Admin/operator usability improvements:
  - Improve trust in data boundaries.

### D. Notifications and reminders
- Pages/screens to add or change:
  - Update notifications page
  - Optional reminder badges in dashboard or header
- UI workflow changes:
  - Show read/unread state
  - Show action-driven notifications for survey deadlines and onboarding decisions
- Validation / UX changes:
  - Avoid duplicate notification clutter
- Admin/operator usability improvements:
  - Students get clear prompts instead of needing to poll the survey list manually.

### E. Operational dashboards
- Pages/screens to add or change:
  - Update `AdminDashboardPage`
  - Update `LecturerDashboardPage`
  - Optionally expand student dashboard with assigned/pending/completed participation states
- UI workflow changes:
  - Dashboard becomes a workboard:
    - pending approvals by age
    - low-response active surveys
    - unresolved feedback tickets
- Validation / UX changes:
  - Add filters and sorting where data volume grows.
- Admin/operator usability improvements:
  - Make dashboard directly actionable, not decorative.

### F. Feedback ticket lifecycle
- Pages/screens to add or change:
  - Update `FeedbackPage`
  - Update `ManageFeedbackPage`
  - Add ticket state, category, assignee, and resolution controls
- UI workflow changes:
  - Staff classifies and resolves tickets instead of only replying.
  - Students see whether issue is open, in progress, resolved, or closed.
- Validation / UX changes:
  - Require resolution note when closing if appropriate.
- Admin/operator usability improvements:
  - Better filtering and queue management.

## 5. Infrastructure / DevOps / Runtime Changes

### A. Object storage
- What infrastructure/runtime change is needed:
  - Add object storage configuration and adapter.
- Whether it is local-dev only, deployment only, or both:
  - Both
- How difficult it is:
  - Medium
- Whether it is essential or optional:
  - High-value, close to essential once onboarding review gets serious

### B. Background jobs / scheduler
- What infrastructure/runtime change is needed:
  - Add scheduled job execution in backend runtime.
  - Optional separate worker process later if job load grows.
- Whether it is local-dev only, deployment only, or both:
  - Both
- How difficult it is:
  - Medium
- Whether it is essential or optional:
  - Essential for reminders and stale-case monitoring

### C. Structured logging and metrics
- What infrastructure/runtime change is needed:
  - Add structured logs and health/metrics endpoints.
- Whether it is local-dev only, deployment only, or both:
  - Both
- How difficult it is:
  - Low-Medium
- Whether it is essential or optional:
  - High-value, should be added early

### D. Deployment orchestration
- What infrastructure/runtime change is needed:
  - Add root-level local orchestration for backend, frontend, and database; optional object storage service for local demo.
- Whether it is local-dev only, deployment only, or both:
  - Both, but most useful for local demo and review setup
- How difficult it is:
  - Medium
- Whether it is essential or optional:
  - Optional but strongly recommended

### E. Email workflow expansion
- What infrastructure/runtime change is needed:
  - Reuse current Resend integration with templated lifecycle emails.
- Whether it is local-dev only, deployment only, or both:
  - Both
- How difficult it is:
  - Low-Medium
- Whether it is essential or optional:
  - High-value after recipient tracking exists

### F. Reporting read models
- What infrastructure/runtime change is needed:
  - Add summary tables or SQL views updated by application flows or jobs.
- Whether it is local-dev only, deployment only, or both:
  - Both
- How difficult it is:
  - Medium
- Whether it is essential or optional:
  - Essential once participation reporting is introduced

## 6. Testing Impact
- What backend tests should be added:
  - Onboarding workflow state transition tests
  - Survey lifecycle transition tests
  - Participation tracking tests
  - Authorization scope tests for lecturer/admin access
  - Audit log emission tests
  - Notification/reminder generation tests
- What frontend tests should be added:
  - Student upload/resubmission flow
  - Admin onboarding decision flow
  - Survey draft -> publish -> view metrics flow
  - Lecturer result visibility flow
  - Notification read/unread interaction flow
- What integration tests should be added:
  - End-to-end onboarding case path
  - Survey publication creating recipient tracking records
  - Survey submission updating participation metrics
  - Reminder job writing notifications and sending email
  - Lecturer scoped result access
- Which additions most improve confidence and demo quality:
  - Onboarding workflow tests
  - Survey lifecycle + participation tests
  - Lecturer scoping tests
  - Frontend tests for admin review and survey publish flow

## 7. Suggested Delivery Order

### Stage 1
- What to build:
  - onboarding review workflow
  - rejection reasons / reviewer notes
  - lecturer access scoping
  - audit logging baseline
- Why that order makes sense:
  - It fixes obvious realism and governance problems first without requiring major infrastructure.
- What it unlocks for later stages:
  - Better admin workflows
  - safer data access
  - traceable lifecycle actions

### Stage 2
- What to build:
  - survey lifecycle states
  - department selector/master-data support
  - survey publish/close/archive controls
  - frontend/admin UX improvements
- Why that order makes sense:
  - It deepens the repo’s strongest module before adding async/reporting complexity.
- What it unlocks for later stages:
  - Clean point to generate recipients and reminders
  - more meaningful survey governance

### Stage 3
- What to build:
  - recipient tracking
  - response-rate metrics
  - reporting read models
  - actionable dashboards
- Why that order makes sense:
  - Reporting quality only improves after participation data exists.
- What it unlocks for later stages:
  - reminder targeting
  - trend reporting
  - management reporting

### Stage 4
- What to build:
  - persistent notifications
  - reminder jobs
  - object storage migration
  - feedback ticket lifecycle
  - export/report pipeline if time remains
- Why that order makes sense:
  - These are valuable once core workflows and reporting are already strong.
- What it unlocks for later stages:
  - more realistic operations
  - stronger deployment story
  - stronger capstone demo narrative

## 8. Demo Value
- Lecturer impression:
  - The project will look less like a thin CRUD prototype and more like a governed institutional system.
  - Reviewer-visible wins:
    - richer lifecycle states
    - correction loops
    - scoped access
    - actionable reporting
- Recruiter impression:
  - The repo will show not only Java/React CRUD, but also:
    - stateful workflow design
    - storage abstraction
    - background jobs
    - policy-based authorization
    - auditability
    - testing maturity
- Realism of the product:
  - Students get a clearer status journey.
  - Admins get actual queues and decision tools.
  - Lecturers get governed access and useful reporting.
  - Notifications and reminders stop being cosmetic.
- Quality of project presentation/demo:
  - The demo story becomes much stronger:
    - "student uploads invalid docs, admin rejects with reason, student corrects and resubmits"
    - "admin drafts and publishes survey to department"
    - "system tracks participation and sends reminders"
    - "lecturer sees scoped result dashboard with response rate"

## 9. Final Recommended Build Set
- Top 5 items to implement immediately:
  - Onboarding review workflow with rejection reasons and resubmission
  - Lecturer access scoping
  - Audit logging for privileged actions
  - Survey lifecycle states and publish/archive controls
  - Department selector/master-data support in admin flows
- Top 5 items after that:
  - Recipient tracking and participation states
  - Response-rate reporting and operational dashboards
  - Persistent notifications and reminder jobs
  - Object storage for student documents
  - Feedback ticket lifecycle
- 3 things NOT worth building yet because they are overengineering:
  - Microservices
  - Kafka or large-scale broker infrastructure
  - Kubernetes
