# Reporting and Export Architecture

This document describes the reporting and export boundaries currently used in the backend monolith.

## Why SQL Does Not Live In Controllers

Controllers are HTTP adapters. Their job is to handle request parameters, authentication context, response headers, and DTO mapping. They should not own reporting SQL, database joins, metric calculations, report formatting, or export assembly.

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
          -> EnterpriseSurveyReport
          -> SurveyReportRenderer
              -> BirtSurveyReportRenderer
          -> ExportedReport
  -> ResponseEntity
```

Responsibilities:

- `SurveyResultController`: reads path/auth context and returns HTTP headers/body.
- `SurveyReportExportService`: enforces export authorization, loads report data, prepares the render view, and delegates rendering.
- `LoadSurveyReportPort`: defines the reporting query needed for export.
- `SurveyResultPersistenceAdapter`: loads entities/query data and delegates report assembly.
- `SurveyReportDataAssembler`: aggregates question data and builds the enterprise report read model.
- `EnterpriseSurveyReport`: report view with metadata, branding, filters, period, and summary statistics.
- `SurveyReportRenderer`: renderer boundary for export formats.
- `BirtSurveyReportRenderer`: BIRT PDF/XLSX rendering through the report template.
- `ExportedReport`: format-neutral filename, content type, and bytes returned to the controller.

The renderer does not query the database and does not perform business aggregation. Report data is prepared before rendering.

## Current Supported Formats

PDF and XLSX export are implemented through Eclipse BIRT.

The current endpoint remains:

```text
GET /api/v1/survey-results/{surveyId}/export?format=pdf|xlsx
```

It returns a synchronous binary attachment for admins.

## Template Management

The active BIRT template lives at:

```text
backend/src/main/resources/reports/survey_template.rptdesign
```

Spring loads the template through `ClassPathResource`, so the file must stay under `src/main/resources` and must not be loaded through an absolute local filesystem path.

The BIRT runtime path is configured with:

```text
APP_REPORTS_BIRT_TEMPLATE_PATH=classpath:/reports/survey_template.rptdesign
```

Designer guidance:

- Keep report templates in `backend/src/main/resources/reports/`.
- Install Roboto on the machine used for PDF rendering.
- Use Roboto consistently in template styles to preserve Vietnamese glyphs.
- Prefer BIRT tables and grids for stable PDF/XLSX output.
- Avoid relying on native BIRT chart XML unless it has been verified with the exact runtime dependency set used by the backend.

The core flow should remain:

```text
controller -> export use case -> report query -> enterprise report -> BIRT renderer -> exported report
```

The persistence query should continue returning report/read models, and renderers should continue receiving prepared view data.

## BIRT AppContext Contract

`BirtSurveyReportRenderer` receives `EnterpriseSurveyReport` from the application layer and exposes the prepared data to BIRT through `appContext`. The current appContext keys are:

- `enterpriseSurveyReport`: the single report model.
- `enterpriseSurveyReports`: a one-item list for POJO/list-compatible templates.
- `summary`: `SummaryStatistics`.
- `summaryStatistics`: same summary object for template compatibility.
- `rows`: question row list.
- `questions`: same question list for template compatibility.
- `branding`: `OrganizationBranding`.
- `organizationBranding`: same branding object for template compatibility.
- `filterCriteria`: report filters.
- `reportPeriod`: report period.
- `comments`: qualitative comments.
- `metadata`: compact map of generated-by, generated-at, survey title, survey id, version, environment, and confidentiality label.

The renderer is the only place that translates Java report models into BIRT runtime context. Persistence adapters should not contain export formatting logic.

## Runtime Template Hydration

The active template may contain designer-authored grids, labels, and placeholder structures. At render time, `BirtSurveyReportRenderer` patches a copy of the template in memory and never rewrites `survey_template.rptdesign` on disk.

Runtime hydration currently covers:

- branding header with logo, organization name, and report label
- footer with confidentiality label, generated timestamp, and page number
- executive participation funnel
- question analysis table
- Top 5 question rating chart fallback

## Grid-Based Chart Fallback

Native BIRT chart support is dependency-sensitive and can break differently between Designer and backend runtime. To keep exports stable, the production renderer uses a grid-based horizontal bar chart fallback:

- data source: Top 5 questions by `averageRating`
- label: truncated question content
- value: average rating on a 0 to 5 scale
- visual: fixed-width grid with teal bar cells and numeric labels

This fallback is intentionally boring but reliable. It renders consistently in both PDF and XLSX without requiring BIRT chart model XML compatibility.

## Intentionally Not Implemented

The current architecture intentionally does not include:

- asynchronous export jobs
- generated report storage
- report download history
- summary tables or cached reporting metrics
- a separate reporting service or microservice

No database changes were introduced for this reporting refactor. If a future reporting change requires schema changes, add a new Flyway migration under `backend/src/main/resources/db/migration/` and do not edit existing migration history.
