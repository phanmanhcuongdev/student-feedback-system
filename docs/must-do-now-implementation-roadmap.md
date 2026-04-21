# Must-do-now Implementation Roadmap

Tài liệu này là tutorial triển khai theo từng giai đoạn cho các module trọng tâm trong roadmap. Mỗi giai đoạn phải bám kiến trúc Hexagonal/Ports-and-Adapters của backend hiện tại.

Phạm vi hiện tại của tài liệu:

- Giai đoạn 1: Observability & Logging.
- Giai đoạn 2: Redis Rate Limiting. Chưa viết, chờ xác nhận.
- Giai đoạn 3: AI Analysis Integration với Gemini. Chưa viết, chờ xác nhận.

Ghi chú path tham chiếu:

- Roadmap evaluation: `docs/technical-roadmap-evaluation.md`.
- Backend config chính: `backend/src/main/resources/application.yaml`.
- Security config hiện tại: `backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java`.
- Docker/Portainer stack: `docker-compose.yml` hoặc stack env tương ứng trong Portainer.

## Giai đoạn 1: Observability & Logging

Mục tiêu của giai đoạn này là giúp hệ thống có health check, readiness/liveness probe, metrics cơ bản và log JSON chuẩn hóa để quan sát dễ hơn trong Portainer. Đây là nền tảng vận hành trước khi thêm Redis rate limiting và AI analysis.

### 1.1. Kết quả mong muốn

Sau khi triển khai xong:

- Backend expose health endpoint tại `/actuator/health`.
- Backend expose liveness/readiness tại:
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
- Portainer có thể dùng health endpoint để kiểm tra container backend.
- Metrics cơ bản có tại `/actuator/metrics`, nhưng không nên public tự do ra Internet.
- Log backend trên stdout là JSON, dễ đọc/lọc trong Portainer logs.
- SQL logging ồn ào từ `spring.jpa.show-sql` được tắt, thay bằng logging có kiểm soát.

### 1.2. Dependency

File cần chỉnh:

- `backend/pom.xml`

Thêm Actuator:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Thêm Logstash Encoder để Logback xuất JSON:

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>9.0</version>
</dependency>
```

Ghi chú:

- Spring Boot parent đang quản lý version cho `spring-boot-starter-actuator`, không cần khai báo version riêng.
- `logstash-logback-encoder` cần version rõ ràng vì không phải dependency được Spring Boot BOM quản lý trực tiếp.

### 1.3. Environment Variables cho Linux Bridge/Portainer

Các biến môi trường nên đưa vào Portainer stack env hoặc file env mà stack đang dùng.

```env
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized
MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED=true

LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TTCS_BACKEND=INFO
LOGGING_LEVEL_ORG_HIBERNATE_SQL=WARN
```

Khuyến nghị vận hành trong mạng nội bộ Linux Bridge:

- Chỉ publish port backend `8080` nếu frontend/Nginx hoặc admin cần truy cập từ ngoài Docker network.
- Nếu backend, frontend, Redis, MinIO cùng nằm trong một bridge network, các service nên gọi nhau bằng hostname container/service, ví dụ `backend:8080`, `minio:9000`, `redis:6379`.
- Actuator health có thể cho phép frontend reverse proxy hoặc Portainer healthcheck gọi nội bộ.
- Không expose `/actuator/metrics` ra public Internet nếu chưa có reverse proxy rule hoặc auth riêng.

Ví dụ healthcheck trong `docker-compose.yml` hoặc Portainer stack:

```yaml
backend:
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 30s
    timeout: 5s
    retries: 5
    start_period: 60s
```

Nếu image backend chưa có `curl`, dùng một trong các hướng sau:

- Cài `curl` trong Dockerfile backend.
- Dùng `wget -qO- http://localhost:8080/actuator/health`.
- Hoặc để Portainer kiểm tra endpoint từ bên ngoài container thay vì Docker healthcheck nội bộ.

### 1.4. Cấu hình `application.yaml`

File cần chỉnh:

- `backend/src/main/resources/application.yaml`

Thêm hoặc cập nhật các block sau.

```yaml
spring:
  application:
    name: backend
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false

management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,metrics}
  endpoint:
    health:
      probes:
        enabled: ${MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED:true}
      show-details: ${MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS:when_authorized}
  health:
    db:
      enabled: true
    diskspace:
      enabled: true

logging:
  level:
    root: ${LOGGING_LEVEL_ROOT:INFO}
    com.ttcs.backend: ${LOGGING_LEVEL_COM_TTCS_BACKEND:INFO}
    org.hibernate.SQL: ${LOGGING_LEVEL_ORG_HIBERNATE_SQL:WARN}
```

Giải thích:

- `management.endpoints.web.exposure.include`: chỉ expose endpoint cần thiết. Giai đoạn này dùng `health,info,metrics`.
- `management.endpoint.health.probes.enabled`: bật liveness/readiness probes.
- `show-details: when_authorized`: không lộ chi tiết health cho người chưa được phân quyền.
- `spring.jpa.show-sql=false`: tránh in SQL raw ra console, vì console sẽ là JSON log vận hành.

Nếu sau này thêm Redis dependency ở Giai đoạn 2, Actuator sẽ tự có Redis health indicator khi Redis connection factory tồn tại. Khi đó có thể bổ sung:

```yaml
management:
  health:
    redis:
      enabled: true
```

### 1.5. Cấu hình Security cho Actuator

File cần chỉnh:

- `backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java`

Trong `authorizeHttpRequests`, thêm rule cho health/info trước `.anyRequest().authenticated()`.

Snippet đề xuất:

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/error").permitAll()
        .requestMatchers(
                "/actuator/health",
                "/actuator/health/**",
                "/actuator/info"
        ).permitAll()
        .requestMatchers("/actuator/metrics", "/actuator/metrics/**").hasRole("ADMIN")
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/v1/survey-results/**").hasAnyRole("ADMIN", "LECTURER")
        .requestMatchers("/api/v1/feedback/staff", "/api/v1/feedback/staff/**").hasAnyRole("ADMIN", "LECTURER")
        .requestMatchers("/api/v1/feedback/*/responses").hasAnyRole("ADMIN", "LECTURER")
        .requestMatchers(
                "/api/auth/login",
                "/api/auth/register-student",
                "/api/auth/forgot-password",
                "/api/auth/reset-password"
        ).permitAll()
        .requestMatchers(HttpMethod.GET, "/api/auth/verify-email", "/api/auth/verify-email/**").permitAll()
        .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**"
        ).permitAll()
        .anyRequest().authenticated()
)
```

Lý do:

- `/actuator/health` cần mở để Portainer, Docker healthcheck hoặc reverse proxy có thể kiểm tra backend sống/chết.
- `/actuator/info` có thể mở nếu chỉ chứa metadata không nhạy cảm.
- `/actuator/metrics` nên yêu cầu `ADMIN` hoặc chỉ được route nội bộ, vì metrics có thể làm lộ thông tin vận hành.

Nếu muốn chặt hơn trong production:

- Chỉ permit `/actuator/health` và `/actuator/health/**`.
- Không expose `/actuator/info`.
- Không publish `/actuator/metrics` qua reverse proxy public.

### 1.6. Tạo `logback-spring.xml`

File cần tạo:

- `backend/src/main/resources/logback-spring.xml`

Mẫu cấu hình JSON logging:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <springProperty scope="context" name="appName" source="spring.application.name" defaultValue="backend"/>

    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${appName}","component":"student-feedback-system"}</customFields>
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <level>level</level>
                <logger>logger</logger>
                <thread>thread</thread>
                <message>message</message>
                <stackTrace>stack_trace</stackTrace>
            </fieldNames>
        </encoder>
    </appender>

    <logger name="org.hibernate.SQL" level="${LOGGING_LEVEL_ORG_HIBERNATE_SQL:-WARN}"/>
    <logger name="com.ttcs.backend" level="${LOGGING_LEVEL_COM_TTCS_BACKEND:-INFO}"/>

    <root level="${LOGGING_LEVEL_ROOT:-INFO}">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
```

Log mẫu sau khi chạy backend:

```json
{
  "timestamp": "2026-04-21T10:20:30.123+07:00",
  "level": "INFO",
  "thread": "main",
  "logger": "com.ttcs.backend.BackendApplication",
  "message": "Started BackendApplication in 8.42 seconds",
  "service": "backend",
  "component": "student-feedback-system"
}
```

Ghi chú:

- Log JSON nên đi ra stdout/stderr để Docker và Portainer thu thập tự nhiên.
- Không ghi file log trong container nếu chưa có chiến lược rotate volume rõ ràng.
- Không log JWT token, password, reset token, verify token, hoặc nội dung document upload.

### 1.7. Tùy chọn: thêm request correlation ID

Đây là bước nên làm nhưng có thể để sau nếu muốn giữ Giai đoạn 1 gọn.

Ý tưởng:

- Nếu request có header `X-Correlation-Id`, giữ nguyên.
- Nếu chưa có, backend tự tạo UUID.
- Đưa correlation id vào MDC để JSON log có field `correlation_id`.

File dự kiến nếu triển khai:

- `backend/src/main/java/com/ttcs/backend/adapter/in/web/CorrelationIdFilter.java`

Snippet:

```java
package com.ttcs.backend.adapter.in.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlation_id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = request.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
```

Nếu thêm filter này, cập nhật `logback-spring.xml` để include MDC:

```xml
<includeMdcKeyName>correlation_id</includeMdcKeyName>
```

### 1.8. Verify

#### Verify bằng Maven

Chạy backend test:

```bash
cd backend
./mvnw test
```

Hoặc trên Windows:

```powershell
cd backend
.\mvnw.cmd test
```

#### Verify health endpoint

Khi backend đang chạy:

```bash
curl http://localhost:8080/actuator/health
```

Kết quả kỳ vọng:

```json
{"status":"UP"}
```

Kiểm tra probes:

```bash
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

Kết quả kỳ vọng:

```json
{"status":"UP"}
```

#### Verify metrics endpoint

Nếu đang đăng nhập với quyền admin hoặc test trong mạng nội bộ:

```bash
curl http://localhost:8080/actuator/metrics
```

Kết quả kỳ vọng là danh sách metric names, ví dụ:

```json
{
  "names": [
    "application.ready.time",
    "http.server.requests",
    "jvm.memory.used",
    "process.cpu.usage"
  ]
}
```

#### Verify JSON logs trong Portainer

Trong Portainer:

1. Mở container `backend`.
2. Vào tab `Logs`.
3. Kiểm tra log mỗi dòng là JSON object.
4. Tìm các field:
   - `timestamp`
   - `level`
   - `logger`
   - `message`
   - `service`
   - `component`

Kết quả không đạt nếu:

- Log vẫn là text plain kiểu Spring Boot mặc định.
- Console in ra SQL raw quá nhiều.
- Log có token/password hoặc nội dung nhạy cảm.

#### Verify Docker/Portainer healthcheck

Trong Portainer container list:

- Backend container nên chuyển sang trạng thái `healthy`.
- Nếu trạng thái `unhealthy`, kiểm tra:
  - Container backend có công cụ `curl`/`wget` không.
  - Port nội bộ có đúng `8080` không.
  - `/actuator/health` có bị security chặn không.
  - Backend có kết nối được DB/Redis/MinIO bắt buộc không.

### 1.9. Checklist Giai đoạn 1

- [ ] Thêm `spring-boot-starter-actuator` vào `backend/pom.xml`.
- [ ] Thêm `logstash-logback-encoder` vào `backend/pom.xml`.
- [ ] Cập nhật `backend/src/main/resources/application.yaml`.
- [ ] Cập nhật `SecurityConfig` để mở `/actuator/health`.
- [ ] Tạo `backend/src/main/resources/logback-spring.xml`.
- [ ] Tắt `spring.jpa.show-sql`.
- [ ] Cấu hình healthcheck trong Portainer stack hoặc `docker-compose.yml`.
- [ ] Kiểm tra `/actuator/health`.
- [ ] Kiểm tra `/actuator/health/liveness`.
- [ ] Kiểm tra `/actuator/health/readiness`.
- [ ] Kiểm tra log JSON trong Portainer.

## Giai đoạn 2: Redis Rate Limiting

Mục tiêu của giai đoạn này là bảo vệ các endpoint auth khỏi brute force, spam đăng ký và abuse reset password bằng Redis-backed rate limiting. Thiết kế phải giữ đúng Hexagonal Architecture:

- Application layer định nghĩa năng lực qua `RateLimiterPort`.
- Outbound adapter triển khai Redis qua `RedisRateLimiterAdapter`.
- Inbound security adapter chặn request qua `AuthRateLimitingFilter`.
- `SecurityConfig` chỉ nối filter vào Spring Security chain.

Nguyên tắc vận hành quan trọng:

- Rate limiter phải fail-open ở giai đoạn này. Nếu Redis lỗi hoặc mất kết nối, request vẫn được đi tiếp nhưng backend log lỗi. Lý do là hệ thống ưu tiên availability cho khoảng 10.000 user, tránh việc Redis outage làm toàn bộ login/register bị tê liệt.
- Key rate-limit dùng `IP + Request URI` để chặn abuse theo nguồn truy cập và endpoint.
- Filter phải chạy trước `JwtAuthenticationFilter`, vì các endpoint auth public thường chưa có JWT.

### 2.1. Kết quả mong muốn

Sau khi triển khai xong:

- Các endpoint `/api/auth/**` được rate limit.
- Request vượt ngưỡng nhận HTTP `429 Too Many Requests`.
- Response có header:
  - `X-RateLimit-Remaining`
  - `Retry-After` khi bị chặn
- Nếu Redis unavailable:
  - Backend log error.
  - Request vẫn đi tiếp.
  - Hệ thống không mất khả năng login/register.
- Redis host/port cấu hình bằng env để chạy mượt trong VyOS/Linux Bridge hoặc Docker bridge network.

### 2.2. Dependency

File cần chỉnh:

- `backend/pom.xml`

Thêm Redis starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

Dependency này cung cấp:

- `StringRedisTemplate`
- Redis connection factory
- Redis health indicator cho Actuator nếu Actuator đã bật ở Giai đoạn 1

Không cần thêm Redisson ở giai đoạn này. Use case hiện tại chỉ cần counter đơn giản theo fixed time window, `StringRedisTemplate` là đủ.

### 2.3. Cấu hình Redis trong hạ tầng VyOS/Linux Bridge

Nếu Redis chạy trong cùng Docker/Portainer stack với backend:

```env
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=
```

Nếu Redis chạy trên VM/server khác trong mạng nội bộ đi qua VyOS/Linux Bridge:

```env
REDIS_HOST=192.168.10.50
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

Khuyến nghị:

- Trong Docker bridge network, dùng hostname service như `redis`, không dùng IP container.
- Trong Linux Bridge/VyOS network, dùng IP nội bộ ổn định hoặc DNS nội bộ.
- Không publish Redis ra Internet.
- Nếu bắt buộc publish port Redis, chỉ allow từ backend subnet bằng firewall/VyOS rule.
- Với 10.000 user, Redis nên bật persistence tùy nhu cầu, nhưng rate limit key là dữ liệu tạm nên không cần bảo toàn tuyệt đối.

Ví dụ service Redis trong `docker-compose.yml` hoặc Portainer stack:

```yaml
redis:
  image: redis:7.4-alpine
  container_name: redis
  restart: unless-stopped
  command: ["redis-server", "--appendonly", "no"]
  ports:
    - "6379:6379"
  networks:
    - app-network
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

Ghi chú:

- `appendonly=no` là hợp lý cho rate limiting vì key có TTL và không cần restore sau restart.
- Nếu Redis còn dùng cho job lock/cache quan trọng sau này, cân nhắc bật persistence riêng.

### 2.4. Configuration trong `application.yaml`

File cần chỉnh:

- `backend/src/main/resources/application.yaml`

Thêm block Redis:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: ${REDIS_TIMEOUT:2s}
```

Thêm block rate limit:

```yaml
app:
  rate-limit:
    auth:
      enabled: ${APP_RATE_LIMIT_AUTH_ENABLED:true}
      max-requests: ${APP_RATE_LIMIT_AUTH_MAX_REQUESTS:20}
      window: ${APP_RATE_LIMIT_AUTH_WINDOW:PT1M}
      fail-open: ${APP_RATE_LIMIT_AUTH_FAIL_OPEN:true}
```

Giải thích:

- `max-requests=20`: mỗi IP + URI được gọi tối đa 20 lần trong một cửa sổ.
- `window=PT1M`: cửa sổ 1 phút theo ISO-8601 duration.
- `fail-open=true`: Redis lỗi thì cho request đi tiếp.
- `timeout=2s`: tránh request bị treo quá lâu nếu Redis mất kết nối.

Env gợi ý cho production/demo:

```env
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_TIMEOUT=2s

APP_RATE_LIMIT_AUTH_ENABLED=true
APP_RATE_LIMIT_AUTH_MAX_REQUESTS=20
APP_RATE_LIMIT_AUTH_WINDOW=PT1M
APP_RATE_LIMIT_AUTH_FAIL_OPEN=true
```

Nếu login abuse mạnh hơn, có thể hạ xuống:

```env
APP_RATE_LIMIT_AUTH_MAX_REQUESTS=10
APP_RATE_LIMIT_AUTH_WINDOW=PT1M
```

### 2.5. Configuration Properties

File cần tạo:

- `backend/src/main/java/com/ttcs/backend/config/RateLimitProperties.java`

Code:

```java
package com.ttcs.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(Auth auth) {

    public record Auth(
            boolean enabled,
            long maxRequests,
            Duration window,
            boolean failOpen
    ) {
    }
}
```

Sau đó enable configuration properties trong application config.

File cần chỉnh:

- `backend/src/main/java/com/ttcs/backend/BackendApplication.java`

Snippet:

```java
package com.ttcs.backend;

import com.ttcs.backend.config.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RateLimitProperties.class)
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

Nếu project đã có class config riêng để enable properties, có thể đặt `@EnableConfigurationProperties(RateLimitProperties.class)` ở đó thay vì `BackendApplication`.

### 2.6. Outbound Port: `RateLimiterPort`

File cần tạo:

- `backend/src/main/java/com/ttcs/backend/application/port/out/RateLimiterPort.java`

Code:

```java
package com.ttcs.backend.application.port.out;

public interface RateLimiterPort {

    RateLimitDecision consume(String key);

    record RateLimitDecision(
            boolean allowed,
            long remaining,
            long retryAfterSeconds
    ) {
        public static RateLimitDecision allowed(long remaining) {
            return new RateLimitDecision(true, remaining, 0);
        }

        public static RateLimitDecision blocked(long retryAfterSeconds) {
            return new RateLimitDecision(false, 0, retryAfterSeconds);
        }
    }
}
```

Vai trò:

- Application port không biết Redis là gì.
- Inbound filter chỉ gọi `consume(key)` và nhận decision.
- Có thể thay Redis bằng in-memory adapter trong test mà không đổi filter.

### 2.7. Outbound Adapter: `RedisRateLimiterAdapter`

File cần tạo:

- `backend/src/main/java/com/ttcs/backend/adapter/out/redis/RedisRateLimiterAdapter.java`

Tạo package mới nếu chưa có:

- `backend/src/main/java/com/ttcs/backend/adapter/out/redis`

Code:

```java
package com.ttcs.backend.adapter.out.redis;

import com.ttcs.backend.application.port.out.RateLimiterPort;
import com.ttcs.backend.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiterAdapter implements RateLimiterPort {

    private static final String KEY_PREFIX = "rate-limit:auth:";

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    @Override
    public RateLimitDecision consume(String key) {
        RateLimitProperties.Auth auth = properties.auth();

        if (!auth.enabled()) {
            return RateLimitDecision.allowed(auth.maxRequests());
        }

        try {
            return consumeWithRedis(KEY_PREFIX + key, auth);
        } catch (RedisConnectionFailureException ex) {
            return handleRedisFailure(key, ex);
        } catch (RuntimeException ex) {
            return handleRedisFailure(key, ex);
        }
    }

    private RateLimitDecision consumeWithRedis(String redisKey, RateLimitProperties.Auth auth) {
        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count == null) {
            log.error("Redis returned null increment result for rate limit key={}", redisKey);
            return failOpenOrBlocked(auth);
        }

        if (count == 1) {
            redisTemplate.expire(redisKey, auth.window());
        }

        long remaining = Math.max(0, auth.maxRequests() - count);
        if (count <= auth.maxRequests()) {
            return RateLimitDecision.allowed(remaining);
        }

        long retryAfterSeconds = resolveRetryAfterSeconds(redisKey, auth.window());
        return RateLimitDecision.blocked(retryAfterSeconds);
    }

    private long resolveRetryAfterSeconds(String redisKey, Duration fallbackWindow) {
        Long expireSeconds = redisTemplate.getExpire(redisKey);
        if (expireSeconds == null || expireSeconds < 1) {
            return Math.max(1, fallbackWindow.toSeconds());
        }
        return expireSeconds;
    }

    private RateLimitDecision handleRedisFailure(String key, RuntimeException ex) {
        if (properties.auth().failOpen()) {
            log.error("Redis rate limiter failed open for key={}", key, ex);
            return RateLimitDecision.allowed(properties.auth().maxRequests());
        }

        log.error("Redis rate limiter failed closed for key={}", key, ex);
        return RateLimitDecision.blocked(Math.max(1, properties.auth().window().toSeconds()));
    }

    private RateLimitDecision failOpenOrBlocked(RateLimitProperties.Auth auth) {
        if (auth.failOpen()) {
            return RateLimitDecision.allowed(auth.maxRequests());
        }
        return RateLimitDecision.blocked(Math.max(1, auth.window().toSeconds()));
    }
}
```

Logic chính:

1. Tạo Redis key dạng `rate-limit:auth:{IP}:{URI}`.
2. Gọi `increment(redisKey)`.
3. Nếu counter vừa được tạo (`count == 1`), set TTL bằng `expire(redisKey, window)`.
4. Nếu `count <= maxRequests`, cho qua.
5. Nếu vượt ngưỡng, trả decision blocked với `Retry-After` lấy từ TTL.
6. Nếu Redis lỗi và `fail-open=true`, log error và cho qua.

Lý do dùng `increment` + `expire`:

- Đơn giản, đủ nhanh cho auth endpoint.
- Redis xử lý increment nguyên tử.
- TTL tự dọn key sau cửa sổ thời gian.
- Không cần scheduler cleanup.

Điểm cần lưu ý:

- Đây là fixed window rate limiting. Có thể có burst ở ranh giới giữa hai cửa sổ.
- Với auth endpoint, fixed window là đủ cho giai đoạn này.
- Nếu sau này cần chống abuse tinh vi hơn, chuyển sang sliding window hoặc token bucket bằng Lua script.

### 2.8. Inbound Adapter: `AuthRateLimitingFilter`

File cần tạo:

- `backend/src/main/java/com/ttcs/backend/adapter/in/security/AuthRateLimitingFilter.java`

Code:

```java
package com.ttcs.backend.adapter.in.security;

import com.ttcs.backend.application.port.out.RateLimiterPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthRateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterPort rateLimiterPort;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String key = buildRateLimitKey(request);
        RateLimiterPort.RateLimitDecision decision = rateLimiterPort.consume(key);

        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));

        if (!decision.allowed()) {
            writeRateLimitedResponse(response, decision.retryAfterSeconds());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String buildRateLimitKey(HttpServletRequest request) {
        return clientIp(request) + ":" + request.getRequestURI();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void writeRateLimitedResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"success":false,"code":"RATE_LIMITED","message":"Too many requests. Please try again later."}
                """);
    }
}
```

Giải thích:

- `shouldNotFilter`: chỉ áp dụng cho `/api/auth/**`.
- `buildRateLimitKey`: dùng `IP + Request URI`, đúng yêu cầu.
- `clientIp`: ưu tiên `X-Forwarded-For`, sau đó `X-Real-IP`, cuối cùng là `remoteAddr`.
- Nếu chạy sau reverse proxy/Nginx, cần đảm bảo proxy forward đúng header IP thật.

Nếu sau này route auth chuyển sang `/api/v1/auth/**`, đổi `shouldNotFilter` thành:

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return !(uri.startsWith("/api/auth/") || uri.startsWith("/api/v1/auth/"));
}
```

### 2.9. Gắn filter vào Spring Security chain

File cần chỉnh:

- `backend/src/main/java/com/ttcs/backend/config/SecurityConfig.java`

Thêm dependency vào constructor qua Lombok `@RequiredArgsConstructor`:

```java
private final AuthRateLimitingFilter authRateLimitingFilter;
```

Import:

```java
import com.ttcs.backend.adapter.in.security.AuthRateLimitingFilter;
```

Trong `securityFilterChain`, chèn filter trước `JwtAuthenticationFilter`.

Snippet:

```java
.addFilterBefore(authRateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

Điểm cần đảm bảo:

- `AuthRateLimitingFilter` chạy trước JWT filter.
- Với code hiện tại, cả hai filter đều được đặt trước `UsernamePasswordAuthenticationFilter`; thứ tự khai báo trong chain nên để rate limit trước JWT.
- Auth endpoints public vẫn cần nằm trong `permitAll`, nhưng request đã đi qua filter rate limit trước khi tới controller.

Ví dụ đoạn cuối config:

```java
http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics", "/actuator/metrics/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register-student",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/verify-email", "/api/auth/verify-email/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
        )
        .addFilterBefore(authRateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

### 2.10. Endpoint scope nên rate limit

Filter `/api/auth/**` sẽ bao phủ:

- `POST /api/auth/login`
- `POST /api/auth/register-student`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/auth/verify-email`
- `POST /api/auth/upload-docs`
- `GET /api/auth/onboarding-status`
- `POST /api/auth/change-password`

Tuy nhiên không phải endpoint nào cũng public. Nếu muốn chỉ rate limit public auth endpoints, đổi filter sang whitelist:

```java
private boolean isProtectedAuthEndpoint(HttpServletRequest request) {
    String method = request.getMethod();
    String uri = request.getRequestURI();

    return ("POST".equals(method) && "/api/auth/login".equals(uri))
            || ("POST".equals(method) && "/api/auth/register-student".equals(uri))
            || ("POST".equals(method) && "/api/auth/forgot-password".equals(uri))
            || ("POST".equals(method) && "/api/auth/reset-password".equals(uri));
}
```

Khuyến nghị thực tế:

- Phase đầu: rate limit toàn bộ `/api/auth/**` để đơn giản và an toàn.
- Nếu user thật bị ảnh hưởng ở `upload-docs`, tách whitelist public endpoints sau.

### 2.11. Verify

#### Verify Redis connectivity

Trong Portainer hoặc host chạy Redis:

```bash
redis-cli -h redis -p 6379 ping
```

Kết quả kỳ vọng:

```text
PONG
```

Nếu Redis ở IP nội bộ qua VyOS/Linux Bridge:

```bash
redis-cli -h 192.168.10.50 -p 6379 ping
```

#### Verify backend startup

Chạy backend:

```bash
cd backend
./mvnw spring-boot:run
```

Hoặc Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Kiểm tra log:

- Không có lỗi tạo `StringRedisTemplate`.
- Actuator health vẫn `UP`.

```bash
curl http://localhost:8080/actuator/health
```

Nếu Actuator có Redis health indicator, khi Redis sống kết quả chi tiết sẽ có Redis `UP` nếu được phép xem details.

#### Verify rate limit success path

Gọi login dưới ngưỡng:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"demo@example.com\",\"password\":\"wrong-password\"}"
```

Kỳ vọng:

- Response không phải `429`.
- Có header `X-RateLimit-Remaining`.

#### Verify blocked path

Gọi nhiều lần vượt `APP_RATE_LIMIT_AUTH_MAX_REQUESTS`.

PowerShell ví dụ:

```powershell
1..25 | ForEach-Object {
  curl.exe -i -X POST http://localhost:8080/api/auth/login `
    -H "Content-Type: application/json" `
    -d "{\"email\":\"demo@example.com\",\"password\":\"wrong-password\"}"
}
```

Kỳ vọng sau khi vượt ngưỡng:

```http
HTTP/1.1 429
Retry-After: 30
X-RateLimit-Remaining: 0
Content-Type: application/json
```

Body:

```json
{"success":false,"code":"RATE_LIMITED","message":"Too many requests. Please try again later."}
```

#### Verify Redis key

Trong Redis:

```bash
redis-cli keys "rate-limit:auth:*"
```

Kiểm tra TTL:

```bash
redis-cli ttl "rate-limit:auth:127.0.0.1:/api/auth/login"
```

Nếu IP thực tế khác, key sẽ khác. Có thể dùng `keys` để tìm.

#### Verify fail-open

Tạm dừng Redis trong môi trường test:

```bash
docker stop redis
```

Gọi login:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"demo@example.com\",\"password\":\"wrong-password\"}"
```

Kỳ vọng:

- Request vẫn không bị chặn bởi rate limiter.
- Backend log error dạng JSON có message tương tự:

```text
Redis rate limiter failed open for key=...
```

Khởi động lại Redis:

```bash
docker start redis
```

### 2.12. Checklist Giai đoạn 2

- [ ] Thêm `spring-boot-starter-data-redis` vào `backend/pom.xml`.
- [ ] Thêm Redis env vào Portainer stack hoặc `.env`.
- [ ] Thêm `spring.data.redis.*` vào `application.yaml`.
- [ ] Thêm `app.rate-limit.auth.*` vào `application.yaml`.
- [ ] Tạo `RateLimitProperties`.
- [ ] Enable `RateLimitProperties`.
- [ ] Tạo `RateLimiterPort`.
- [ ] Tạo `RedisRateLimiterAdapter`.
- [ ] Tạo `AuthRateLimitingFilter`.
- [ ] Gắn `AuthRateLimitingFilter` trước `JwtAuthenticationFilter`.
- [ ] Verify request dưới ngưỡng vẫn đi qua.
- [ ] Verify request vượt ngưỡng trả `429`.
- [ ] Verify Redis key có TTL.
- [ ] Verify Redis outage thì fail-open.

## Giai đoạn 3: AI Analysis Integration với Gemini

Mục tiêu của giai đoạn này là thêm chức năng "AI Summary" cho kết quả khảo sát, giúp Admin/Giảng viên phân tích nhanh các phản hồi text từ sinh viên. Đây không phải chatbot tự do. AI chỉ nhận dữ liệu khảo sát đã được backend chuẩn hóa và trả về bản phân tích có cấu trúc.

Thiết kế vẫn bám Hexagonal Architecture:

- Application layer định nghĩa use case và outbound port `AiAnalysisPort`.
- Domain/application service lấy dữ liệu survey result, kiểm tra quyền, gọi rate limit, sau đó gọi AI port.
- Outbound adapter `GeminiAiAdapter` dùng Spring AI `ChatClient`.
- Inbound web controller chỉ expose command "Generate AI Summary" cho Admin/Giảng viên.

Nguồn dependency tham chiếu:

- Spring AI Google GenAI Chat: `https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html`
- Spring AI ChatClient API: `https://docs.spring.io/spring-ai/reference/api/chatclient.html`
- Spring AI Dependency Management: `https://docs.spring.io/spring-ai/reference/getting-started.html`

### 3.1. Kết quả mong muốn

Sau khi triển khai xong:

- Admin/Giảng viên có thể click "AI Summary" trên màn hình survey result/dashboard.
- Backend tạo summary dựa trên phản hồi khảo sát, không cho user nhập prompt tự do.
- Mỗi Admin/Giảng viên bị giới hạn số lần gọi AI, ví dụ 5 lần/giờ.
- AI call có timeout để không treo servlet thread pool khi Gemini chậm.
- Kết quả trả về là bản phân tích có cấu trúc:
  - Tóm tắt tổng quan.
  - Điểm mạnh.
  - Vấn đề nổi bật.
  - Mức độ ưu tiên.
  - Khuyến nghị hành động.
  - Lưu ý về dữ liệu chưa đủ hoặc thiên lệch.

### 3.2. Dependency

File cần chỉnh:

- `backend/pom.xml`

Thêm Spring AI BOM vào `dependencyManagement`.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Ghi chú:

- Tại thời điểm lập roadmap, Spring AI release đã có `1.0.4` trên Maven Central.
- Nếu project dùng Spring Boot 4.0.x, cần chạy thử compatibility. Tài liệu Spring AI hiện ghi hỗ trợ Spring Boot 3.4.x/3.5.x. Nếu dependency chưa tương thích Boot 4, có hai lựa chọn:
  - Giữ roadmap này và triển khai khi Spring AI hỗ trợ Boot 4 ổn định.
  - Tách AI adapter thành module/service riêng chạy Boot 3.5.x, backend chính gọi nội bộ.

#### Option A: Gemini Developer API bằng API key

Phù hợp demo, đồ án, triển khai nhanh.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>
```

#### Option B: Vertex AI Gemini

Phù hợp production trên Google Cloud, dùng project/location và Google Cloud auth.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-vertex-ai-gemini</artifactId>
</dependency>
```

Khuyến nghị cho project hiện tại:

- Dùng Option A trước để demo "AI Summary" nhanh.
- Không dùng artifact cũ hoặc tự đoán như `spring-ai-gemini-ai-spring-boot-starter` nếu Maven Central không có.
- Khi production hóa trên GCP, đổi sang Option B bằng cách giữ nguyên `AiAnalysisPort`, chỉ thay adapter/config.

### 3.3. Environment Variables

Với Gemini Developer API:

```env
SPRING_AI_GOOGLE_GENAI_API_KEY=your-gemini-api-key
SPRING_AI_GOOGLE_GENAI_CHAT_OPTIONS_MODEL=gemini-2.0-flash
SPRING_AI_GOOGLE_GENAI_CHAT_OPTIONS_TEMPERATURE=0.2

APP_AI_ANALYSIS_ENABLED=true
APP_AI_ANALYSIS_TIMEOUT=20s
APP_AI_ANALYSIS_MAX_FEEDBACK_ITEMS=200
APP_AI_ANALYSIS_MAX_CHARS=30000
APP_AI_ANALYSIS_RATE_LIMIT_MAX_REQUESTS=5
APP_AI_ANALYSIS_RATE_LIMIT_WINDOW=PT1H
```

Với Vertex AI:

```env
SPRING_AI_VERTEX_AI_GEMINI_PROJECT_ID=your-gcp-project-id
SPRING_AI_VERTEX_AI_GEMINI_LOCATION=us-central1
SPRING_AI_VERTEX_AI_GEMINI_CHAT_OPTIONS_MODEL=gemini-2.0-flash
SPRING_AI_VERTEX_AI_GEMINI_CHAT_OPTIONS_TEMPERATURE=0.2

APP_AI_ANALYSIS_ENABLED=true
APP_AI_ANALYSIS_TIMEOUT=20s
APP_AI_ANALYSIS_MAX_FEEDBACK_ITEMS=200
APP_AI_ANALYSIS_MAX_CHARS=30000
APP_AI_ANALYSIS_RATE_LIMIT_MAX_REQUESTS=5
APP_AI_ANALYSIS_RATE_LIMIT_WINDOW=PT1H
```

Gợi ý cho VyOS/Linux Bridge:

- Backend container cần outbound HTTPS tới Gemini/Google API.
- Nếu network đi qua VyOS firewall/NAT, allow outbound TCP `443` từ backend subnet.
- Không hard-code API key trong image hoặc git.
- Lưu API key trong Portainer secret/env hoặc CI/CD secret.
- Không log prompt đầy đủ nếu prompt chứa phản hồi sinh viên.

### 3.4. Configuration trong `application.yaml`

File cần chỉnh:

- `backend/src/main/resources/application.yaml`

Option A: Google GenAI API key.

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${SPRING_AI_GOOGLE_GENAI_API_KEY:}
        chat:
          options:
            model: ${SPRING_AI_GOOGLE_GENAI_CHAT_OPTIONS_MODEL:gemini-2.0-flash}
            temperature: ${SPRING_AI_GOOGLE_GENAI_CHAT_OPTIONS_TEMPERATURE:0.2}
```

Option B: Vertex AI Gemini.

```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          project-id: ${SPRING_AI_VERTEX_AI_GEMINI_PROJECT_ID:}
          location: ${SPRING_AI_VERTEX_AI_GEMINI_LOCATION:us-central1}
          chat:
            options:
              model: ${SPRING_AI_VERTEX_AI_GEMINI_CHAT_OPTIONS_MODEL:gemini-2.0-flash}
              temperature: ${SPRING_AI_VERTEX_AI_GEMINI_CHAT_OPTIONS_TEMPERATURE:0.2}
```

App-level config:

```yaml
app:
  ai:
    analysis:
      enabled: ${APP_AI_ANALYSIS_ENABLED:false}
      timeout: ${APP_AI_ANALYSIS_TIMEOUT:20s}
      max-feedback-items: ${APP_AI_ANALYSIS_MAX_FEEDBACK_ITEMS:200}
      max-chars: ${APP_AI_ANALYSIS_MAX_CHARS:30000}
      rate-limit:
        max-requests: ${APP_AI_ANALYSIS_RATE_LIMIT_MAX_REQUESTS:5}
        window: ${APP_AI_ANALYSIS_RATE_LIMIT_WINDOW:PT1H}
```

Tại sao cần app-level config:

- `enabled=false` mặc định giúp tránh gọi AI ngoài ý muốn khi thiếu key.
- `timeout` bảo vệ thread pool.
- `max-feedback-items` và `max-chars` kiểm soát token/cost.
- `rate-limit` tách riêng với auth rate limit vì AI đắt hơn nhiều.

### 3.5. AI Analysis Properties

File cần tạo:

- `backend/src/main/java/com/ttcs/backend/config/AiAnalysisProperties.java`

Code:

```java
package com.ttcs.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ai.analysis")
public record AiAnalysisProperties(
        boolean enabled,
        Duration timeout,
        int maxFeedbackItems,
        int maxChars,
        RateLimit rateLimit
) {
    public record RateLimit(
            long maxRequests,
            Duration window
    ) {
    }
}
```

Enable properties:

```java
@EnableConfigurationProperties({
        RateLimitProperties.class,
        AiAnalysisProperties.class
})
```

Nếu `BackendApplication` đã enable `RateLimitProperties` ở Giai đoạn 2, chỉ thêm `AiAnalysisProperties.class` vào danh sách.

### 3.6. Outbound Port: `AiAnalysisPort`

File cần tạo:

- `backend/src/main/java/com/ttcs/backend/application/port/out/AiAnalysisPort.java`

Code:

```java
package com.ttcs.backend.application.port.out;

import java.util.List;

public interface AiAnalysisPort {

    String summarizeSurveyFeedback(AiSurveyAnalysisRequest request);

    record AiSurveyAnalysisRequest(
            String surveyTitle,
            String lecturerName,
            String courseName,
            int totalResponses,
            List<String> feedbackComments
    ) {
    }
}
```

Ranh giới:

- Port chỉ nhận dữ liệu đã được application service chuẩn hóa.
- Port không biết controller, JPA entity, Redis, security principal.
- Port trả `String` để giữ phase đầu đơn giản. Sau này có thể đổi sang structured record nếu frontend cần render từng section riêng.

### 3.7. Outbound Adapter: `GeminiAiAdapter`

File cần tạo:

- `backend/src/main/java/com/ttcs/backend/adapter/out/ai/GeminiAiAdapter.java`

Tạo package:

- `backend/src/main/java/com/ttcs/backend/adapter/out/ai`

Code:

```java
package com.ttcs.backend.adapter.out.ai;

import com.ttcs.backend.application.port.out.AiAnalysisPort;
import com.ttcs.backend.config.AiAnalysisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAiAdapter implements AiAnalysisPort {

    private static final String SYSTEM_PROMPT = """
            You are an academic survey analyst for a university quality assurance team.
            Your job is to analyze student survey feedback and produce a concise, evidence-based summary.

            Rules:
            - Do not behave like a general chatbot.
            - Do not invent facts, numbers, courses, lecturers, or student opinions.
            - Use only the survey data provided in the user message.
            - If the data is insufficient, say so clearly.
            - Do not reveal personal data or identify individual students.
            - Keep the tone professional, neutral, and action-oriented.
            - Write the answer in Vietnamese.

            Required output format:
            1. Tổng quan ngắn gọn
            2. Điểm mạnh được sinh viên ghi nhận
            3. Vấn đề hoặc điểm yếu nổi bật
            4. Mức độ ưu tiên xử lý
            5. Khuyến nghị hành động cho giảng viên/admin
            6. Lưu ý về độ tin cậy của dữ liệu
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final AiAnalysisProperties properties;

    @Override
    public String summarizeSurveyFeedback(AiSurveyAnalysisRequest request) {
        if (!properties.enabled()) {
            throw new IllegalStateException("AI analysis is disabled");
        }

        String userPrompt = buildUserPrompt(request);
        Duration timeout = properties.timeout();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(() -> callGemini(userPrompt));
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            log.error("Gemini AI analysis timed out after {}", timeout, ex);
            throw new IllegalStateException("AI analysis timed out. Please try again later.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI analysis was interrupted", ex);
        } catch (ExecutionException ex) {
            log.error("Gemini AI analysis failed", ex);
            throw new IllegalStateException("AI analysis failed. Please try again later.", ex);
        } finally {
            executor.shutdownNow();
        }
    }

    private String callGemini(String userPrompt) {
        ChatClient chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();

        String content = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty analysis");
        }

        return content.trim();
    }

    private String buildUserPrompt(AiSurveyAnalysisRequest request) {
        List<String> comments = request.feedbackComments().stream()
                .filter(comment -> comment != null && !comment.isBlank())
                .limit(properties.maxFeedbackItems())
                .toList();

        String joinedComments = trimToMaxChars(String.join("\\n- ", comments), properties.maxChars());

        return """
                Analyze the following survey feedback.

                Survey title: %s
                Lecturer: %s
                Course: %s
                Total responses: %d
                Included text comments: %d

                Student comments:
                - %s
                """.formatted(
                safeValue(request.surveyTitle()),
                safeValue(request.lecturerName()),
                safeValue(request.courseName()),
                request.totalResponses(),
                comments.size(),
                joinedComments
        );
    }

    private String trimToMaxChars(String value, int maxChars) {
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "\\n[Truncated because the feedback exceeded the configured limit]";
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        return value;
    }
}
```

Ghi chú về timeout:

- `ChatClient.call()` là blocking call trong servlet app.
- Timeout trong adapter giúp request không giữ thread quá lâu khi Gemini chậm.
- Code trên dùng executor cục bộ để minh họa tutorial. Khi production hóa, nên cấu hình `TaskExecutor` bean riêng cho AI để giới hạn concurrency.

Production-oriented executor:

```java
@Bean
public ExecutorService aiAnalysisExecutor() {
    return Executors.newFixedThreadPool(4);
}
```

Sau đó inject `ExecutorService` vào `GeminiAiAdapter` thay vì tạo executor mỗi request.

### 3.8. Critical Point: Rate limit riêng cho AI bằng `RateLimiterPort`

AI call tốn tiền/token và có latency cao, nên không dùng chung quota với auth endpoint. Dù port `RateLimiterPort` ở Giai đoạn 2 được thiết kế ban đầu cho auth, có thể dùng lại nếu key rõ ràng và adapter đọc config phù hợp.

Khuyến nghị tốt hơn:

- Giữ `RateLimiterPort` generic.
- Mở rộng method để nhận policy cụ thể, hoặc tạo key namespace riêng.

Phiên bản tối thiểu không đổi port:

```java
String aiRateLimitKey = "ai-summary:admin:" + currentUserId;
RateLimiterPort.RateLimitDecision decision = rateLimiterPort.consume(aiRateLimitKey);
```

Nhược điểm:

- Nếu `RedisRateLimiterAdapter` đang hard-code `auth.maxRequests/window`, AI sẽ dùng chung quota auth.

Phiên bản khuyến nghị: mở rộng port.

File cần chỉnh:

- `backend/src/main/java/com/ttcs/backend/application/port/out/RateLimiterPort.java`

Code:

```java
package com.ttcs.backend.application.port.out;

import java.time.Duration;

public interface RateLimiterPort {

    RateLimitDecision consume(String key);

    RateLimitDecision consume(String key, RateLimitPolicy policy);

    record RateLimitPolicy(
            long maxRequests,
            Duration window,
            boolean failOpen
    ) {
    }

    record RateLimitDecision(
            boolean allowed,
            long remaining,
            long retryAfterSeconds
    ) {
        public static RateLimitDecision allowed(long remaining) {
            return new RateLimitDecision(true, remaining, 0);
        }

        public static RateLimitDecision blocked(long retryAfterSeconds) {
            return new RateLimitDecision(false, 0, retryAfterSeconds);
        }
    }
}
```

Trong `RedisRateLimiterAdapter`, method cũ gọi method mới:

```java
@Override
public RateLimitDecision consume(String key) {
    RateLimitProperties.Auth auth = properties.auth();
    return consume(key, new RateLimitPolicy(auth.maxRequests(), auth.window(), auth.failOpen()));
}

@Override
public RateLimitDecision consume(String key, RateLimitPolicy policy) {
    try {
        return consumeWithRedis("rate-limit:" + key, policy);
    } catch (RuntimeException ex) {
        if (policy.failOpen()) {
            log.error("Redis rate limiter failed open for key={}", key, ex);
            return RateLimitDecision.allowed(policy.maxRequests());
        }
        log.error("Redis rate limiter failed closed for key={}", key, ex);
        return RateLimitDecision.blocked(Math.max(1, policy.window().toSeconds()));
    }
}
```

AI service sẽ dùng policy 5 lần/giờ:

```java
RateLimiterPort.RateLimitPolicy policy = new RateLimiterPort.RateLimitPolicy(
        aiAnalysisProperties.rateLimit().maxRequests(),
        aiAnalysisProperties.rateLimit().window(),
        true
);

RateLimiterPort.RateLimitDecision decision = rateLimiterPort.consume(
        "ai-summary:admin:" + currentUserId,
        policy
);

if (!decision.allowed()) {
    throw new TooManyRequestsException("AI summary limit exceeded. Try again later.");
}
```

Khuyến nghị:

- Key nên theo `adminId` hoặc `lecturerId`, không theo IP, vì AI cost gắn với tài khoản người dùng.
- Có thể thêm survey id vào key nếu muốn giới hạn theo từng survey:

```java
"ai-summary:admin:%s:survey:%s".formatted(currentUserId, surveyId)
```

Nhưng với cost control tổng thể, key theo user/giờ là dễ hiểu nhất.

### 3.9. Integration vào Service layer

File dự kiến tạo use case:

- `backend/src/main/java/com/ttcs/backend/application/port/in/result/GenerateSurveyAiSummaryUseCase.java`
- `backend/src/main/java/com/ttcs/backend/application/domain/service/GenerateSurveyAiSummaryService.java`

Use case:

```java
package com.ttcs.backend.application.port.in.result;

public interface GenerateSurveyAiSummaryUseCase {

    String generate(Long surveyId, Long currentUserId);
}
```

Service:

```java
package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.result.GenerateSurveyAiSummaryUseCase;
import com.ttcs.backend.application.port.out.AiAnalysisPort;
import com.ttcs.backend.application.port.out.RateLimiterPort;
import com.ttcs.backend.config.AiAnalysisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateSurveyAiSummaryService implements GenerateSurveyAiSummaryUseCase {

    private final AiAnalysisPort aiAnalysisPort;
    private final RateLimiterPort rateLimiterPort;
    private final AiAnalysisProperties aiAnalysisProperties;

    // Inject existing survey/result ports here instead of repositories directly.
    // private final SurveyResultQueryPort surveyResultQueryPort;
    // private final SurveyAccessPolicy surveyAccessPolicy;

    @Override
    public String generate(Long surveyId, Long currentUserId) {
        enforceAiRateLimit(currentUserId);

        // 1. Load survey result detail through an application/outbound port.
        // 2. Check current user can access this survey result.
        // 3. Extract only text feedback/comments needed for AI analysis.
        // 4. Do not pass student identity, email, phone, document data, or JWT.

        String surveyTitle = "TODO load survey title";
        String lecturerName = "TODO load lecturer name";
        String courseName = "TODO load course name";
        int totalResponses = 0;
        List<String> comments = List.of();

        return aiAnalysisPort.summarizeSurveyFeedback(
                new AiAnalysisPort.AiSurveyAnalysisRequest(
                        surveyTitle,
                        lecturerName,
                        courseName,
                        totalResponses,
                        comments
                )
        );
    }

    private void enforceAiRateLimit(Long currentUserId) {
        RateLimiterPort.RateLimitPolicy policy = new RateLimiterPort.RateLimitPolicy(
                aiAnalysisProperties.rateLimit().maxRequests(),
                aiAnalysisProperties.rateLimit().window(),
                true
        );

        RateLimiterPort.RateLimitDecision decision = rateLimiterPort.consume(
                "ai-summary:user:" + currentUserId,
                policy
        );

        if (!decision.allowed()) {
            throw new IllegalStateException(
                    "AI summary limit exceeded. Retry after %d seconds.".formatted(decision.retryAfterSeconds())
            );
        }
    }
}
```

Điểm quan trọng:

- Service layer không gọi `ChatClient` trực tiếp.
- Service layer không gọi Redis trực tiếp.
- Controller không build prompt.
- Adapter không truy vấn database.
- Dữ liệu đưa vào AI phải được lọc PII trước.

### 3.10. Inbound Controller cho nút "AI Summary"

File dự kiến:

- `backend/src/main/java/com/ttcs/backend/adapter/in/web/SurveyAiAnalysisController.java`

Code:

```java
package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.SurveyAiSummaryResponse;
import com.ttcs.backend.application.port.in.result.GenerateSurveyAiSummaryUseCase;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequestMapping("/api/v1/survey-results")
@RequiredArgsConstructor
public class SurveyAiAnalysisController {

    private final GenerateSurveyAiSummaryUseCase generateSurveyAiSummaryUseCase;
    private final CurrentIdentityProvider currentIdentityProvider;

    @PostMapping("/{surveyId}/ai-summary")
    public ResponseEntity<SurveyAiSummaryResponse> generate(@PathVariable Long surveyId) {
        String summary = generateSurveyAiSummaryUseCase.generate(
                surveyId,
                currentIdentityProvider.currentUserId()
        );

        return ResponseEntity.ok(new SurveyAiSummaryResponse(summary));
    }
}
```

DTO:

```java
package com.ttcs.backend.adapter.in.web.dto;

public record SurveyAiSummaryResponse(String summary) {
}
```

Security:

- Endpoint này nên dùng rule hiện có cho survey results:

```java
.requestMatchers("/api/v1/survey-results/**").hasAnyRole("ADMIN", "LECTURER")
```

Vẫn cần kiểm tra scope trong service:

- Admin xem được toàn bộ.
- Lecturer chỉ xem survey/result thuộc phạm vi được phân quyền.

### 3.11. Frontend integration gợi ý

Phần này chỉ là hướng dẫn nối nút, không phải trọng tâm backend.

API call:

```ts
export async function generateSurveyAiSummary(surveyId: number) {
  const response = await api.post<{ summary: string }>(
    `/api/v1/survey-results/${surveyId}/ai-summary`
  );
  return response.data;
}
```

UI behavior:

- Nút: `AI Summary`.
- Disable button khi loading.
- Nếu backend trả limit exceeded, hiển thị message "Bạn đã vượt giới hạn phân tích AI. Vui lòng thử lại sau."
- Không cho user nhập prompt tự do.
- Render summary trong panel hoặc modal cạnh survey result.

### 3.12. Safety checklist cho dữ liệu AI

Trước khi gọi `AiAnalysisPort`, service phải đảm bảo:

- [ ] Không gửi tên sinh viên.
- [ ] Không gửi email, phone, student code.
- [ ] Không gửi document URL hoặc MinIO object key.
- [ ] Không gửi JWT/access token.
- [ ] Không gửi raw audit log.
- [ ] Cắt số lượng phản hồi theo `max-feedback-items`.
- [ ] Cắt tổng số ký tự theo `max-chars`.
- [ ] Prompt yêu cầu AI không bịa số liệu.
- [ ] Prompt yêu cầu AI nói rõ khi dữ liệu không đủ.
- [ ] Rate limit theo user trước khi gọi AI.
- [ ] Timeout AI call.

### 3.13. Verify

#### Verify config

Kiểm tra backend start không lỗi:

```bash
cd backend
./mvnw spring-boot:run
```

Nếu thiếu API key và `APP_AI_ANALYSIS_ENABLED=false`, app vẫn nên start bình thường.

Nếu bật AI:

```env
APP_AI_ANALYSIS_ENABLED=true
SPRING_AI_GOOGLE_GENAI_API_KEY=your-key
```

#### Verify endpoint

Gọi endpoint bằng token Admin/Giảng viên:

```bash
curl -i -X POST http://localhost:8080/api/v1/survey-results/1/ai-summary \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Kỳ vọng:

```json
{
  "summary": "1. Tổng quan ngắn gọn\n..."
}
```

#### Verify rate limit AI

Gọi quá 5 lần trong 1 giờ với cùng user:

```powershell
1..6 | ForEach-Object {
  curl.exe -i -X POST http://localhost:8080/api/v1/survey-results/1/ai-summary `
    -H "Authorization: Bearer <ACCESS_TOKEN>"
}
```

Kỳ vọng lần thứ 6:

- Backend không gọi Gemini.
- Service trả lỗi limit exceeded.
- Có thể map lỗi thành HTTP `429` trong `GlobalExceptionHandler`.

Gợi ý exception riêng:

```java
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
```

Trong `GlobalExceptionHandler`, map sang:

```java
@ExceptionHandler(TooManyRequestsException.class)
public ResponseEntity<ApiErrorResponse> handleTooManyRequests(TooManyRequestsException ex) {
    return ResponseEntity.status(429).body(new ApiErrorResponse("RATE_LIMITED", ex.getMessage()));
}
```

#### Verify timeout

Set timeout thấp trong môi trường test:

```env
APP_AI_ANALYSIS_TIMEOUT=1ms
```

Gọi endpoint:

```bash
curl -i -X POST http://localhost:8080/api/v1/survey-results/1/ai-summary \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Kỳ vọng:

- Request kết thúc với lỗi có kiểm soát.
- Backend không treo lâu.
- Log JSON có message timeout.

#### Verify Portainer logs

Trong Portainer logs, kiểm tra:

- Có log lỗi timeout nếu test timeout.
- Không có API key.
- Không có full prompt chứa dữ liệu sinh viên nhạy cảm.
- Không log token.

### 3.14. Checklist Giai đoạn 3

- [ ] Chọn provider: Google GenAI API key hoặc Vertex AI Gemini.
- [ ] Thêm Spring AI BOM.
- [ ] Thêm starter `spring-ai-starter-model-google-genai` hoặc `spring-ai-starter-model-vertex-ai-gemini`.
- [ ] Thêm env Gemini/Vertex AI vào Portainer stack.
- [ ] Thêm `app.ai.analysis.*` vào `application.yaml`.
- [ ] Tạo `AiAnalysisProperties`.
- [ ] Enable `AiAnalysisProperties`.
- [ ] Tạo `AiAnalysisPort`.
- [ ] Tạo `GeminiAiAdapter`.
- [ ] Viết system prompt chuyên biệt cho survey analysis.
- [ ] Tích hợp `RateLimiterPort` cho AI quota 5 lần/giờ/user.
- [ ] Tạo use case `GenerateSurveyAiSummaryUseCase`.
- [ ] Tạo service `GenerateSurveyAiSummaryService`.
- [ ] Tạo controller endpoint `POST /api/v1/survey-results/{surveyId}/ai-summary`.
- [ ] Kiểm tra phân quyền Admin/Lecturer.
- [ ] Lọc PII trước khi gọi AI.
- [ ] Cấu hình timeout AI call.
- [ ] Verify endpoint trả summary.
- [ ] Verify rate limit AI.
- [ ] Verify timeout.
- [ ] Verify logs không lộ secret/PII.

## Checklist tổng kết Giai đoạn 1-3

| Giai đoạn | Hạng mục | File chính | Trạng thái khi hoàn tất |
|---|---|---|---|
| 1 | Thêm Actuator dependency | `backend/pom.xml` | Backend có actuator runtime |
| 1 | Cấu hình health/metrics | `backend/src/main/resources/application.yaml` | `/actuator/health` hoạt động |
| 1 | Mở health endpoint trong Security | `SecurityConfig.java` | Portainer healthcheck không bị 401/403 |
| 1 | Thêm JSON logging | `backend/src/main/resources/logback-spring.xml` | Log stdout là JSON |
| 1 | Tắt SQL console noisy | `application.yaml` | Không spam SQL raw trong Portainer logs |
| 1 | Verify Portainer health/logs | Portainer stack/container | Backend healthy, log có field chuẩn |
| 2 | Thêm Redis dependency | `backend/pom.xml` | Có `StringRedisTemplate` |
| 2 | Cấu hình Redis env | Portainer stack/env | Backend gọi Redis qua hostname/IP nội bộ |
| 2 | Thêm Redis config | `application.yaml` | `spring.data.redis.*` hoạt động |
| 2 | Thêm rate limit config | `application.yaml` | Có quota auth configurable |
| 2 | Tạo rate limit properties | `RateLimitProperties.java` | Config bind được |
| 2 | Tạo outbound port | `RateLimiterPort.java` | Application không phụ thuộc Redis |
| 2 | Tạo Redis adapter | `RedisRateLimiterAdapter.java` | Counter dùng `increment` + `expire` |
| 2 | Tạo inbound filter | `AuthRateLimitingFilter.java` | `/api/auth/**` bị rate limit |
| 2 | Gắn filter trước JWT | `SecurityConfig.java` | Public auth endpoint được bảo vệ trước auth processing |
| 2 | Verify fail-open | Redis/Portainer | Redis down không làm login/register ngừng hoạt động |
| 3 | Chọn Gemini provider | `pom.xml`, env | Dùng Google GenAI hoặc Vertex AI |
| 3 | Thêm Spring AI BOM/starter | `backend/pom.xml` | Có `ChatClient.Builder` |
| 3 | Cấu hình AI env | Portainer stack/env | API key/project không nằm trong git |
| 3 | Thêm AI analysis config | `application.yaml` | Timeout, max chars, quota configurable |
| 3 | Tạo AI properties | `AiAnalysisProperties.java` | Config bind được |
| 3 | Tạo AI outbound port | `AiAnalysisPort.java` | Application không phụ thuộc Gemini |
| 3 | Tạo Gemini adapter | `GeminiAiAdapter.java` | Gọi Gemini qua `ChatClient` |
| 3 | Tạo AI use case/service | `GenerateSurveyAiSummaryUseCase`, `GenerateSurveyAiSummaryService` | Service kiểm tra quyền, rate limit, lọc dữ liệu |
| 3 | Dùng `RateLimiterPort` cho AI quota | Service layer | 5 lần/giờ/user, bảo vệ chi phí token |
| 3 | Tạo endpoint AI Summary | `SurveyAiAnalysisController.java` | Admin/Giảng viên gọi được từ dashboard |
| 3 | Verify safety | Logs/API response | Không lộ PII, token, API key |
| 3 | Verify timeout/rate limit | Curl/Portainer logs | Không treo thread pool, quá quota bị chặn |
