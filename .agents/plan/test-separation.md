# Plan: Test Module Separation

**Status**: Draft
**Date**: 2025-12-21
**Goal**: Physically separate Unit Tests from Integration Tests and introduce shared test fixtures using Gradle SourceSets.

## Phase 1: Separate Integration Test SourceSet
Currently, all tests reside in `src/test`. We want to run Unit tests separately from Database/Container-heavy tests.

- [ ] **Gradle Configuration**:
    - Modify `build.gradle.kts` to define a new `testSourceSet` named `integrationTest`.
    - Configure the `integrationTest` task to run only tests in `src/integrationTest`.
    - Ensure `check` task depends on `integrationTest` (or keep them separate for CI optimization).
- [ ] **Migration**:
    - Move `src/test/java/org/veri/be/integration` -> `src/integrationTest/java/org/veri/be/integration`.
    - Move related resources (`application-persistence.yml`) if they are specific to integration tests.
- [ ] **CI/CD Optimization**:
    - Configure Pipeline: Run `./gradlew test` (Unit) on every commit.
    - Configure Pipeline: Run `./gradlew integrationTest` only on PR merges or Nightly builds.

## Phase 2: Shared Test Fixtures
Common test code (Object Mothers, Builders, Helper methods) often gets duplicated or tangled.

- [ ] **Apply Plugin**: Add `java-test-fixtures` plugin to `build.gradle.kts`.
- [ ] **Refactoring**:
    - Move reusable classes from `src/test/java/org/veri/be/support` -> `src/testFixtures/java/org/veri/be/support`.
    - Example candidates: `IntegrationTestSupport`, `SharedTestConfig`, `Fixture` classes.
- [ ] **Usage**:
    - Update dependencies:
        ```kotlin
        testImplementation(testFixtures(project(":"))) // Access in Unit Tests
        integrationTestImplementation(testFixtures(project(":"))) // Access in Integration Tests
        ```

## Phase 3: Slice Test Organization
- [ ] **Decision**: Decide whether "Slice Tests" (Controller Tests) belong to `test` (Unit-like speed) or `integrationTest` (Spring Context loading).
    - *Recommendation*: Keep `@WebMvcTest` in `test` (Unit) as they are relatively fast and mocked.
