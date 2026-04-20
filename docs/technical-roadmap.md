# Technical Roadmap - Student Feedback System

## 1. Định hướng tổng thể

Dự án không nên được định vị như một bài CRUD khảo sát đơn giản. Hướng đúng là phát triển thành một hệ thống nội bộ cho nhà trường, kết hợp giữa quản lý khảo sát sinh viên, quản lý phản hồi, theo dõi tỷ lệ tham gia, phân tích dữ liệu phản hồi, quản lý onboarding sinh viên, phân quyền, bảo mật, audit và báo cáo.

Mục tiêu của roadmap này là chọn công nghệ đủ mạnh, có lý do nghiệp vụ rõ ràng, có thể demo được, và thể hiện tư duy thiết kế hệ thống theo hướng enterprise.

Dự án cần có hai tầng giá trị:

- Giá trị chức năng nhìn thấy được: AI phân tích phản hồi, dashboard khảo sát, response rate, reminder, export báo cáo, quản lý vòng đời khảo sát, quản lý hồ sơ sinh viên, phân quyền theo vai trò và phạm vi dữ liệu.
- Giá trị kỹ thuật đứng phía sau: kiến trúc backend rõ ràng, API contract, background job, cache/rate limiting, object storage, audit logging, CI/CD security scanning, observability, testing và deployment bằng Docker Compose.

## 2. Tech stack nền tảng

### 2.1. Frontend

Stack nền tảng:

- React
- TypeScript
- Vite
- TailwindCSS
- Shared UI primitives hoặc shadcn/ui
- TanStack Query
- TanStack Table
- Recharts
- Vitest / React Testing Library
- Playwright cho smoke test hoặc end-to-end test quan trọng

Lý do lựa chọn:

- React + TypeScript phù hợp xây dựng SPA hiện đại, dễ chia component và kiểm soát kiểu dữ liệu.
- TailwindCSS giúp phát triển giao diện nhanh nhưng vẫn kiểm soát được design system.
- TanStack Query giúp quản lý server state tốt hơn, đặc biệt với các màn hình admin có search, filter, pagination.
- TanStack Table phù hợp cho các trang có dữ liệu lớn như user management, survey management, feedback queue.
- Recharts dùng để trực quan hóa kết quả khảo sát, response rate, sentiment chart.
- Playwright giúp kiểm thử các luồng quan trọng như login, tạo khảo sát, submit khảo sát, admin duyệt hồ sơ.

Frontend không nên chỉ là giao diện nhập liệu. Frontend cần thể hiện được tư duy sản phẩm:

- AppShell rõ ràng
- navigation theo role
- account/security area
- data table có search/filter/pagination
- trạng thái loading/error/empty thống nhất
- dashboard có ý nghĩa vận hành
- responsive đủ tốt cho demo và sử dụng thực tế

### 2.2. Backend

Stack nền tảng:

- Spring Boot 3.x
- Spring Security
- JWT authentication
- Hexagonal Architecture / Ports and Adapters
- Spring Data JPA
- Spring Validation
- Springdoc OpenAPI
- Spring Scheduler hoặc Quartz
- Spring AI + Gemini API
- Spring Boot Actuator

Lý do lựa chọn:

- Spring Boot phù hợp để xây dựng backend enterprise.
- Spring Security hỗ trợ xác thực, phân quyền, OAuth2/OIDC nếu mở rộng.
- Hexagonal Architecture giúp tách domain/application khỏi infrastructure, phù hợp với dự án muốn thể hiện tư duy kiến trúc.
- Springdoc OpenAPI giúp tạo API documentation rõ ràng, hỗ trợ frontend/backend integration.
- Scheduler hoặc Quartz phục vụ reminder, background analysis, report generation.
- Spring AI + Gemini API dùng cho AI Survey Insight Engine.
- Actuator phục vụ healthcheck, monitoring và deployment readiness.

Backend cần tập trung vào business workflow, không chỉ CRUD:

- onboarding workflow
- survey lifecycle
- recipient tracking
- feedback ticket workflow
- audit logging
- policy-based authorization
- reporting read models

### 2.3. Database & Storage

Stack đề xuất:

- SQL Server
- Redis
- MinIO / S3-compatible Object Storage
- Elasticsearch hoặc OpenSearch ở giai đoạn nâng cao
- Flyway hoặc Liquibase

Lý do lựa chọn:

- SQL Server là database chính, lưu dữ liệu quan hệ như user, role, student, survey, question, response, feedback.
- Redis dùng cho rate limiting, cache dữ liệu ít thay đổi, lock nhẹ cho background jobs hoặc AI analysis.
- MinIO/S3-compatible storage dùng để lưu tài liệu sinh viên và file báo cáo sinh ra từ hệ thống.
- Elasticsearch/OpenSearch dùng cho full-text search và phân tích phản hồi text khi dữ liệu đủ lớn.
- Flyway/Liquibase giúp quản lý migration database chuyên nghiệp hơn.

Không nên dùng Elasticsearch để thay thế SQL filter thông thường ngay từ đầu. SQL Server vẫn là nguồn dữ liệu chính. Elasticsearch/OpenSearch chỉ nên dùng cho tìm kiếm toàn văn phản hồi sinh viên, feedback ticket, highlight keyword hoặc topic/search nâng cao.

### 2.4. DevOps / Deployment

Stack đề xuất:

- Docker
- Docker Compose
- Nginx reverse proxy
- GitHub Actions
- GHCR
- Trivy
- Gitleaks
- OWASP Dependency Check hoặc Dependabot
- Spring Boot Actuator healthcheck
- Prometheus + Grafana ở giai đoạn nâng cao

Lý do lựa chọn:

- Docker giúp đóng gói backend/frontend nhất quán.
- Docker Compose giúp dựng môi trường demo gồm frontend, backend, database, redis, minio, opensearch nếu cần.
- Nginx làm reverse proxy và phục vụ frontend.
- GitHub Actions + GHCR thể hiện CI/CD cơ bản.
- Trivy/Gitleaks/Dependency Check thể hiện tư duy DevSecOps.
- Actuator giúp hệ thống có endpoint health/readiness.
- Prometheus/Grafana có thể bổ sung sau để hiển thị metric vận hành.

## 3. Nhóm công nghệ nên ưu tiên triển khai

### 3.1. Springdoc OpenAPI

Mục tiêu:

- Tự động sinh tài liệu API.
- Giúp frontend hiểu contract backend.
- Giúp báo cáo học thuật có phần API rõ ràng.
- Giúp demo chuyên nghiệp hơn.

Áp dụng:

- Swagger UI cho toàn bộ REST API.
- Group API theo module: auth, account, admin users, onboarding, surveys, survey results, feedback, notifications.

Mức ưu tiên: Cao  
Độ khó: Thấp

Lý do nên làm sớm:

- Dễ triển khai.
- Dễ ghi vào báo cáo.
- Thể hiện tính chuyên nghiệp ngay lập tức.

### 3.2. Audit Logging

Mục tiêu:

- Ghi lại các hành động quan trọng trong hệ thống.
- Tăng tính minh bạch và trách nhiệm.
- Phù hợp với hệ thống nội bộ của trường.

Các hành động nên audit:

- Admin duyệt hồ sơ sinh viên
- Admin từ chối hồ sơ sinh viên
- Sinh viên resubmit hồ sơ
- Admin kích hoạt/vô hiệu hóa user
- Admin tạo/publish/close/archive survey
- Admin thay đổi visibility survey
- Staff phản hồi feedback
- Thay đổi phân quyền hoặc phạm vi truy cập

Bảng gợi ý:

- `audit_log`
    - `id`
    - `actor_user_id`
    - `action_type`
    - `target_type`
    - `target_id`
    - `summary`
    - `old_state`
    - `new_state`
    - `created_at`

Mức ưu tiên: Rất cao  
Độ khó: Trung bình

Giá trị:

- Rất hợp enterprise.
- Dễ giải thích với giảng viên.
- Hỗ trợ phần bảo mật và governance trong báo cáo.

### 3.3. Redis cho Rate Limiting, Cache và Lock

Redis không nên được mô tả chung chung là “cache session”. Với JWT, cache session không phải trọng tâm.

Redis nên dùng cho ba nhóm việc chính.

#### Rate limiting

Áp dụng cho:

- login
- register
- forgot password
- reset password
- submit feedback
- gọi AI analysis
- export report

Công nghệ:

- Redis
- Bucket4j hoặc custom rate limiter
- Resilience4j nếu phù hợp

Mục tiêu:

- Chống spam
- Chống brute force login
- Chống abuse AI API
- Bảo vệ endpoint public

#### Cache

Áp dụng cho:

- department list
- dashboard metrics
- survey result summary
- user management metadata
- system config

Mục tiêu:

- Giảm query lặp
- Tăng tốc màn hình dashboard
- Tăng tính enterprise của hệ thống

#### Distributed lock nhẹ

Áp dụng cho:

- tránh chạy AI analysis trùng trên cùng survey
- tránh tạo report trùng
- tránh reminder job chạy đè nhau

Mức ưu tiên: Cao  
Độ khó: Trung bình

Giá trị:

- Rất hợp DevSecOps.
- Có tác dụng rõ ràng.
- Dễ đưa vào deployment diagram.

### 3.4. Background Job / Scheduler

Mục tiêu:

- Chạy các tác vụ không nên phụ thuộc vào request trực tiếp.
- Làm hệ thống có tính vận hành thật hơn.

Công nghệ đề xuất:

- Spring Scheduler cho giai đoạn đầu
- Quartz Scheduler nếu muốn nâng cấp enterprise hơn
- DB-backed job log để lưu lịch sử chạy job

Use case:

- gửi reminder khảo sát sắp hết hạn
- gửi reminder cho sinh viên chưa mở khảo sát
- gửi reminder cho sinh viên đã mở nhưng chưa submit
- kiểm tra hồ sơ onboarding pending quá lâu
- gửi email khi hồ sơ được duyệt/từ chối
- chạy AI analysis bất đồng bộ
- tạo file export báo cáo
- refresh reporting summary table

Bảng gợi ý:

- `job_run_log`
    - `id`
    - `job_name`
    - `status`
    - `started_at`
    - `finished_at`
    - `duration_ms`
    - `error_message`
    - `created_at`

Mức ưu tiên: Rất cao  
Độ khó: Trung bình

Giá trị:

- Rất dễ giải thích là hệ thống vận hành thật.
- Hợp với reminder, notification, AI analysis, report export.
- Là một trong những tech đáng thêm nhất.

### 3.5. MinIO / S3-compatible Object Storage

Mục tiêu:

- Không lưu file sinh viên bằng local filesystem đơn giản.
- Quản lý file upload theo hướng an toàn và triển khai được.
- Hỗ trợ lưu báo cáo export.

Công nghệ:

- MinIO cho local/dev/homelab
- S3-compatible API
- Storage adapter trong backend
- Signed URL hoặc controlled download endpoint

Áp dụng:

- ảnh căn cước / thẻ sinh viên
- file minh chứng onboarding
- file Excel/PDF report được generate
- attachment nếu feedback sau này có file đính kèm

Metadata nên lưu:

- `original_file_name`
- `object_key`
- `content_type`
- `file_size`
- `checksum`
- `uploaded_by`
- `uploaded_at`
- `storage_provider`

Mức ưu tiên: Cao  
Độ khó: Trung bình

Giá trị:

- Rất hợp với dự án có upload giấy tờ sinh viên.
- Tăng tính production-ready.
- Demo được bằng Docker Compose.

### 3.6. Policy-based Authorization

Mục tiêu:

- Không chỉ phân quyền theo role.
- Kiểm soát dữ liệu theo phạm vi nghiệp vụ.

Vấn đề nếu chỉ dùng role:

- Lecturer có thể xem quá rộng.
- Admin/Lecturer cần bị giới hạn theo khoa, bộ môn, khảo sát được phân công.
- Một số dữ liệu khảo sát có tính nhạy cảm.

Hướng triển khai:

- Vẫn giữ RBAC:
    - Student
    - Lecturer/Lecturer
    - Admin

- Bổ sung policy layer:
    - `canViewSurveyResult(user, survey)`
    - `canManageSurvey(user, survey)`
    - `canReviewStudent(user, student)`
    - `canRespondFeedback(user, feedback)`
    - `canAccessDocument(user, document)`

Use case:

- Giảng viên chỉ xem survey result thuộc khoa/lớp/môn/phạm vi được phân quyền.
- Admin có thể xem toàn bộ.
- Sinh viên chỉ xem dữ liệu của chính mình.
- File giấy tờ chỉ được xem bởi sinh viên sở hữu hoặc admin review.

Mức ưu tiên: Rất cao  
Độ khó: Trung bình

Giá trị:

- Thể hiện tư duy bảo mật tốt.
- Ăn điểm phần phân quyền.
- Tốt cho DevSecOps.

### 3.7. Reporting Read Models / Summary Tables

Mục tiêu:

- Không tính dashboard/report phức tạp bằng query ad-hoc mỗi lần.
- Tạo lớp dữ liệu phục vụ báo cáo.

Use case:

- response rate theo khảo sát
- response rate theo khoa
- số sinh viên targeted/opened/submitted/not submitted
- survey participation funnel
- số hồ sơ onboarding pending quá hạn
- số feedback unresolved
- tỷ lệ sentiment tích cực/trung lập/tiêu cực

Cách triển khai:

- Dùng SQL view nếu đơn giản.
- Dùng summary table nếu dữ liệu cần precompute.
- Update summary khi có event nghiệp vụ hoặc bằng scheduled job.

Bảng gợi ý:

- `survey_participation_summary`
- `survey_sentiment_summary`
- `dashboard_metric_snapshot`
- `feedback_ticket_summary`

Mức ưu tiên: Cao  
Độ khó: Trung bình đến cao

Giá trị:

- Làm dashboard có chất.
- Làm survey result mạnh hơn nhiều.
- Hợp với hệ thống khảo sát thật.

### 3.8. Spring AI + Gemini API

Mục tiêu:

- Không chỉ thu thập khảo sát, mà còn phân tích phản hồi text.
- Tạo điểm nhấn chức năng mạnh cho demo.

Không nên ưu tiên chatbot trước. Chatbot dễ bị xem là add-on.

Nên xây dựng AI Survey Insight Engine.

Chức năng:

- tóm tắt phản hồi text của sinh viên
- phân loại sentiment:
    - Positive
    - Neutral
    - Negative
- trích xuất vấn đề nổi bật
- gom nhóm topic
- tạo executive summary cho admin/lecturer
- gợi ý hành động cải thiện
- liệt kê comment tiêu biểu theo từng nhóm vấn đề

Luồng demo:

1. Sinh viên submit khảo sát có câu trả lời text.
2. Admin mở Survey Result Detail.
3. Admin bấm “Analyze feedback”.
4. Backend gọi Gemini qua Spring AI.
5. Kết quả được lưu vào database.
6. Frontend hiển thị sentiment chart, top topics, summary, suggested actions và representative comments.

Bảng gợi ý:

- `ai_analysis_result`
    - `id`
    - `survey_id`
    - `model_name`
    - `prompt_version`
    - `status`
    - `summary`
    - `sentiment_json`
    - `topics_json`
    - `suggested_actions`
    - `created_by`
    - `created_at`

Mức ưu tiên: Cao, nhưng nên làm sau khi survey result đã ổn  
Độ khó: Trung bình đến cao

Giá trị:

- Rất “đập vào mắt”.
- Có ý nghĩa nghiệp vụ thật.
- Hợp với xu hướng AI nhưng không bị lạc đề.

### 3.9. Elasticsearch / OpenSearch

Mục tiêu:

- Tìm kiếm và phân tích phản hồi text ở quy mô lớn hơn.
- Không dùng để thay SQL filter cơ bản.

Use case phù hợp:

- full-text search trong comment khảo sát
- tìm feedback ticket theo nội dung
- tìm keyword trong phản hồi
- highlight đoạn comment chứa keyword
- lọc comment theo topic/sentiment
- hỗ trợ dashboard text analytics

Index gợi ý:

- `survey_comments`
- `feedback_tickets`
- `ai_topics`

Cách triển khai hợp lý:

- Phase đầu: SQL-backed search/filter
- Phase sau: OpenSearch/Elasticsearch cho full-text search
- Có fallback nếu search service chưa chạy

Mức ưu tiên: Trung bình  
Độ khó: Cao

Giá trị:

- Đẹp trong kiến trúc.
- Nhưng chỉ nên thêm khi có đủ dữ liệu text và nhu cầu search thật.

Ghi chú:

- Elasticsearch không nên là core stack ngay từ đầu.
- OpenSearch có thể phù hợp hơn nếu muốn open-source-friendly.
- Với báo cáo học thuật, có thể ghi Elasticsearch vì dễ nhận diện hơn.

### 3.10. SSE / WebSocket cho realtime update

Mục tiêu:

- Cập nhật dữ liệu vận hành gần realtime.
- Làm dashboard sinh động hơn.

Use case:

- admin dashboard cập nhật số response mới
- survey result cập nhật response count khi sinh viên submit
- trạng thái AI analysis:
    - PENDING
    - RUNNING
    - COMPLETED
    - FAILED
- notification mới cho sinh viên
- trạng thái report export

Khuyến nghị:

- Dùng Server-Sent Events nếu chỉ cần server push một chiều.
- Dùng WebSocket nếu cần tương tác hai chiều thật sự.

Với hệ thống này, SSE hợp lý hơn WebSocket trong giai đoạn đầu.

Mức ưu tiên: Trung bình  
Độ khó: Trung bình

Giá trị:

- Demo đẹp.
- Không bắt buộc.
- Nên làm sau khi background job / notification có thật.

### 3.11. Export Report Pipeline

Mục tiêu:

- Xuất kết quả khảo sát thành file.
- Tăng tính thực tế vì phòng giáo vụ/giảng viên thường cần báo cáo offline.

Công nghệ:

- Apache POI cho Excel
- OpenPDF hoặc JasperReports cho PDF
- MinIO để lưu file report
- Background job để generate report
- Bảng `report_export` để lưu trạng thái

Chức năng:

- export survey results Excel
- export sentiment summary
- export response rate
- export feedback ticket list
- export onboarding pending list
- tạo report theo survey hoặc theo kỳ học

Bảng gợi ý:

- `report_export`
    - `id`
    - `report_type`
    - `status`
    - `requested_by`
    - `file_object_key`
    - `created_at`
    - `finished_at`
    - `error_message`

Mức ưu tiên: Trung bình đến cao  
Độ khó: Trung bình

Giá trị:

- Demo dễ hiểu.
- Ăn điểm báo cáo.
- Kết hợp tốt với MinIO và background job.

### 3.12. Observability

Mục tiêu:

- Hệ thống không chỉ chạy được mà còn quan sát được.
- Phù hợp định hướng DevSecOps.

Giai đoạn đầu:

- Spring Boot Actuator
- health endpoint
- readiness/liveness endpoint
- Docker healthcheck
- structured logging
- request logging cơ bản
- job run log

Giai đoạn sau:

- Prometheus
- Grafana
- dashboard metrics
- alert rule cơ bản
- log aggregation nếu cần

Metric nên có:

- số request theo endpoint
- số lỗi 4xx/5xx
- thời gian xử lý API
- số survey submitted
- số reminder sent
- số AI analysis failed
- số report export failed
- số login failed / rate limited

Mức ưu tiên: Cao  
Độ khó: Thấp đến trung bình

Giá trị:

- Rất hợp DevSecOps.
- Tăng tính production-ready.
- Dễ đưa vào deployment diagram.

### 3.13. Security Scanning trong CI

Mục tiêu:

- Thể hiện tư duy DevSecOps.
- Phát hiện sớm lỗi dependency, image, secret.

Công nghệ đề xuất:

- Trivy scan Docker image
- Gitleaks secret scan
- OWASP Dependency Check hoặc Dependabot
- npm audit hoặc pnpm audit
- Semgrep optional

Áp dụng trong GitHub Actions:

- build backend
- build frontend
- run tests
- scan dependencies
- scan Docker images
- scan secrets
- push image lên GHCR nếu pass

Mức ưu tiên: Rất cao  
Độ khó: Thấp đến trung bình

Giá trị:

- Rất hợp profile DevSecOps.
- Dễ trình bày với thầy.
- Không ảnh hưởng nhiều đến business logic.

### 3.14. OAuth2 / OIDC

Mục tiêu:

- Cho phép đăng nhập bằng tài khoản tổ chức.
- Tăng tính giống hệ thống trường học thật.

Công nghệ:

- Spring Security OAuth2 Client
- Google Workspace
- Microsoft Entra ID
- OIDC

Cách định vị:

- Local JWT login vẫn giữ cho demo.
- OAuth2/OIDC là planned hoặc optional.
- Nếu có domain email trường thì dùng Google/Microsoft.
- Nếu không có identity provider thật, không nên ép triển khai giả.

Mức ưu tiên: Trung bình  
Độ khó: Trung bình đến cao

Giá trị:

- Rất enterprise.
- Nhưng phụ thuộc môi trường và tài khoản tổ chức.
- Không nên ưu tiên trước survey lifecycle/AI/reporting.

### 3.15. Feature Flags

Mục tiêu:

- Bật/tắt chức năng nâng cao mà không sửa code.
- Hữu ích với AI, export, reminder.

Cách triển khai:

- DB-backed feature flags đơn giản
- Không cần dùng LaunchDarkly/Unleash ở giai đoạn này

Flag gợi ý:

- `AI_ANALYSIS_ENABLED`
- `SURVEY_REMINDER_ENABLED`
- `EXPORT_REPORT_ENABLED`
- `OPENSEARCH_ENABLED`
- `OAUTH_LOGIN_ENABLED`

Mức ưu tiên: Thấp đến trung bình  
Độ khó: Thấp

Giá trị:

- Có tính enterprise.
- Hữu ích khi demo các tính năng optional.

## 4. Các chức năng kỹ thuật nổi bật nên xây dựng

### 4.1. AI Survey Insight Engine

Mục tiêu:

- Biến dữ liệu text từ khảo sát thành insight dễ hiểu.

Chức năng:

- sentiment analysis
- topic extraction
- summary
- suggested actions
- representative comments
- regenerate analysis
- lưu lịch sử phân tích

Tech:

- Spring AI
- Gemini API
- Background job
- Redis lock
- SQL Server
- Recharts

Giá trị demo: Rất cao

### 4.2. Survey Participation Tracking

Mục tiêu:

- Không chỉ biết ai đã submit, mà biết ai được mời, ai đã mở, ai chưa làm.

Trạng thái:

- ASSIGNED
- OPENED
- SUBMITTED
- REMINDED
- EXPIRED

Tech:

- SQL Server
- `Survey_Recipient` table
- background reminder job
- reporting summary
- dashboard chart

Giá trị demo: Rất cao

### 4.3. Reminder Workflow

Mục tiêu:

- Hệ thống chủ động nhắc sinh viên làm khảo sát.

Tech:

- Spring Scheduler / Quartz
- Resend email
- persistent notifications
- job log
- `Survey_Recipient`

Chức năng:

- nhắc sinh viên chưa mở khảo sát
- nhắc sinh viên đã mở nhưng chưa submit
- nhắc khảo sát sắp hết hạn
- admin xem số reminder đã gửi

Giá trị demo: Cao

### 4.4. Secure Document Review

Mục tiêu:

- Nâng onboarding từ upload file đơn giản thành review workflow.

Tech:

- MinIO
- signed URL / controlled download
- file validation
- audit logging
- onboarding review status
- resubmission

Chức năng:

- admin xem preview giấy tờ
- admin reject với reason
- sinh viên resubmit
- lưu lịch sử review

Giá trị demo: Cao

### 4.5. Response Report Export

Mục tiêu:

- Xuất kết quả khảo sát phục vụ báo cáo.

Tech:

- Apache POI
- MinIO
- background job
- `report_export` table

Chức năng:

- export Excel
- export sentiment summary
- export response rate
- export comments

Giá trị demo: Cao

### 4.6. Full-text Search for Feedback and Comments

Mục tiêu:

- Tìm kiếm trong phản hồi text.

Tech:

- SQL search giai đoạn đầu
- OpenSearch/Elasticsearch giai đoạn nâng cao

Chức năng:

- search comments
- search feedback
- highlight keyword
- filter by sentiment/topic

Giá trị demo: Trung bình đến cao

### 4.7. Realtime Operational Updates

Mục tiêu:

- Dashboard hoặc analysis status cập nhật gần realtime.

Tech:

- SSE hoặc WebSocket
- background job status
- frontend event listener

Use case:

- AI analysis completed
- report export completed
- new survey response submitted
- new notification created

Giá trị demo: Trung bình đến cao

## 5. Thứ tự triển khai đề xuất

### Phase 1: Foundation có giá trị cao

Mục tiêu:

- Làm hệ thống chuyên nghiệp hơn ngay mà không quá rủi ro.

Tech nên làm:

- Springdoc OpenAPI
- Audit logging
- Redis rate limiting
- Spring Boot Actuator
- Trivy/Gitleaks trong CI
- master data selector
- backend-backed pagination/search/filter đã có thì chuẩn hóa thêm
- frontend test cơ bản cho critical flows

Kết quả mong muốn:

- API có docs.
- Endpoint public được rate limit.
- Admin action có audit.
- CI có security scanning.
- Hệ thống có healthcheck.

### Phase 2: Enterprise workflow

Mục tiêu:

- Làm business flow sâu hơn.

Tech nên làm:

- background jobs / Quartz
- persistent notifications
- reminder workflow
- MinIO object storage
- onboarding resubmission
- secure document review
- survey lifecycle
- survey recipient tracking

Kết quả mong muốn:

- Survey có lifecycle thật.
- Sinh viên được nhắc làm khảo sát.
- Admin theo dõi participation.
- File sinh viên không còn lưu local thô.
- Onboarding có correction loop.

### Phase 3: Analytics và AI

Mục tiêu:

- Tạo điểm nhấn chức năng mạnh.

Tech nên làm:

- Spring AI + Gemini
- AI Survey Insight Engine
- sentiment analysis
- topic extraction
- word cloud / keyword cloud
- reporting read models
- dashboard chart
- Recharts
- optional Redis cache cho report

Kết quả mong muốn:

- Admin/Giảng viên thấy tóm tắt AI.
- Có biểu đồ sentiment.
- Có top topics.
- Có response-rate analytics.
- Có dashboard vận hành.

### Phase 4: Reporting và realtime

Mục tiêu:

- Làm hệ thống giống sản phẩm nội bộ thật.

Tech nên làm:

- Apache POI export Excel
- OpenPDF/JasperReports nếu cần PDF
- MinIO lưu report
- report export job
- SSE cho job status / notification
- optional Prometheus/Grafana

Kết quả mong muốn:

- Admin export báo cáo.
- Job generate report có trạng thái.
- Dashboard hoặc notification cập nhật realtime.
- Hệ thống có monitoring cơ bản.

### Phase 5: Advanced optional

Mục tiêu:

- Chỉ làm nếu còn thời gian.

Tech có thể làm:

- OAuth2/OIDC
- OpenSearch/Elasticsearch
- feature flags
- RabbitMQ nếu background job quá nhiều
- Prometheus/Grafana đầy đủ

Không nên làm:

- Kubernetes
- microservices
- Kafka
- event sourcing
- service mesh

## 6. Technology Positioning

### 6.1. AI / Gemini

Nên dùng, nhưng không nên làm chatbot trước.

Nên dùng cho:

- survey comment summarization
- sentiment analysis
- topic extraction
- suggested actions
- executive summary

Không nên ưu tiên:

- chatbot hỏi đáp chung chung
- AI thay thế logic nghiệp vụ
- AI chạy đồng bộ trong request chính nếu dữ liệu nhiều

### 6.2. Redis

Nên dùng cho:

- rate limiting
- cache dashboard/report
- lock chống duplicate jobs
- cache master data

Không nên mô tả Redis chỉ là session cache.

### 6.3. Elasticsearch / OpenSearch

Nên dùng khi:

- dữ liệu comment/feedback đủ nhiều
- cần full-text search thật
- cần highlight keyword
- cần phân tích text nâng cao

Không nên dùng để thay SQL filter cơ bản.

### 6.4. WebSocket / SSE

Nên dùng khi:

- cần update trạng thái job
- cần thông báo mới realtime
- cần dashboard live response count

Ưu tiên SSE trước vì hệ thống chủ yếu server push một chiều.

### 6.5. OAuth2 / OIDC

Nên đưa vào roadmap vì rất enterprise.

Nhưng chỉ triển khai nếu:

- có Google Workspace / Microsoft Entra ID phù hợp
- hoặc muốn demo login bằng Google

Không nên để OAuth2 làm nghẽn tiến độ core workflow.

### 6.6. Kubernetes / Microservices / Kafka

Không nên làm trong giai đoạn này.

Lý do:

- dự án chưa cần scale ở mức đó
- làm phức tạp deployment
- khó demo
- dễ bị xem là overengineering

Docker Compose + healthcheck + Redis + MinIO + background jobs đã đủ mạnh cho bài này.

## 7. Feature - Tech Mapping

| Feature | Tech / Pattern | Lý do hợp lý | Priority |
|---|---|---|---|
| AI Survey Insight | Spring AI + Gemini | Phân tích phản hồi text, tạo điểm nhấn demo | High |
| Sentiment Analysis | Spring AI + SQL Server | Biến comment thành dữ liệu phân tích | High |
| Topic Extraction | Gemini / keyword extraction | Tìm vấn đề nổi bật trong phản hồi | High |
| Response Rate Analytics | Survey_Recipient + Reporting Summary | Có denominator, không chỉ đếm submission | Critical |
| Survey Reminder | Scheduler/Quartz + Email + Notification | Hệ thống chủ động tăng tỷ lệ tham gia | Critical |
| Secure Document Review | MinIO + File Validation + Audit | Quản lý giấy tờ sinh viên an toàn hơn | High |
| Audit Trail | Audit Log Table | Theo dõi admin action | Critical |
| Rate Limiting | Redis + Bucket4j | Chống spam/brute-force/AI abuse | High |
| OpenAPI Docs | Springdoc OpenAPI | API rõ ràng, dễ báo cáo | High |
| Report Export | Apache POI + MinIO | Xuất báo cáo Excel/PDF | Medium-High |
| Full-text Search | OpenSearch/Elasticsearch | Tìm kiếm comment/feedback nâng cao | Medium |
| Realtime Update | SSE/WebSocket | Live dashboard/job status | Medium |
| Observability | Actuator + Logs + Metrics | Hệ thống có khả năng vận hành | High |
| CI Security Scan | Trivy + Gitleaks | Thể hiện DevSecOps | High |
| OAuth2/OIDC | Spring Security OAuth2 | Đăng nhập bằng tài khoản tổ chức | Medium |
| Frontend Testing | Vitest/Playwright | Bảo vệ flow quan trọng | Medium-High |

## 8. Deployment Architecture đề xuất

Giai đoạn demo mạnh có thể chạy bằng Docker Compose gồm:

- frontend container
- backend container
- SQL Server container hoặc external SQL Server
- Redis container
- MinIO container
- optional OpenSearch container
- Nginx reverse proxy
- optional Prometheus/Grafana

Luồng request:

- User truy cập frontend qua Nginx.
- Frontend gọi `/api`.
- Nginx reverse proxy đến Spring Boot backend.
- Backend truy cập SQL Server.
- Backend dùng Redis cho rate limit/cache/lock.
- Backend dùng MinIO cho file.
- Backend gọi Gemini API cho AI analysis.
- Backend chạy scheduled jobs cho reminder/report.
- Backend expose healthcheck qua Actuator.

## 9. Chiến lược học thuật

Công nghệ cần được gắn với sơ đồ và tài liệu.

### 9.1. Use Case Diagram

Cần thể hiện đủ actor:

- Guest
- Authenticated User
- Student
- Lecturer
- Admin
- System

System actor nên dùng cho:

- send reminder
- generate report
- run AI analysis
- update notification
- refresh dashboard metrics

### 9.2. Sequence Diagram

Nên có sequence cho:

- Student submit survey
- Admin publish survey
- System send reminder
- Admin run AI analysis
- Admin export report
- Student resubmit onboarding documents
- Admin review onboarding case

### 9.3. Class Diagram / Domain Model

Nên có thêm các entity/pattern:

- Survey
- Question
- SurveyResponse
- SurveyRecipient
- FeedbackTicket
- Notification
- AuditLog
- AiAnalysisResult
- ReportExport
- StudentDocument
- JobRunLog

### 9.4. Deployment Diagram

Nên vẽ:

- Browser
- Nginx
- Frontend container
- Backend container
- SQL Server
- Redis
- MinIO
- Gemini API
- Optional OpenSearch
- GitHub Actions / GHCR

## 10. Final Recommended Stack

### 10.1. Core stack

- React + TypeScript + Vite
- TailwindCSS
- Spring Boot 3.x
- Spring Security + JWT
- Hexagonal Architecture
- SQL Server
- Docker / Docker Compose
- Nginx
- GitHub Actions + GHCR

### 10.2. Enterprise enhancement stack

- Springdoc OpenAPI
- Redis
- Bucket4j / Rate Limiting
- Spring Scheduler / Quartz
- MinIO / S3-compatible storage
- Spring Boot Actuator
- Audit logging
- Reporting read models
- Trivy
- Gitleaks
- Dependabot / OWASP Dependency Check

### 10.3. Showcase stack

- Spring AI + Gemini API
- AI Survey Insight Engine
- Recharts
- Apache POI
- SSE / WebSocket
- OpenSearch / Elasticsearch optional
- Prometheus / Grafana optional
- OAuth2/OIDC optional

## 11. Final Recommendation

Dự án nên đi theo hướng:

**Hệ thống khảo sát, phản hồi và phân tích ý kiến sinh viên thông minh cho nhà trường.**

Không nên chỉ xây CRUD khảo sát.

Không nên chạy theo microservices, Kafka hay Kubernetes ở giai đoạn này.

Nên tập trung vào các công nghệ tạo được hiệu quả rõ ràng:

1. Springdoc OpenAPI để chuẩn hóa API.
2. Redis để rate limit/cache/lock.
3. Audit logging để thể hiện governance.
4. Background jobs để gửi reminder và chạy task hệ thống.
5. MinIO để xử lý file sinh viên và report.
6. Survey recipient tracking để có response rate thật.
7. Reporting read models để dashboard có ý nghĩa.
8. Spring AI + Gemini để tạo AI Survey Insight.
9. Apache POI để export report.
10. Trivy/Gitleaks/Actuator để thể hiện DevSecOps.

AI, Redis, Elasticsearch, WebSocket đều có thể giữ trong định hướng, nhưng phải đặt đúng vai trò:

- AI dùng cho phân tích phản hồi, không ưu tiên chatbot chung chung.
- Redis dùng cho rate limiting/cache/lock, không chỉ cache session.
- Elasticsearch/OpenSearch dùng cho full-text search comment khi cần, không thay SQL filter.
- WebSocket/SSE dùng cho live update/job status, không bắt buộc toàn hệ thống realtime.

Nếu triển khai đúng, dự án sẽ không chỉ “nhiều chức năng”, mà còn thể hiện rõ tư duy kỹ sư:

- biết thiết kế workflow
- biết chọn tech theo bài toán
- biết bảo mật
- biết vận hành
- biết báo cáo
- biết scale frontend/backend theo dữ liệu lớn hơn
- biết tạo demo có điểm nhấn