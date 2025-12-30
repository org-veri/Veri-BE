# Plan: Kotlin Migration Phase 2 Test Foundations

**Status**: In Progress
**Date**: 2025-12-30
**Goal**: Introduce Kotlin-friendly testing dependencies and structure to enable new tests in Kotlin.

**Parent Task**:
```
.agents/work/backlog/kotlin-migration.md
```

## Phase 1: Test Dependencies
- [x] **Select test stack** (JUnit 5 + MockK).
- [x] **Add Kotlin test dependencies** to the tests module.

## Phase 2: Structure
- [x] **Create Kotlin test source root** and migrate a low-risk test when ready.

## Phase 3: Verification
- [ ] **Run test suite** after a full phase is complete.

## History
- **2025-12-30**: **Phase 1 updates**. Added Kotlin test dependencies for MockK and Kotlin test support.
  - **Modified Files**:
    ```
    tests/build.gradle.kts
    ```
- **2025-12-30**: **Phase 2 updates**. Migrated a low-risk unit test to Kotlin and created the Kotlin test source root.
  - **Modified Files**:
    ```
    tests/src/test/kotlin/org/veri/be/unit/book/ReadingDetailResponseTest.kt
    tests/src/test/java/org/veri/be/unit/book/ReadingDetailResponseTest.java
    ```
