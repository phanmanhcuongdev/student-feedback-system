# Database Setup Notes

## Fresh setup
- Use `full_schema.sql` to create a new database from scratch.
- Use `seed_data.sql` after that if you want demo accounts and sample data.

## Incremental update path
- For an existing database that already contains application data, apply scripts in `database/migrations/` instead of re-running `full_schema.sql`.
- Current onboarding slice migration:
  - `migrations/2026-04-14-onboarding-review-workflow.sql`
- Current survey lifecycle slice migration:
  - `migrations/2026-04-14-survey-lifecycle-states.sql`
- Current survey recipient tracking slice migration:
  - `migrations/2026-04-14-survey-recipient-tracking.sql`
- Current audit logging slice migration:
  - `migrations/2026-04-14-audit-log-privileged-actions.sql`

## Demo credentials
- Admin: `admin@university.edu` / `admin123`
- Teacher: `teacher@university.edu` / `teacher123`
- Students: any seeded `student.*@university.edu` account / `student123`

These passwords are BCrypt-hashed in `seed_data.sql` and are compatible with the runtime `BCryptPasswordEncoder`.

## Current survey lifecycle model
- `Survey.lifecycle_state` stores explicit business lifecycle, separate from the derived runtime open/closed window.
- Allowed values in this phase:
  - `DRAFT`
  - `PUBLISHED`
  - `CLOSED`
  - `ARCHIVED`
- Existing survey rows migrated from older databases are initialized as:
  - `PUBLISHED` if the survey had not ended yet
  - `CLOSED` if `end_date < GETDATE()`
- Migration limitation:
  - old databases had no explicit draft/archive history, so the migration cannot infer whether a not-yet-ended survey was once intended to be draft-only or already operational
  - for that reason, old active/upcoming surveys are treated as `PUBLISHED` to preserve prior user-visible behavior
- Runtime survey availability is still derived from dates, but only for `PUBLISHED` surveys:
  - `PUBLISHED` + future start date => runtime `NOT_OPEN`
  - `PUBLISHED` + current window => runtime `OPEN`
  - `PUBLISHED` + past end date => runtime `CLOSED`
  - `DRAFT`, `CLOSED`, and `ARCHIVED` are not student-submittable
- Important semantic note:
  - lifecycle `PUBLISHED` means the survey has been formally released into operations
  - runtime `CLOSED` can still occur while lifecycle remains `PUBLISHED` if the end date has passed and no admin close/archive action has been taken yet

## Current survey recipient tracking model
- `Survey_Recipient` is the concrete participation snapshot for a survey run.
- One row represents one targeted active student for one survey.
- Key fields:
  - `assigned_at`
  - `opened_at`
  - `submitted_at`
- Current participation semantics:
  - row exists => student was targeted when the survey was published
  - `opened_at` set => student opened survey access at least once
  - `submitted_at` set => student submitted successfully
- Recipient rows are created when a survey is published, based on the assignment scope and the active students that exist at publish time.
- Publish-time recipient eligibility in this phase requires both:
  - `Student.status = ACTIVE`
  - `User.verify = true` (used in this repo as the active/non-deactivated account flag)
- Current limitation:
  - this is a publish-time snapshot, not a live dynamic audience
  - students who become active after publication are not backfilled automatically in this phase
  - assignment changes after publication do not rebuild the recipient audience in this phase
- If a targeted student is later deactivated:
  - their existing recipient row remains for denominator/reporting history
  - they are still counted in the original targeted population
  - they can no longer participate because access and submission require an active account at runtime
- Migration behavior for older databases:
  - recipient rows are generated for existing `PUBLISHED`, `CLOSED`, and `ARCHIVED` surveys using current active students that match existing assignments
  - if an older survey already has a submission, `submitted_at` is copied from `Survey_Response`
  - `opened_at` is initialized from `submitted_at` for migrated submitted rows because historical open timestamps did not exist before this slice

## Current onboarding review storage model
- This phase stores the latest review snapshot directly on `Student`:
  - `review_reason`
  - `review_notes`
  - `reviewed_by_user_id`
  - `reviewed_at`
  - `resubmission_count`
- Full review history and audit trail are not implemented yet.
- That is acceptable for the current lifecycle-depth slice, but it is not sufficient for the later audit/governance phase.

## Current privileged-action audit model
- `Audit_Log` stores a business audit trail for successful privileged actions.
- Current fields:
  - `actor_user_id`
  - `action_type`
  - `target_type`
  - `target_id`
  - `summary`
  - `details`
  - `old_state`
  - `new_state`
  - `created_at`
- Current actions covered in this phase:
  - onboarding approval and rejection
  - survey publish, close, archive, and visibility change
  - user profile update, activation, and deactivation
- Current action vocabulary:
  - `ONBOARDING_APPROVED`
  - `ONBOARDING_REJECTED`
  - `SURVEY_PUBLISHED`
  - `SURVEY_CLOSED`
  - `SURVEY_ARCHIVED`
  - `SURVEY_VISIBILITY_CHANGED`
  - `USER_PROFILE_UPDATED`
  - `USER_ACTIVATED`
  - `USER_DEACTIVATED`
- Current field semantics:
  - `summary` is a short business label for the completed action
  - `details` stores a limited business context summary, such as rejection reason, survey title, recipient count, or before/after profile fields
  - `old_state` and `new_state` store short state summaries, not full row snapshots
- Sensitive-data rule in this phase:
  - audit rows do not store passwords, document contents, or uploaded file payloads
  - stored values are limited to business-safe identifiers and summary fields
- Important scope note:
  - audit rows are written only from successful backend business actions
  - failed validation attempts do not create audit rows
  - denied authorization checks are also not audit-logged in this phase
  - this is a focused governance trail, not a full event history or observability system
