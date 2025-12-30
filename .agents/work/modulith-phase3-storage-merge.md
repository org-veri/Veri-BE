# Plan: Modulith Phase 3 Storage Merge

**Status**: In Progress
**Date**: 2025-12-30
**Goal**: Remove the **storage** module by integrating repository/entity packages into **core** so package-private visibility can be enforced.

**Parent Task**:
```
.agents/work/backlog/modulith-migration.md
```

## Phase 1: Preparation
- [x] **Map storage packages** (`storage/db-core`) to their destination packages under **core**.
- [x] **Enumerate cross-module usage** to avoid breaking dependencies during the move.
- [x] **Update module boundaries plan** for the new package layout.

## Phase 2: Migration
- [x] **Move repository/entity classes** into core module packages.
- [x] **Update imports/build scripts** to remove `storage` module references.
- [ ] **Adjust module declarations** for new package locations.

## Phase 3: Visibility Enforcement
- [x] **Align auth token storage package** to co-locate repositories and service for future package-private access.
- [ ] **Demote internal visibility** (repositories/services/DTOs) to package-private where safe.
- [ ] **Re-run Modulith verification** after visibility changes.

## History
 - **2025-12-30**: **Plan created**. Storage module integration planned to unblock visibility enforcement.
 - **2025-12-30**: **Phase 1 complete**. Prepared storage-to-core mapping and dependencies audit.
 - **2025-12-30**: **Phase 2 partial**. Moved storage entities/repositories and removed storage module from Gradle/settings.
   - **Modified Files**:
     ```
     settings.gradle.kts
     core/core-api/build.gradle.kts
     tests/build.gradle.kts
     core/core-api/src/main/java/org/veri/be/**/entity/**
     core/core-api/src/main/java/org/veri/be/**/repository/**
     core/core-api/src/main/resources/storage.yml
     core/core-api/src/main/resources/storage-local.yml
     ```
 - **2025-12-30**: **Auth storage package aligned** to colocate token repositories and service.
   - **Modified Files**:
     ```
     core/core-api/src/main/java/org/veri/be/auth/storage/TokenStorageService.java
     core/core-api/src/main/java/org/veri/be/auth/storage/BlacklistedTokenRepository.java
     core/core-api/src/main/java/org/veri/be/auth/storage/RefreshTokenRepository.java
     core/core-api/src/main/java/org/veri/be/auth/service/AuthService.java
     tests/src/test/java/org/veri/be/integration/usecase/AuthIntegrationTest.java
     tests/src/test/java/org/veri/be/unit/auth/AuthServiceTest.java
     tests/src/test/java/org/veri/be/unit/auth/TokenStorageServiceTest.java
     ```
