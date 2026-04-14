# Database Setup Notes

## Fresh setup
- Use `full_schema.sql` to create a new database from scratch.
- Use `seed_data.sql` after that if you want demo accounts and sample data.

## Incremental update path
- For an existing database that already contains application data, apply scripts in `database/migrations/` instead of re-running `full_schema.sql`.
- Current onboarding slice migration:
  - `migrations/2026-04-14-onboarding-review-workflow.sql`

## Demo credentials
- Admin: `admin@university.edu` / `admin123`
- Teacher: `teacher@university.edu` / `teacher123`
- Students: any seeded `student.*@university.edu` account / `student123`

These passwords are BCrypt-hashed in `seed_data.sql` and are compatible with the runtime `BCryptPasswordEncoder`.

## Current onboarding review storage model
- This phase stores the latest review snapshot directly on `Student`:
  - `review_reason`
  - `review_notes`
  - `reviewed_by_user_id`
  - `reviewed_at`
  - `resubmission_count`
- Full review history and audit trail are not implemented yet.
- That is acceptable for the current lifecycle-depth slice, but it is not sufficient for the later audit/governance phase.
