# Final Recommended Expansion Scope

## 1. Decision Summary
- What final direction should this project take?
  - The project should evolve into a university internal survey operations and onboarding management product, not into a generic "campus platform" and not into a technology-heavy architecture experiment.
  - The final direction should focus on:
    - stronger onboarding lifecycle
    - governed survey lifecycle
    - participation tracking and operational reporting
    - scoped admin/lecturer access
    - real notification and audit behavior
- Why this is the best direction:
  - It strengthens the repo's strongest existing domain instead of fragmenting it.
  - The current implementation already has real foundations in onboarding, surveys, results, and role-based flows. The biggest weakness is not missing modules; it is missing closure and realism.
  - This direction gives the best balance of:
    - product realism
    - business completeness
    - justified technical diversity
    - manageable implementation effort
- Why other directions are less suitable:
  - Expanding mainly into feedback/helpdesk would improve one weak module but would leave the core survey product shallow.
  - Expanding mainly into onboarding verification operations would deepen a useful flow but would underuse the repo's strongest survey/reporting base.
  - Expanding the architecture itself with microservices, event platforms, or infrastructure-heavy patterns would increase explanation cost faster than product value.

## 2. Final Feature Scope

### Core lifecycle features
- Onboarding correction workflow
  - Why it was selected:
    - The current onboarding flow is one of the clearest signs of “student-project logic”: verify, upload, approve/reject, stop.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Survey lifecycle states
  - Why it was selected:
    - Current survey management is still campaign CRUD with limited state control.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Recipient tracking and participation states
  - Why it was selected:
    - The current system cannot calculate meaningful response rates because it tracks only completed responses.
  - Expected impact:
    - Critical
  - Mandatory or optional:
    - Mandatory
- Feedback ticket lifecycle
  - Why it was selected:
    - This upgrades feedback from a thread into a service process, but it is less central than onboarding and survey operations.
  - Expected impact:
    - Medium-High
  - Mandatory or optional:
    - Optional

### Admin/governance features
- Reviewer notes and rejection reasons for onboarding
  - Why it was selected:
    - Real approval decisions need explanation and recovery guidance.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Scoped lecturer/admin access
  - Why it was selected:
    - Current role-only access to results is too broad and unrealistic.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Survey ownership / approval metadata
  - Why it was selected:
    - Needed to make survey lifecycle governance believable.
  - Expected impact:
    - Medium-High
  - Mandatory or optional:
    - Mandatory
- Master-data-backed admin forms
  - Why it was selected:
    - Current admin UI exposes raw IDs and weak operator experience.
  - Expected impact:
    - Medium
  - Mandatory or optional:
    - Mandatory

### Reporting/analytics features
- Response-rate reporting
  - Why it was selected:
    - It is the most important missing analytic.
  - Expected impact:
    - Critical
  - Mandatory or optional:
    - Mandatory
- Operational dashboard metrics
  - Why it was selected:
    - Current dashboards are present but shallow.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Trend comparison by term/run
  - Why it was selected:
    - Valuable once participation and term metadata exist.
  - Expected impact:
    - Medium-High
  - Mandatory or optional:
    - Optional
- Exportable reports
  - Why it was selected:
    - Useful for demonstration and institutional realism, but not essential to core product closure.
  - Expected impact:
    - Medium
  - Mandatory or optional:
    - Optional

### Operational/notification features
- Persistent in-app notifications
  - Why it was selected:
    - Current notification logic is not operationally real.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Reminder workflow for pending surveys
  - Why it was selected:
    - The system should actively help achieve participation, not just display surveys.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Stale-case escalation for onboarding
  - Why it was selected:
    - Useful for operator realism but not as central as the core workflow itself.
  - Expected impact:
    - Medium
  - Mandatory or optional:
    - Optional

### Security/audit features
- Audit logging for privileged actions
  - Why it was selected:
    - Approval, deactivation, visibility change, publish, close, and archive should be traceable.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Secure document handling
  - Why it was selected:
    - Student identity documents are already part of the product and currently handled too weakly.
  - Expected impact:
    - High
  - Mandatory or optional:
    - Mandatory
- Split account activation semantics from verification semantics
  - Why it was selected:
    - Current `verify` usage is overloaded and weakly expressive.
  - Expected impact:
    - Medium-High
  - Mandatory or optional:
    - Mandatory

## 3. Final Technology Scope
- Background jobs / scheduler
  - Why it was selected:
    - Needed for reminders, stale-case checks, and possibly summary refreshes.
  - What concrete problem it solves:
    - Makes notification and escalation behavior proactive instead of page-driven.
  - What feature(s) it supports:
    - persistent notifications
    - reminder workflow
    - stale-case escalation
  - Mandatory or optional:
    - Mandatory
- Object storage abstraction
  - Why it was selected:
    - Current local file storage is too weak for identity documents.
  - What concrete problem it solves:
    - Safer, more deployable file handling.
  - What feature(s) it supports:
    - onboarding document review
    - secure document access
    - export/report files later
  - Mandatory or optional:
    - Mandatory
- Policy-based authorization layer
  - Why it was selected:
    - Static roles are not enough for scoped survey/result visibility.
  - What concrete problem it solves:
    - Prevents overbroad lecturer/admin access.
  - What feature(s) it supports:
    - lecturer scoping
    - survey ownership/governance
  - Mandatory or optional:
    - Mandatory
- Audit logging model
  - Why it was selected:
    - Governance improvements need traceability.
  - What concrete problem it solves:
    - Records privileged actions and decision history.
  - What feature(s) it supports:
    - onboarding review
    - user management actions
    - survey lifecycle actions
  - Mandatory or optional:
    - Mandatory
- Reporting read models / summary tables
  - Why it was selected:
    - Response-rate and operational metrics will be awkward if built only from ad hoc transactional queries.
  - What concrete problem it solves:
    - Cleaner, more scalable reporting and dashboard queries.
  - What feature(s) it supports:
    - response-rate reporting
    - operational dashboards
    - trend reporting later
  - Mandatory or optional:
    - Mandatory
- Frontend testing
  - Why it was selected:
    - The frontend currently has no testing story and will become more stateful.
  - What concrete problem it solves:
    - Protects multi-step UX from regression.
  - What feature(s) it supports:
    - onboarding workflow
    - survey admin workflow
    - notifications UX
  - Mandatory or optional:
    - Mandatory
- Backend integration testing
  - Why it was selected:
    - Lifecycle changes and authorization changes need stronger confidence.
  - What concrete problem it solves:
    - Protects key business flows across controller-service-persistence boundaries.
  - What feature(s) it supports:
    - all mandatory lifecycle/governance additions
  - Mandatory or optional:
    - Mandatory
- Structured logging / metrics
  - Why it was selected:
    - Better runtime realism and easier diagnosis of lifecycle jobs and admin actions.
  - What concrete problem it solves:
    - Improves operational visibility.
  - What feature(s) it supports:
    - reminder jobs
    - onboarding workflows
    - dashboard metric confidence
  - Mandatory or optional:
    - Optional but strongly recommended
- Deployment orchestration
  - Why it was selected:
    - The repo has images but no cohesive runnable stack.
  - What concrete problem it solves:
    - Better local demo and deployment realism.
  - What feature(s) it supports:
    - all final-scope features indirectly
  - Mandatory or optional:
    - Optional

## 4. Feature-Tech Justification

| Feature | Supporting tech/pattern | Why this pair is worth building | Evaluation value | Engineering value |
|---|---|---|---|---|
| Onboarding correction workflow | Object storage + audit logging | It turns a weak binary admin step into a credible review process with traceability and secure file handling | Very high | High |
| Reviewer notes and rejection reasons | Audit logging | Makes decisions accountable and explainable | High | Medium-High |
| Survey lifecycle states | Backend integration tests | Lifecycle logic is only convincing if protected by real flow tests | High | High |
| Recipient tracking and participation states | Reporting read models | This is what makes survey analytics meaningful instead of decorative | Very high | High |
| Response-rate reporting | Reporting read models | Solves the repo’s biggest analytics gap directly | Very high | High |
| Scoped lecturer/admin access | Policy-based authorization | Fixes an obvious realism/security problem without changing the stack | High | Very high |
| Persistent notifications | Background jobs / scheduler | Makes reminder logic operational instead of page-derived | High | High |
| Reminder workflow | Background jobs + email workflow expansion | Produces clear product value and justified async behavior | High | High |
| Operational dashboards | Reporting read models + structured metrics | Makes dashboards useful rather than cosmetic | High | Medium-High |
| Secure document handling | Object storage + validation hardening | Sensitive documents deserve better handling than local path copies | Medium-High | High |

## 5. Minimum Strong Scope
- If I want the minimum scope that still looks much stronger than the current project, I should build:
  - onboarding rejection reasons + reviewer notes + resubmission
  - survey lifecycle states (`draft`, `published`, `closed`, `archived`)
  - lecturer access scoping
  - audit logging
  - response-rate reporting with minimal recipient tracking
- Why this is the minimum strong scope:
  - It fixes the most visible product shallowness.
  - It improves both academic and recruiter evaluation meaningfully.
  - It does not require too many supporting technologies at once.

## 6. Ideal Balanced Scope
- If I want the best balance of realism, technical diversity, and manageable effort, I should build exactly:
  - onboarding correction workflow
  - reviewer notes / rejection reasons / decision history
  - survey lifecycle states and publish/archive controls
  - recipient tracking and participation states
  - response-rate reporting
  - scoped lecturer/admin access
  - persistent notifications and reminder workflow
  - actionable admin/lecturer dashboards
  - audit logging
  - secure document storage abstraction
  - frontend tests
  - backend integration tests
- This is the ideal balanced scope because:
  - It creates a clearly stronger product without turning into an endless feature program.
  - It introduces broader engineering capability only where the product really needs it.
  - It keeps the repo understandable and demoable.

## 7. Maximum Scope Before Overengineering
- The bad tradeoff point begins when I try to add:
  - advanced search infrastructure
  - distributed messaging infrastructure
  - microservices
  - orchestration-heavy cloud patterns
  - AI summarization features
- Which additions become low-value:
  - separate search platform before SQL filtering/reporting is mature
  - export pipeline before core reporting is correct
  - trend analytics before participation tracking exists
- Which technologies become hard to justify:
  - Kafka or RabbitMQ-sized solutions before a simple scheduler/outbox pattern is exhausted
  - Kubernetes before a coherent local/runtime stack exists
  - Elasticsearch before the product actually suffers from filter/search limitations
- What should be intentionally excluded:
  - microservices
  - Kafka-scale messaging
  - Kubernetes
  - event sourcing
  - AI-first features for comments

## 8. Final Prioritized Build List
- Top 10 total additions:
  - onboarding correction workflow
  - reviewer notes and rejection reasons
  - survey lifecycle states
  - recipient tracking and participation states
  - response-rate reporting
  - scoped lecturer/admin access
  - persistent notifications
  - reminder workflow
  - audit logging
  - secure document storage abstraction
- Top 5 mandatory additions:
  - onboarding correction workflow
  - survey lifecycle states
  - recipient tracking plus response-rate reporting
  - scoped lecturer/admin access
  - audit logging
- Top 5 optional additions:
  - feedback ticket lifecycle
  - operational dashboard expansion
  - trend reporting by term/run
  - exportable reports
  - deployment orchestration
- Top 5 things to deliberately NOT build:
  - microservices
  - Kafka or heavy broker infrastructure
  - Kubernetes
  - Elasticsearch
  - AI summarization features

## 9. Final Verdict
- The single best scope recommendation:
  - Build a survey-operations-and-onboarding expansion that closes lifecycle gaps, adds scoped governance, and introduces only the technologies required to support those workflows.
- The single best feature cluster:
  - survey lifecycle + recipient tracking + response-rate reporting
- The single best technology cluster:
  - policy-based authorization + audit logging + background jobs
- The most dangerous overengineering trap:
  - replacing product depth with infrastructure complexity
