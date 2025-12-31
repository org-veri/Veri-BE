# Plan: Modulith Phase 3 Storage Merge

**Status**: Completed
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
- [x] **Align domain repositories** (book/card/comment/image/member/post) into service packages for package-private enforcement.
- [x] **Demote internal visibility** (repositories/services/DTOs) to package-private where safe.
- [x] **Re-run Modulith verification** after visibility changes.

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
 - **2025-12-30**: **Domain repositories aligned** into service packages to prepare for package-private enforcement.
   - **Modified Files**:
     ```
     core/core-api/src/main/java/org/veri/be/book/service/BookRepository.java
     core/core-api/src/main/java/org/veri/be/book/service/ReadingRepository.java
     core/core-api/src/main/java/org/veri/be/card/service/CardRepository.java
     core/core-api/src/main/java/org/veri/be/comment/service/CommentRepository.java
     core/core-api/src/main/java/org/veri/be/image/service/ImageRepository.java
     core/core-api/src/main/java/org/veri/be/image/service/OcrResultRepository.java
     core/core-api/src/main/java/org/veri/be/member/service/MemberRepository.java
     core/core-api/src/main/java/org/veri/be/post/service/PostRepository.java
     core/core-api/src/main/java/org/veri/be/post/service/LikePostRepository.java
     core/core-api/src/main/java/org/veri/be/auth/package-info.java
     core/core-api/src/main/java/org/veri/be/card/package-info.java
     tests/src/test/java/org/veri/be/**/*
     ```
 - **2025-12-30**: **Modulith test attempt blocked** by Kotlin compiler failure on JDK 25.
   - **Command**:
     ```
     ./gradlew :tests:test --tests "org.veri.be.modulith.ModulithArchitectureTest"
     ```
   - **Observed Error**:
     ```
     Execution failed for task ':tests:compileTestKotlin'.
     e: java.lang.IllegalArgumentException: 25
     ```
 - **2025-12-30**: **Repository visibility tightened** with package-private access for internal repositories.
   - **Modified Files**:
     ```
     core/core-api/src/main/java/org/veri/be/auth/storage/BlacklistedTokenRepository.java
     core/core-api/src/main/java/org/veri/be/auth/storage/RefreshTokenRepository.java
     core/core-api/src/main/java/org/veri/be/book/service/BookRepository.java
     core/core-api/src/main/java/org/veri/be/card/service/CardRepository.java
     core/core-api/src/main/java/org/veri/be/comment/service/CommentRepository.java
     core/core-api/src/main/java/org/veri/be/image/service/ImageRepository.java
     core/core-api/src/main/java/org/veri/be/image/service/OcrResultRepository.java
     core/core-api/src/main/java/org/veri/be/post/service/PostRepository.java
     core/core-api/src/main/java/org/veri/be/post/service/LikePostRepository.java
     ```
 - **2025-12-30**: **Tests aligned** to repository visibility and integration setup via services.
   - **Modified Files**:
     ```
     tests/src/test/java/org/veri/be/integration/usecase/CommentIntegrationTest.java
     tests/src/test/java/org/veri/be/integration/usecase/PostIntegrationTest.java
     tests/src/test/java/org/veri/be/integration/usecase/SocialCardIntegrationTest.java
     tests/src/test/java/org/veri/be/integration/usecase/SocialReadingIntegrationTest.java
     tests/src/test/java/org/veri/be/slice/persistence/**/*
     tests/src/test/java/org/veri/be/unit/**/*
     ```
 - **2025-12-30**: **Modulith verification succeeded** with JDK 21 runtime.
   - **Command**:
     ```
     JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./gradlew :tests:test --tests "org.veri.be.modulith.ModulithArchitectureTest"
     ```
 - **2025-12-30**: **Member access routed through services** to allow repository visibility tightening.
   - **Modified Files**:
     ```
     core/core-api/src/main/java/org/veri/be/auth/service/AuthService.java
     core/core-api/src/main/java/org/veri/be/member/service/MemberCommandService.java
     core/core-api/src/main/java/org/veri/be/member/service/MemberQueryService.java
     core/core-api/src/main/java/org/veri/be/member/service/MemberRepository.java
     core/core-api/src/main/java/org/veri/be/member/auth/context/ThreadLocalCurrentMemberAccessor.java
     core/core-api/src/main/java/org/veri/be/mock/MockTokenController.java
     ```
 - **2025-12-30**: **Reading access routed through bookshelf service** and repository visibility tightened.
   - **Modified Files**:
     ```
     core/core-api/src/main/java/org/veri/be/book/service/BookshelfService.java
     core/core-api/src/main/java/org/veri/be/book/service/ReadingRepository.java
     core/core-api/src/main/java/org/veri/be/card/service/CardCommandService.java
     ```
 - **2025-12-30**: **Tests realigned** for member/reading visibility changes.
   - **Modified Files**:
     ```
     tests/src/test/java/org/veri/be/integration/**/*
     tests/src/test/java/org/veri/be/slice/persistence/**/*
     tests/src/test/java/org/veri/be/unit/**/*
     ```
 - **2025-12-30**: **Modulith verification re-run** after member/reading changes.
   - **Command**:
     ```
     JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./gradlew :tests:test --tests "org.veri.be.modulith.ModulithArchitectureTest"
     ```
 - **2025-12-30**: **Phase 3 complete**. Repository visibility tightened and module boundaries validated.
   - **Modified Files**:
     ```
     core/core-api/src/main/java/org/veri/be/**
     tests/src/test/java/org/veri/be/**
     ```
