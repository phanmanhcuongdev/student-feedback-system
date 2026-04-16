# API Contract

Implemented API slices currently used by the frontend.

## Auth And Onboarding

### `POST /api/auth/register-student`

Registers a student account.

Request body:

```json
{
  "name": "Nguyen Van A",
  "email": "student@example.com",
  "password": "secret",
  "studentCode": "S12345",
  "departmentName": "Computer Science"
}
```

Behavior:

- Creates a user and student in `EMAIL_UNVERIFIED`
- Creates a verification token
- Sends a verification email through Resend
- Does not return the verification URL or token to the client

### `GET /api/auth/verify-email?token=...`

Validates the verification token.

Possible business codes include:

- `VERIFY_SUCCESS`
- `TOKEN_INVALID`
- `TOKEN_EXPIRED`
- `ALREADY_VERIFIED`
- `STUDENT_NOT_FOUND`

### `POST /api/auth/login`

Authenticates a user and returns a JWT access token on success.

The login flow is state-aware for student accounts and can return failure codes such as:

- `INVALID_CREDENTIALS`
- `EMAIL_NOT_VERIFIED`
- `WAITING_APPROVAL`
- `STUDENT_PROFILE_NOT_FOUND`

Student accounts in `REJECTED` are now allowed to sign in again so they can view review feedback and resubmit documents.

### `POST /api/auth/upload-docs`

Uploads student onboarding documents for the authenticated student.

Multipart parts:

- `studentCard`
- `nationalId`

Rules:

- Requires authentication
- Student identity is resolved from JWT on the server side
- Current onboarding status must allow document upload
- `EMAIL_VERIFIED` submits the first onboarding package
- `REJECTED` submits corrected documents and returns the account to `PENDING`

### `GET /api/auth/onboarding-status`

Returns the authenticated student's onboarding state, review feedback, upload eligibility, and resubmission count.

Current implementation note:

- This slice stores only the latest review snapshot on the student record.
- Full review history is not implemented yet.

## Surveys

Lifecycle note:

- Surveys now have two different status concepts:
  - `lifecycleState`: explicit admin governance state (`DRAFT`, `PUBLISHED`, `CLOSED`, `ARCHIVED`)
  - `status`: derived runtime availability for student-facing screens (`NOT_OPEN`, `OPEN`, `CLOSED`)
- Student-facing survey APIs only expose surveys that are both `PUBLISHED` and not hidden.
- A `PUBLISHED` survey whose end date has already passed still remains lifecycle `PUBLISHED` until an admin closes or archives it. In that case the runtime `status` becomes `CLOSED`, so students still cannot submit.
- Participation note:
  - student-facing access now depends on concrete `Survey_Recipient` rows, not only assignment rules
  - recipients are created when the survey is published
  - this is a publish-time audience snapshot for the current phase
  - publish-time recipient eligibility requires an `ACTIVE` student record plus an active user account (`verify = true` in this repo)

Identity ownership:

- Frontend does not send `studentId`
- Backend resolves the current student server-side from the authenticated JWT user
- Survey APIs require authentication so survey access and submission belong to the logged-in student session

### `GET /api/v1/surveys`

Returns a list of survey summaries.

Rules:

- Only surveys with an existing recipient row for the current active student are returned
- `DRAFT`, hidden, and archived surveys do not appear

### `GET /api/v1/surveys/{surveyId}`

Returns one survey summary.

### `GET /api/v1/surveys/{surveyId}/detail`

Returns survey detail and ordered questions.

Rules:

- Draft, archived, hidden, or out-of-scope surveys are treated as not found
- Detail access is enforced in the use case layer, not only at the controller layer
- Opening survey access records `opened_at` for the recipient the first time they access survey detail or direct survey view

### `POST /api/v1/surveys/{surveyId}/submit`

Request body:

```json
{
  "answers": [
    {
      "questionId": 1,
      "rating": 5,
      "comment": null
    }
  ]
}
```

Rules:

- `RATING` questions require `rating` in range `1..5` and `comment = null`
- `TEXT` questions require non-empty `comment` and `rating = null`
- Every survey question must be answered exactly once
- Submission is rejected if the survey is not published and open, does not exist, the current student does not exist, or the student already submitted
- Successful submission updates the matching recipient row with `submitted_at`

Response body:

```json
{
  "success": true,
  "code": "SUBMIT_SUCCESS",
  "message": "Submit survey successfully"
}
```

Business response codes currently used by the frontend:

- `SUBMIT_SUCCESS`
- `ALREADY_SUBMITTED`
- `SURVEY_CLOSED`
- `INVALID_INPUT`
- `SURVEY_NOT_FOUND`
- `STUDENT_NOT_FOUND`

## Admin Survey Management

### `POST /api/admin/surveys`

Creates a survey draft.

Request body:

```json
{
  "title": "Midterm Teaching Feedback",
  "description": "Collect student feedback for the midterm teaching period.",
  "startDate": "2026-04-20T00:00:00Z",
  "endDate": "2026-04-27T00:00:00Z",
  "questions": [
    {
      "content": "Rate the lecturer's teaching clarity.",
      "type": "RATING"
    }
  ],
  "recipientScope": "ALL_STUDENTS",
  "recipientDepartmentId": null
}
```

Response body:

```json
{
  "success": true,
  "surveyId": 12,
  "code": "SURVEY_CREATED",
  "message": "Create survey successfully"
}
```

Behavior:

- New surveys start in `DRAFT`
- Drafts are not visible to students
- After create, admin is expected to continue on the draft management screen and explicitly publish later

### `PUT /api/admin/surveys/{surveyId}`

Updates an existing survey draft.

Rules:

- Only `DRAFT` surveys can be edited in this phase
- Editable fields in `DRAFT`: title, description, dates, recipient scope, recipient department, questions
- `PUBLISHED`, `CLOSED`, and `ARCHIVED` are read-only for these fields

### `POST /api/admin/surveys/{surveyId}/publish`

Publishes a survey draft.

Rules:

- Only `DRAFT` surveys can be published
- Publishing requires:
  - valid start and end dates
  - at least one question with non-blank content
  - at least one valid assignment target
  - department-scoped assignments must include a department id
- Publishing creates concrete recipient rows for the currently active students who match the assignment scope
- Assignment semantics after publication:
  - assignment scope is used only to build the initial recipient snapshot
  - later assignment changes do not backfill or remove recipient rows in this phase because published surveys are already locked from editing
- Successful publish actions now persist audit records
- Current publish audit action type:
  - `SURVEY_PUBLISHED`

### `POST /api/admin/surveys/{surveyId}/close`

Closes a published survey early.

Rules:

- Only `PUBLISHED` surveys can be closed
- Closing sets lifecycle to `CLOSED`
- If the survey end date is still in the future, closing also truncates `endDate` to the close time
- Successful close actions now persist audit records
- Current close audit action type:
  - `SURVEY_CLOSED`

### `POST /api/admin/surveys/{surveyId}/archive`

Archives a closed survey.

Rules:

- Only `CLOSED` surveys can be archived
- Archived surveys remain read-only in this phase
- Archived surveys do not re-enter student-facing flows in this phase
- Successful archive actions now persist audit records
- Current archive audit action type:
  - `SURVEY_ARCHIVED`

### `POST /api/admin/surveys/{surveyId}/visibility`

Updates survey visibility.

Request body:

```json
{
  "hidden": true
}
```

Rules:

- Visibility changes are allowed only for `PUBLISHED` and `CLOSED` surveys
- `hidden` remains a visibility control and is not the same thing as lifecycle state
- Visibility changes do not make a `DRAFT` or `ARCHIVED` survey student-visible
- Successful visibility changes now persist audit records
- Current visibility audit action type:
  - `SURVEY_VISIBILITY_CHANGED`
- Visibility audit state semantics:
  - `oldState` and `newState` store `VISIBLE` or `HIDDEN`

### `GET /api/admin/surveys`

Supports query parameters:

- `keyword`
- `lifecycleState`
- `runtimeStatus`
- `hidden`
- `recipientScope`
- `startDateFrom`
- `endDateTo`
- `page`
- `size`
- `sortBy`
- `sortDir`

Summary rows now include:

- `recipientDepartmentName`
- `targetedCount`
- `openedCount`
- `submittedCount`
- `responseRate`

### `GET /api/admin/surveys/departments`

Returns department options for survey audience filters and survey authoring.

### `GET /api/admin/surveys/{surveyId}`

Detail payloads also include:

- `targetedCount`
- `openedCount`
- `submittedCount`
- `responseRate`
- `pendingRecipients`
  - recipients whose `submittedAt` is still null
  - this includes both `ASSIGNED` and `OPENED` participation states

## Admin Approval

### `GET /api/admin/students/pending`

Returns pending student onboarding records for admin review.

Current frontend note:

- queue search, filter, sort, and pagination are currently frontend-side over this payload

### `POST /api/admin/students/{studentId}/approve`

Approves a pending student.

Request body:

```json
{
  "reviewNotes": "Identity documents verified."
}
```

### `POST /api/admin/students/{studentId}/reject`

Rejects a pending student.

Request body:

```json
{
  "reviewReason": "Document mismatch",
  "reviewNotes": "Please upload a clearer student card and a complete national ID image."
}
```

Rules:

- Rejection reason is required
- Approval and rejection both persist reviewer notes
- Pending student payloads now include the previous review context and `resubmissionCount`
- Successful approve/reject actions now also persist business audit records with actor, target, action type, review summary, and old/new state
- Current onboarding audit action types:
  - `ONBOARDING_APPROVED`
  - `ONBOARDING_REJECTED`

## Survey Results

### `GET /api/v1/survey-results`

Returns survey result summaries for admin and teacher roles.

Authorization semantics in this phase:

- `ADMIN` can view all survey results
- `TEACHER` can only view results for surveys explicitly targeted to the teacher's own department
- teacher scope is derived from `Survey_Assignment`
- surveys targeted to `ALL_STUDENTS` remain admin-visible only in this phase because the repository does not yet have a stronger ownership model for broader lecturer access
- if a teacher profile is missing or the teacher department scope is incomplete, access fails closed

Summary rows now include denominator-based participation metrics:

- `lifecycleState`
- `runtimeStatus`
- `recipientScope`
- `recipientDepartmentName`
- `targetedCount`
- `openedCount`
- `submittedCount`
- `responseRate`

### `GET /api/v1/survey-results/{surveyId}`

Returns detailed question statistics, including rating breakdowns and text comments.

Authorization semantics match the list endpoint:

- admins can open any survey result
- teachers can open only in-scope department-targeted survey results
- out-of-scope teacher access is rejected with `403 Forbidden`
- a truly nonexistent survey still returns not found
- denied survey-result authorization attempts are not audit-logged in this phase

Detail payloads also include:

- `lifecycleState`
- `runtimeStatus`
- `recipientScope`
- `recipientDepartmentName`
- `targetedCount`
- `openedCount`
- `submittedCount`
- `responseRate`

## Admin User Management

### `PUT /api/admin/users/{userId}`

Updates a managed user profile.

Behavior:

- Successful updates now persist an audit record with actor, target user, and before/after summary
- Current user-profile audit action type:
  - `USER_PROFILE_UPDATED`

### `POST /api/admin/users/{userId}/activate`

Activates a managed user account.

### `POST /api/admin/users/{userId}/deactivate`

Deactivates a managed user account.

Rules:

- Self-activation/deactivation remains blocked
- Successful activation/deactivation actions now persist audit records
- Current account-state audit action types:
  - `USER_ACTIVATED`
  - `USER_DEACTIVATED`

### `GET /api/admin/users`

Supports backend-backed query parameters:

- `role`
- `keyword`
- `active`
- `studentStatus`
- `departmentId`
- `page`
- `size`
- `sortBy`
- `sortDir`

### `GET /api/admin/users/departments`

Returns department options for user-management filters and edit forms.

## Account And Security Frontend Routing

Frontend route behavior currently used by the web client:

- `/account`
- `/account/security`
- `/change-password` redirects to `/account/security`

Current implementation note:

- the account overview relies on existing authenticated session data and does not use a dedicated profile endpoint
