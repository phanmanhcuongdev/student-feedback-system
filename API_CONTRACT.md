# Survey API Contract

Current MVP source of truth for the implemented survey slice.

## Identity ownership

- Frontend does not send `studentId`.
- Backend resolves the current student server-side from `APP_AUTH_MOCK_STUDENT_ID`.
- This is a temporary MVP-safe identity strategy until real authentication is wired.

## Endpoints

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

- `RATING` questions require `rating` in range `1..5` and `comment = null`.
- `TEXT` questions require non-empty `comment` and `rating = null`.
- Every survey question must be answered exactly once.
- Submission is rejected if the survey is not open, does not exist, the current student does not exist, or the student already submitted.

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
