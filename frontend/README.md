# Frontend

## Overview

This module is the React web client for the Student Feedback System.

It currently includes pages and flows for:

- Login
- Student registration
- Email verification landing page
- Student document upload
- Shared authenticated app shell with role-aware navigation
- Account overview and security area
- Student survey listing and submission
- Admin user management
- Admin survey management
- Admin pending-student review
- Survey result viewing for admin and teacher roles
- Staff feedback management

## Stack

- React 19
- TypeScript
- Vite
- React Router
- Axios
- Tailwind CSS

## Environment Variables

```env
VITE_API_BASE_URL=/api
VITE_API_PROXY_TARGET=http://localhost:8080
```

Notes:

- In local development, Vite proxies `/api` to the backend target.
- In production image builds, Nginx serves the SPA and proxies `/api/` to the backend container.

## Run Locally

```powershell
cd frontend
npm ci
npm run dev
```

Useful commands:

```powershell
npm run lint
npm run build
npm run preview
```

Frontend default URL:

- `http://localhost:5173`

## Routing Summary

- `/login`
- `/register`
- `/verify-email`
- `/upload-documents`
- `/account`
- `/account/security`
- `/change-password` -> redirects to `/account/security`
- `/dashboard/student`
- `/dashboard/lecturer`
- `/dashboard/admin`
- `/surveys`
- `/surveys/:id`
- `/notifications`
- `/feedback`
- `/feedback/manage`
- `/admin/users`
- `/admin/users/:id`
- `/admin/surveys`
- `/admin/surveys/create`
- `/admin/surveys/:id/edit`
- `/admin/students/pending`
- `/survey-results`
- `/survey-results/:id`

## Frontend Architecture

- React 19 + Vite + TypeScript SPA
- React Router route groups under a shared authenticated `AppShell`
- Role-aware navigation for:
  - Student workspace
  - Lecturer workspace
  - Admin operations
  - Account
- Shared UI primitives for:
  - page headers and sections
  - loading, empty, and error states
  - status and role badges
  - form sections
- Shared operational data-view components for:
  - toolbars and filters
  - search inputs
  - select filters
  - tables
  - responsive mobile lists
  - pagination

## Current Implementation Notes

- Account overview uses existing auth/session data only. It does not fetch or fabricate unsupported profile fields.
- User management at `/admin/users` is backend-backed for search, filter, pagination, and sort.
- Survey management at `/admin/surveys` is backend-backed for search, filter, pagination, and sort.
- Survey results expose richer backend metadata such as lifecycle state, runtime status, and audience scope.
- Feedback management and pending-student review currently use frontend-side filtering and pagination over the current API payloads.

## API Integration Notes

- Axios base URL comes from `VITE_API_BASE_URL`.
- The app expects the backend to expose endpoints under `/api`.
- The authentication session is stored in local storage and attached as a bearer token on API requests.

## Local Development Notes

- Start the backend before using the frontend.
- Email verification depends on the backend and Resend being configured correctly.
- Route access is role-based and depends on the backend JWT payload.
- Authenticated pages are expected to mount inside the shared `AppShell`; account and change-password behavior should stay under the account route group.
