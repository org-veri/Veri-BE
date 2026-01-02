# Plan: Fix core-app bootJar mainClass

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Configure bootJar mainClass for core-app to avoid build failure.

## Steps
- [x] Set explicit mainClass for Spring Boot in **core-app** build
- [x] Verify build task no longer fails

## History
2026-01-02 23:20 | Fix core-app bootJar mainClass | Set explicit Spring Boot mainClass and validated bootJar task. | Modified files: `core/core-app/build.gradle.kts`

## Review
**Summary**
- Added explicit **mainClass** to **core-app** to satisfy bootJar resolution.

**Findings**
- None.

**Action Items**
- None.
