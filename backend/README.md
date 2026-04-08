# Backend

## Overview

This module is the Spring Boot API server for the Student Feedback System.

It handles:

- Authentication and JWT issuance
- Student onboarding and email verification
- Student document upload
- Admin approval flow
- Survey listing, detail, submission, and result viewing

## Stack

- Java 21
- Spring Boot 4
- Spring MVC
- Spring Security
- Spring Data JPA
- Microsoft SQL Server
- Resend API for verification emails

## Project Structure

- [`src/main/java/com/ttcs/backend/adapter/in`](/E:/Lap/TTCS/student-feedback-system/backend/src/main/java/com/ttcs/backend/adapter/in)
  Web controllers and security filters
- [`src/main/java/com/ttcs/backend/application`](/E:/Lap/TTCS/student-feedback-system/backend/src/main/java/com/ttcs/backend/application)
  Domain models, use cases, input ports, output ports
- [`src/main/java/com/ttcs/backend/adapter/out`](/E:/Lap/TTCS/student-feedback-system/backend/src/main/java/com/ttcs/backend/adapter/out)
  Persistence adapters, JWT adapter, Resend adapter
- [`src/main/java/com/ttcs/backend/config`](/E:/Lap/TTCS/student-feedback-system/backend/src/main/java/com/ttcs/backend/config)
  Spring configuration beans
- [`src/main/resources/application.yaml`](/E:/Lap/TTCS/student-feedback-system/backend/src/main/resources/application.yaml)
  Main Spring configuration

## Required Configuration

Variables used by the backend:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
APP_JWT_SECRET=
APP_JWT_ACCESS_TOKEN_EXPIRATION_MS=86400000
APP_VERIFY_EMAIL_URL_BASE=http://localhost:5173
RESEND_API_KEY=
APP_MAIL_FROM=noreply@cuongdso.id.vn
APP_WEB_ALLOWED_ORIGINS=http://localhost:5173
RESEND_API_URL=https://api.resend.com/emails
```

Important notes:

- The schema must already exist because JPA validation is enabled.
- Email verification uses Resend. Registration will not silently fake success if email delivery is unavailable.
- SQL Server is the only database wired in the current configuration.

## Run Locally

```powershell
cd E:\Lap\TTCS\student-feedback-system\backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Backend runs on:

- `http://localhost:8080`

## Key Flows

- `POST /api/auth/register-student`
  Creates a student account in `EMAIL_UNVERIFIED` state and sends a verification email.
- `GET /api/auth/verify-email`
  Validates the token and advances onboarding within the allowed student status vocabulary.
- `POST /api/auth/login`
  Issues a JWT when the account is in a login-eligible state.
- `POST /api/auth/upload-docs`
  Uploads student documents for the authenticated student.
- `GET /api/v1/surveys`
  Returns survey summaries for the authenticated user.
- `POST /api/v1/surveys/{surveyId}/submit`
  Submits survey answers for the current student.

## Development Notes

- Controllers stay in `adapter.in.web`.
- Business logic stays in `application.domain.service`.
- External service calls such as Resend stay in `adapter.out`.
- If you update environment variables, restart the application so Spring picks them up.
