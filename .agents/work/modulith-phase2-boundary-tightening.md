# Plan: Modulith Phase 2 Boundary Tightening

**Status**: In Progress
**Date**: 2025-12-30
**Goal**: Transition module declarations from **OPEN** to **CLOSED** with explicit **allowedDependencies**, resolving cross-module coupling incrementally.

**Parent Task**:
```
.agents/work/backlog/modulith-migration.md
```

## Phase 1: Baseline Constraints
- [x] **Inventory current dependencies** using Modulith verification output.
- [x] **Define dependency rules** per module (Auth, Member, Book, Card, Comment, Post, Image, Global, Lib).
- [ ] **Order of tightening**: Global/Lib first, then core domains, then social modules.

## Phase 2: Module-by-Module Closure
- [ ] **Global/Lib**: identify shared abstractions and stabilize public APIs.
- [x] **Auth**: reduce dependence on Global DTOs and Authenticator coupling.
- [ ] **Member**: isolate entity exposure and create boundary DTOs where needed.
- [x] **Member**: isolate entity exposure and create boundary DTOs where needed.
- [ ] **Book/Card/Comment/Post/Image**: replace cross-entity exposure with DTOs or service interfaces.

## Phase 3: Verification
- [ ] **Re-enable strict verification** with **CLOSED** modules.
- [ ] **Test pass** with `:tests:test` and Modulith verification.

## History
- **2025-12-30**: **Phase 2 started**. **Global/Lib** modules set to **CLOSED** with **allowedDependencies** derived from current usage.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/global/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/package-info.java
    ```
- **2025-12-30**: **Global/Lib closure blocked**. Modulith violations detected; reverted **OPEN** and logged issue.
  - **Issue**:
    ```
    .agents/issue/modulith-global-lib-closure.md
    ```
- **2025-12-30**: **Auth module closed**. Allowed dependencies set to `global`, `lib`, `member`.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/auth/package-info.java
    ```
- **2025-12-30**: **Auth service exposure**. `TokenBlacklistStore` 공개를 위해 `auth-service` NamedInterface 추가.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/auth/service/package-info.java
    ```
- **2025-12-30**: **Member module closed**. Named interfaces added for entity/repository exposure; allowed dependencies set.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/member/package-info.java
    storage/db-core/src/main/java/org/veri/be/member/entity/package-info.java
    storage/db-core/src/main/java/org/veri/be/member/repository/package-info.java
    storage/db-core/src/main/java/org/veri/be/member/repository/dto/package-info.java
    ```
- **2025-12-30**: **Member exposure 확장**. `member-dto`, `member-enums` 공개 및 **auth** 허용 의존성 보강.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/member/dto/package-info.java
    core/core-enum/build.gradle.kts
    core/core-enum/src/main/java/org/veri/be/member/entity/enums/package-info.java
    core/core-api/src/main/java/org/veri/be/auth/package-info.java
    ```
- **2025-12-30**: **Member service 공개**. `member-service` NamedInterface 추가 및 **auth** 허용 의존성 갱신.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/member/service/package-info.java
    core/core-api/src/main/java/org/veri/be/auth/package-info.java
    ```
