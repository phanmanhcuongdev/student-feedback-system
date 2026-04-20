# Backend Architecture Notes

The backend follows a BuckPal-style hexagonal flow for new and cleaned-up features:

`adapter.in.web` controller -> `application.port.in` use case -> `application.domain.service` -> `application.port.out` port -> `adapter.out.persistence` or another outbound adapter.

Controllers own HTTP concerns only: request parameters, request DTO mapping, response DTO mapping, and authentication context lookup. They must not contain SQL, direct repository access, persistence entity mapping, or cross-use-case business orchestration.

Application services own business flow. Command use cases own validation, authorization decisions that belong to the operation, state transitions, and writes. Query use cases must remain read-only. In particular, listing or detail reads must not create notifications, mutate recipient state, or perform reporting/export writes as a side effect.

Persistence adapters own repository calls, JPA entities, SQL/JPQL, paging/sorting mechanics, and entity-domain mapping. Application services should depend on output ports and domain/application models, not Spring repositories or web DTOs.

## Identity Boundary

Authenticated identity and student profile identity are different concepts:

- `CurrentIdentityProvider.currentUserId()` returns the authenticated `User` id from the security principal.
- `CurrentIdentityProvider.currentStudentProfileId()` resolves the current student's `Student` profile through `LoadStudentByIdPort.loadByUserId(userId)`.
- Student-domain operations such as survey submit, survey detail recipient checks, feedback creation, onboarding documents, and onboarding status use the student profile id.
- User-domain operations such as audit actor ids, password change, notification ownership, and notification recipient ids use the user id.

Code must not assume `Student.id == User.id`. Notification persistence stores recipients by `User`, so `NotificationCreateCommand.recipientUserIds` must contain user ids even when the recipients were selected from survey recipients or student profiles.

## Database Changes

Do not edit existing Flyway migrations. Any schema change must be added as a new migration file under `backend/src/main/resources/db/migration`.

This cleanup did not require a database migration.

## Deferred Work

Deadline reminders are currently generated from the survey publish command when a newly published survey is already open and closing soon. A later slice can add a scheduled command/use case if reminders must also be generated for older published surveys when they enter the closing-soon window.

Some older modules still predate the cleaned-up flow and should be reviewed before broad refactors. Prefer small slices that move one feature at a time behind input/output ports.
