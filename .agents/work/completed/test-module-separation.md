# Plan: Gradle Test Module Separation

**Status**: Completed
**Date**: 2025-02-14
**Goal**: Separate test code into dedicated Gradle module(s) and include JaCoCo coverage in the new structure.

**Parent Task**:
```
.agents/work/backlog/test-separation.md
```

## Phase 1: Scope & Design
- [x] Confirm test module name(s) and ownership boundaries.
- [x] Decide whether to use a dedicated test module or keep app tests with new source sets.
- [x] Define how JaCoCo aggregates coverage across modules.

## Phase 2: Gradle Setup
- [x] Add test module(s) to `settings.gradle` and create module `build.gradle.kts` files.
- [x] Configure dependencies from test module(s) to `:app` (and test fixtures if used).
- [x] Wire JaCoCo tasks in the appropriate module(s) and root aggregation if required.

## Phase 3: Code Migration
- [x] Move tests into the new module structure.
- [x] Update package paths or resources if necessary.
- [x] Ensure test resources map to the correct module.

## Phase 4: Verification
- [x] Run unit/integration test tasks.
- [x] Generate JaCoCo reports and confirm output paths.

## History
2025-02-14 15:06 KST - Created **tests** module, moved test sources/resources from **app**, updated app dependencies, and configured JaCoCo to report against **app** classes. Ran **:tests:test** and **:tests:jacocoTestReport**. Modified files: **settings.gradle**, **app/build.gradle.kts**, **tests/build.gradle.kts**, **tests/src/test/**.
