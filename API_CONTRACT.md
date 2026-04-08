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
- `ACCOUNT_REJECTED`
- `STUDENT_PROFILE_NOT_FOUND`

### `POST /api/auth/upload-docs`

Uploads student onboarding documents for the authenticated student.

Multipart parts:

- `studentCard`
- `nationalId`

Rules:

- Requires authentication
- Student identity is resolved from JWT on the server side
- Current onboarding status must allow document upload

## Surveys

Identity ownership:

- Frontend does not send `studentId`
- Backend resolves the current student server-side from the authenticated JWT user
- Survey APIs require authentication so survey access and submission belong to the logged-in student session

### `GET /api/v1/surveys`

Returns a list of survey summaries.

### `GET /api/v1/surveys/{surveyId}`

Returns one survey summary.

### `GET /api/v1/surveys/{surveyId}/detail`

Returns survey detail and ordered questions.

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
- Submission is rejected if the survey is not open, does not exist, the current student does not exist, or the student already submitted

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

## Admin Approval

### `GET /api/admin/students/pending`

Returns pending student onboarding records for admin review.

### `POST /api/admin/students/{studentId}/approve`

Approves a pending student.

### `POST /api/admin/students/{studentId}/reject`

Rejects a pending student.

## Survey Results

### `GET /api/v1/survey-results`

Returns survey result summaries for admin and teacher roles.

### `GET /api/v1/survey-results/{surveyId}`

Returns detailed question statistics, including rating breakdowns and text comments.
