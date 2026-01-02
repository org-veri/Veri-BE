# Plan: Gradle Multi-Module - Observability Module

**Status**: Completed
**Date**: 2025-02-14
**Goal**: Split observability concerns into a dedicated Gradle submodule as the first step of multi-module migration.

## Phase 1: Scope & Structure
- [x] Confirm module name and directory layout.
- [x] Define which files move into the observability module (configs/resources/classes).
- [x] Decide how shared resources (logback, application config) are packaged.

## Phase 2: Gradle Setup
- [x] Convert root project to multi-module (`settings.gradle` + root `build.gradle.kts`).
- [x] Create observability submodule `build.gradle.kts` with needed dependencies.
- [x] Wire the application module to depend on the observability module.

## Phase 3: Code & Resource Move
- [x] Move **OpenTelemetryLogbackConfig** to the observability module.
- [ ] Move observability test controller (if it should live there).
- [x] Move or merge resource files (`logback-spring.xml`, observability settings).

## Phase 4: Verification
- [x] Run local profile and verify Loki/Tempo delivery.

## History
2025-02-14 14:40 KST - Created **support** module, moved **OpenTelemetryLogbackConfig**, and split Gradle configuration into **app** and **support** modules. Kept **logback-spring.xml** and application YAML in **app**. Modified files: **settings.gradle**, **build.gradle.kts**, **app/build.gradle.kts**, **support/build.gradle.kts**, **support/src/main/java/org/veri/be/global/config/OpenTelemetryLogbackConfig.java**.
2025-02-14 14:32 KST - Fixed root Gradle plugin setup and added Spring Boot BOM import for subprojects to resolve dependency versions. Verified local profile and confirmed Loki/Tempo data delivery. Modified files: **build.gradle.kts**.
2025-02-14 14:44 KST - Split **support** into **support:logging** and **support:monitoring** modules with resource files **logging.yml** and **monitoring.yml**. Moved **logback-spring.xml** into the logging module and wired app to import shared configs. Modified files: **settings.gradle**, **app/build.gradle.kts**, **app/src/main/resources/application.yml**, **app/src/main/resources/application-local.yml**, **support/logging/build.gradle.kts**, **support/monitoring/build.gradle.kts**, **support/logging/src/main/resources/logback/logback-spring.xml**, **support/logging/src/main/resources/logging.yml**, **support/monitoring/src/main/resources/monitoring.yml**.
2025-02-14 14:48 KST - Corrected OTLP metrics configuration to use **url** and added support for **OTLP_ENDPOINT** overrides in **monitoring.yml**. Verified Gradle build, Docker build/run, and Loki/Tempo delivery with container runtime. Modified files: **support/monitoring/src/main/resources/monitoring.yml**, **app/src/main/resources/application.yml**, **support/logging/build.gradle.kts**, **build.gradle.kts**, **Dockerfile**, **makefile**, **settings.gradle**, **app/build.gradle.kts**.


## Review
**Source File**:
```
.agents/review/gradle-multimodule-observability-2025-02-14.md
```

### Review Content

# Delivery Review - 2025-02-14

**Reviewer**: Codex
**Scope**: Gradle multi-module split with support logging/monitoring and container verification

## Summary
Converted the project into **app** and **support** Gradle modules, split support into **logging** and **monitoring**, and moved observability resources/configuration into support. Docker build/run and LGTM delivery were verified using the container runtime.

## Findings
- **Gradle**: `:app:build -x test` succeeds with multi-module setup.
- **Docker**: `Dockerfile` builds using `app/build/libs/*.jar` and container starts with **local** profile.
- **Observability**: Collector accepts log records/spans; Loki series and Tempo traces are present after hitting test endpoints.
- **Config**: OTLP metrics now use **management.otlp.metrics.export.url** and support **OTLP_ENDPOINT** overrides.

## Action Items
- None.
