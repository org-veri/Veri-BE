# Plan: DDD/Modulith Resequenced Execution

**Status**: In Progress
**Date**: 2025-12-30
**Goal**: Reflect the updated execution order for the DDD + Modulith plan without modifying the inspiration doc.

**Parent Task**:
```
.agents/inspiration/ddd.md
```

## Phase 1: Spring Modulith Foundations (Reordered)
- [x] **Define modules** via `package-info.java` and close core domains.
- [x] **Declare named interfaces** for cross-module access boundaries.
- [x] **Align allowed dependencies** across **auth/book/card/comment/image/member/post/global/lib**.
- [x] **Run Modulith architecture verification** (GREEN).
- [x] **Generate Modulith documentation** artifacts for baseline.
- [x] **Record verification command** for repeatable runs.

## Phase 2: Storage/Module Layout Alignment
- [x] **Merge storage module** into **core** (`storage:db-core` removal).
- [x] **Move entities/repositories** into **core** source tree.
- [x] **Update Gradle settings** to drop storage module references.
- [x] **Restore persistence dependencies** in **core** where required.
- [x] **Align repository packages** with service packages.
- [x] **Enforce package-private visibility** for internal repositories.
- [x] **Route cross-module access** through services instead of repositories.

## Phase 3: Domain Refactor (DDD Core)
- [ ] **Reconcile domain model layout** (aggregate roots, entities, VOs).
- [ ] **Define domain repositories** in module-aligned packages.
- [ ] **Split domain services** into **Command/Query** where applicable.
- [ ] **Extract domain events** and publish through application services.
- [ ] **Remove legacy layered package remnants** (if any remain).

## Phase 4: CQRS + FluentQuery
- [ ] **Identify query hotspots** for FluentQuery migration.
- [ ] **Replace QueryDSL usage** with FluentQuery repositories.
- [ ] **Implement query DTO projections** for read models.
- [ ] **Introduce QueryService layer** per module.
- [ ] **Ensure command/query separation** in controllers/services.

## Phase 5: R/W Pool Separation
- [ ] **Add read/write data sources** and routing configuration.
- [ ] **Mark read paths** for replica routing.
- [ ] **Validate transaction routing** in integration tests.
- [ ] **Verify connection pool metrics** under baseline load.

## Phase 6: Performance Verification
- [ ] **Run load tests** for query performance.
- [ ] **Capture P95 latency** per critical read endpoint.
- [ ] **Compare FluentQuery vs baseline** results.
- [ ] **Document performance deltas** and rollback criteria.

## History
- **2025-12-30**: Plan created to reflect reordered execution sequence.
- **2025-12-30**: **Phase 1-2 updated** with completed Modulith, storage integration, and visibility enforcement.
  - **Verification Command**:
    ```
    JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./gradlew :tests:test --tests "org.veri.be.modulith.ModulithArchitectureTest"
    ```
  - **Documentation Command**:
    ```
    JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./gradlew :tests:test --tests "org.veri.be.modulith.ModulithDocumentationTest"
    ```
- **2025-12-30**: **Phase 3 started** with **book command/query service split**.
  - **Updated Files**:
    ```
    core/core-api/src/main/java/org/veri/be/book/service/BookCommandService.java
    core/core-api/src/main/java/org/veri/be/book/service/BookQueryService.java
    core/core-api/src/main/java/org/veri/be/post/service/PostCommandService.java
    core/core-api/src/main/java/org/veri/be/book/BookshelfController.java
    tests/src/test/java/org/veri/be/unit/book/BookCommandServiceTest.java
    tests/src/test/java/org/veri/be/unit/book/BookQueryServiceTest.java
    tests/src/test/java/org/veri/be/unit/post/PostCommandServiceTest.java
    tests/src/test/java/org/veri/be/slice/web/BookshelfControllerTest.java
    tests/src/test/java/org/veri/be/integration/usecase/SocialReadingIntegrationTest.java
    tests/src/test/java/org/veri/be/integration/usecase/CommentIntegrationTest.java
    tests/src/test/java/org/veri/be/integration/usecase/SocialCardIntegrationTest.java
    tests/src/test/java/org/veri/be/integration/usecase/PostIntegrationTest.java
    ```
