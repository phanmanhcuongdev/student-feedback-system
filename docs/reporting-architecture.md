# Reporting and Export Architecture

This document describes the reporting and export boundaries currently used in the backend monolith.

## Why SQL Does Not Live In Controllers

Controllers are HTTP adapters. Their job is to handle request parameters, authentication context, response headers, and DTO mapping. They should not own reporting SQL, database joins, metric calculations, CSV formatting, or export assembly.

Keeping SQL out of controllers protects the application from three common problems:

- HTTP behavior and reporting semantics changing together by accident.
- Duplicated native SQL drifting between dashboards, result pages, and exports.
- Future export formats forcing controller rewrites.

The backend follows a Ports and Adapters flow, so reporting SQL belongs behind output ports in persistence adapters.

## Reporting Boundary In The Monolith

Reporting stays inside the current Spring Boot monolith. It is not a separate service, and it does not use summary tables or generated-report storage.

The current reporting boundary is:

```text
web controller
  -> input port / use case
      -> application service
          -> output port / reporting query
              -> persistence adapter
                  -> native SQL / repositories / query mapping
```

Reporting persistence adapters may use native SQL when that is the clearest fit for dashboard or read-model queries. Shared reporting SQL fragments live in `adapter.out.persistence.reporting` and are intentionally small.

Current shared fragments include:

- survey runtime status CASE expression
- recipient participation stats join
- recipient response-rate expression

Adapter-specific `FROM`, filtering, sorting, and grouping logic remains local to the adapter that owns the query.

## Admin Analytics Flow

Admin analytics uses this flow:

```text
AdminAnalyticsController
  -> GetAdminAnalyticsOverviewUseCase
      -> AdminAnalyticsReportingService
          -> LoadAdminAnalyticsPort
              -> AdminAnalyticsPersistenceAdapter
```

The controller maps query parameters to `AdminAnalyticsOverviewQuery` and maps application results to existing API response DTOs. The native SQL and row mapping live in `AdminAnalyticsPersistenceAdapter`.

## Survey Result Export Flow

Survey result export uses this flow:

```text
SurveyResultController
  -> ExportSurveyReportUseCase
      -> SurveyReportExportService
          -> LoadSurveyReportPort
              -> SurveyResultPersistenceAdapter
          -> SurveyReportView
          -> SurveyReportRenderer
              -> CsvSurveyReportRenderer
          -> ExportedReport
  -> ResponseEntity
```

Responsibilities:

- `SurveyResultController`: reads path/auth context and returns HTTP headers/body.
- `SurveyReportExportService`: enforces export authorization, loads report data, prepares the render view, and delegates rendering.
- `LoadSurveyReportPort`: defines the reporting query needed for export.
- `SurveyResultPersistenceAdapter`: loads and maps the export read model.
- `SurveyReportView`: format-neutral view passed to renderers.
- `SurveyReportRenderer`: renderer boundary for export formats.
- `CsvSurveyReportRenderer`: CSV formatting and escaping only.
- `ExportedReport`: format-neutral filename, content type, and bytes returned to the controller.

The renderer does not query the database and does not perform business aggregation. Report data is prepared before rendering.

## Current Supported Formats

Only CSV export is implemented.

The current endpoint remains:

```text
GET /api/v1/survey-results/{surveyId}/export
```

It returns a synchronous CSV attachment for admins.

## Adding XLSX Or PDF Later

Future formats should be added by extending the renderer boundary, not by changing controller or persistence ownership.

A later slice can add:

- a format request parameter, for example `?format=csv|xlsx|pdf`
- renderer selection in the export application flow
- `XlsxSurveyReportRenderer` or `PdfSurveyReportRenderer`

The core flow should remain:

```text
controller -> export use case -> report query -> report view -> renderer -> exported report
```

The persistence query should continue returning report/read models, and renderers should continue receiving prepared view data.

## Intentionally Not Implemented

The current architecture intentionally does not include:

- XLSX export
- PDF export
- asynchronous export jobs
- generated report storage
- report download history
- summary tables or cached reporting metrics
- a separate reporting service or microservice

No database changes were introduced for this reporting refactor. If a future reporting change requires schema changes, add a new Flyway migration under `backend/src/main/resources/db/migration/` and do not edit existing migration history.
