# Plan: Observability with Grafana LGTM & OpenTelemetry

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Implement comprehensive logging and distributed tracing using the Grafana LGTM stack (Loki, Grafana, Tempo) and Spring Boot 4's OpenTelemetry support.

## 1. Architecture Overview

We will adopt a hybrid push model:
*   **Traces**: App -> OpenTelemetry (OTLP) -> **Tempo**
*   **Logs**: App -> Logback (Loki Appender) -> **Loki**
*   **Metrics**: App -> Prometheus (Existing) -> **Mimir** (Optional/Future) or Grafana (via Prometheus DataSource)
*   **Visualization**: **Grafana** (Correlating Logs and Traces via `TraceID`)

## 2. Dependencies (Spring Boot 4 / 3.x+)

Update `build.gradle.kts` to include OpenTelemetry and Loki support.

```kotlin
dependencies {
    // OpenTelemetry Tracing (Replaces Brave if present)
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // Loki Log Appender (Direct push to Loki)
    implementation("com.github.loki4j:loki-logback-appender:1.5.1")
}
```

## 3. Application Configuration

Configure `application.yml` to enable OTel export and log formatting.

```yaml
management:
  tracing:
    sampling:
      probability: 1.0 # 100% sampling for Dev/Test
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces # Send traces to Tempo (HTTP)

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]" # Add TraceID to console logs
```

Create/Update `logback-spring.xml` for Loki Appender:
```xml
<configuration>
    <appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
        <http>
            <url>http://localhost:3100/loki/api/v1/push</url>
        </http>
        <format>
            <label>
                <pattern>app=${appName},host=${HOSTNAME},level=%level</pattern>
            </label>
            <message>
                <pattern>l=%level c=%logger{20} t=%thread | %msg %ex</pattern>
            </message>
            <sortByTime>true</sortByTime>
        </format>
    </appender>

    <root level="INFO">
        <appender-ref ref="LOKI" />
    </root>
</configuration>
```

## 4. Infrastructure (Docker Compose)

Update `deploy/docker-compose.yml` to include the LGTM stack.

```yaml
services:
  # ... existing services ...

  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml

  tempo:
    image: grafana/tempo:latest
    ports:
      - "4317:4317" # OTLP gRPC
      - "4318:4318" # OTLP HTTP
      - "3200:3200" # Tempo HTTP
    command: -config.file=/etc/tempo.yaml
    volumes:
      - ./config/tempo.yaml:/etc/tempo.yaml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_AUTH_ANONYMOUS_ENABLED=true
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Admin
    volumes:
      - ./config/grafana-datasources.yaml:/etc/grafana/provisioning/datasources/datasources.yaml
    depends_on:
      - loki
      - tempo
```

**Required Config Files**:
*   `deploy/config/tempo.yaml`: Minimal Tempo config (receivers: [otlp]).
*   `deploy/config/grafana-datasources.yaml`: Pre-configure Loki and Tempo data sources.
    *   *Crucial*: Configure Tempo's "Trace to logs" setting to link back to Loki using the `traceId`.

## 5. Verification Steps
1.  **Start Stack**: `docker-compose up -d`
2.  **Generate Traffic**: Hit the API endpoints.
3.  **Check Grafana**:
    *   Open `http://localhost:3000`.
    *   Explore -> Tempo -> Find a trace.
    *   **Key Success Metric**: Click a span in the trace view and see a "Logs" button (or split view) that queries Loki for that specific `traceId`.

## History
- **2025-12-21**: Moved plan into **.agents/work** structure.
- **2025-02-14**: Observability delivery verified and integrated. Updated **application-local.yml** and **application.yml** to use Spring Boot OTLP auto-configuration, added **OpenTelemetryLogbackConfig**, and confirmed delivery to **Loki** and **Tempo**. Related work files:
```
.agents/work/completed/observability-lgtm-verify.md
.agents/review/observability-lgtm-delivery-2025-02-14.md
.agents/issue/completed/observability-otlp-delivery-missing.md
```
