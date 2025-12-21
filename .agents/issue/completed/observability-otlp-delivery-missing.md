# Issue: OTLP Logs/Traces Not Reaching LGTM

**Severity**: High
**Status**: Closed
**Date**: 2025-02-14

## Description
OTLP metrics are reaching the LGTM stack, but OTLP logs and traces are not. Loki has no series, and Tempo returns no traces, even though local logs show **traceId** and **spanId** values for requests.

## Affected Files
```
src/main/resources/application.yml
src/main/resources/application-local.yml
src/main/resources/logback-spring.xml
```

## Findings
- **OTLP Collector Metrics**: `otelcol_receiver_accepted_metric_points` increments, while **spans/logs** counters are absent.
- **Loki**: `loki/api/v1/series` returns an empty set for the last 10 minutes.
- **Tempo**: `api/search` and `api/traces/{traceId}` return no results for recent trace IDs.
- **Appender Install**: The OpenTelemetry Logback appender is configured in **logback-spring.xml**, but the **InstallOpenTelemetryAppender** initialization used in **otel-ref** is not present in the main app.

## Recommendation
- Add an OpenTelemetry configuration class that calls **OpenTelemetryAppender.install(OpenTelemetry)**, following **otel-ref/shared/OpenTelemetryConfiguration.java**.
- Verify OTLP exporter settings for logs and traces (for example, ensure the logs exporter is enabled with `otel.logs.exporter=otlp` if required).
- Re-run local profile tests and confirm collector metrics include accepted spans/logs, and that Loki/Tempo return data for recent requests.

## Resolution
- Added **OpenTelemetryLogbackConfig** to install the appender.
- Switched OTLP tracing/logging configuration to Spring Boot auto-config keys under **management.opentelemetry**.
- Verified collector accepts log records/spans and Loki/Tempo contain data for test endpoints.
