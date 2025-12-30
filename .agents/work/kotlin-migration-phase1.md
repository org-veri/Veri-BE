# Plan: Kotlin Migration Phase 1 Setup

**Status**: In Progress
**Date**: 2025-12-30
**Goal**: Add Kotlin build tooling and baseline dependencies while keeping Java modules intact.

**Parent Task**:
```
.agents/work/backlog/kotlin-migration.md
```

## Phase 1: Gradle & Dependencies
- [ ] **Inspect Gradle layout** to determine where Kotlin plugins should be applied.
- [ ] **Add Kotlin plugins** (`kotlin("jvm")`, `kotlin("plugin.spring")`, `kotlin("plugin.jpa")`) in the appropriate modules.
- [ ] **Add Kotlin dependencies** (`jackson-module-kotlin`, `kotlin-reflect`).
- [ ] **Set JVM target** to align with runtime and existing Java toolchain.

## Phase 2: Verification
- [ ] **Build** or **Modulith test** after changes (run once the chapter completes).

## History
