# Frontend

## Overview

This module is the React web client for the Student Feedback System.

It currently includes pages and flows for:

- Login
- Student registration
- Email verification landing page
- Student document upload
- Survey listing and submission
- Admin pending-student review
- Survey result viewing for admin and teacher roles

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
cd E:\Lap\TTCS\student-feedback-system\frontend
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
- `/surveys`
- `/surveys/:id`
- `/admin/students/pending`
- `/survey-results`
- `/survey-results/:id`

## API Integration Notes

- Axios base URL comes from `VITE_API_BASE_URL`.
- The app expects the backend to expose endpoints under `/api`.
- The authentication session is stored in local storage and attached as a bearer token on API requests.

## Local Development Notes

- Start the backend before using the frontend.
- Email verification depends on the backend and Resend being configured correctly.
- Route access is role-based and depends on the backend JWT payload.
