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
  - **Card**: closed with DTO/service/entity/repository exposures and member/book dependencies.
  - **Book**: closed with explicit member service dependency for reading count.
  - **Note**: Book module closure is blocked by **book-member cycle**; module remains **OPEN** for now.

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
- **2025-12-30**: **Book module closed**. DTO/Service/Entity/Repository/Enum 노출 인터페이스 추가 및 의존성 지정.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/book/package-info.java
    core/core-api/src/main/java/org/veri/be/book/service/package-info.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/package-info.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/package-info.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/response/package-info.java
    storage/db-core/src/main/java/org/veri/be/book/entity/package-info.java
    storage/db-core/src/main/java/org/veri/be/book/repository/package-info.java
    storage/db-core/src/main/java/org/veri/be/book/repository/dto/package-info.java
    core/core-enum/src/main/java/org/veri/be/book/entity/enums/package-info.java
    core/core-api/src/main/java/org/veri/be/member/package-info.java
    ```
- **2025-12-30**: **Book module closed**. `member::member-service` 의존성 허용 후 Modulith 테스트 통과.
- **2025-12-30**: **Card module closed**. DTO/Service/Entity/Repository 노출 인터페이스 추가 및 의존성 지정.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/card/package-info.java
    core/core-api/src/main/java/org/veri/be/card/service/package-info.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/package-info.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/request/package-info.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/response/package-info.java
    core/core-api/src/main/java/org/veri/be/card/controller/enums/package-info.java
    storage/db-core/src/main/java/org/veri/be/card/entity/package-info.java
    storage/db-core/src/main/java/org/veri/be/card/repository/package-info.java
    storage/db-core/src/main/java/org/veri/be/card/repository/dto/package-info.java
    ```
- **2025-12-30**: **Member-book cycle workaround**. Member now depends on `ReadingCountProvider` (member-owned interface) implemented in book.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/member/service/ReadingCountProvider.java
    core/core-api/src/main/java/org/veri/be/book/service/BookReadingCountProvider.java
    core/core-api/src/main/java/org/veri/be/member/service/MemberQueryService.java
    storage/db-core/src/main/java/org/veri/be/book/repository/ReadingRepository.java
    tests/src/test/java/org/veri/be/unit/member/MemberQueryServiceTest.java
    core/core-api/src/main/java/org/veri/be/member/package-info.java
    ```
- **2025-12-30**: **Member-card cycle workaround**. Member now depends on `CardCountProvider` (member-owned interface) implemented in card.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/member/service/CardCountProvider.java
    core/core-api/src/main/java/org/veri/be/card/service/CardCountProviderService.java
    core/core-api/src/main/java/org/veri/be/member/service/MemberQueryService.java
    tests/src/test/java/org/veri/be/unit/member/MemberQueryServiceTest.java
    core/core-api/src/main/java/org/veri/be/member/package-info.java
    core/core-api/src/main/java/org/veri/be/card/package-info.java
    ```
- **2025-12-30**: **Card module reopened**. **book-card cycle** logged and card set to **OPEN** pending refactor.
  - **Issue**:
    ```
    .agents/issue/modulith-book-card-cycle.md
    ```
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/card/package-info.java
    ```
- **2025-12-30**: **Book dependency update**. Allow `card::card-entity` to reflect Reading-card association.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/book/package-info.java
    ```
- **2025-12-30**: **Comment module closed**. Named interfaces added for DTO/service/entity/repository with post and member dependencies.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/comment/package-info.java
    core/core-api/src/main/java/org/veri/be/comment/service/package-info.java
    core/core-api/src/main/java/org/veri/be/comment/dto/request/package-info.java
    storage/db-core/src/main/java/org/veri/be/comment/entity/package-info.java
    storage/db-core/src/main/java/org/veri/be/comment/repository/package-info.java
    ```
- **2025-12-30**: **Post module closed**. Named interfaces added for DTO/service/entity/repository with book/comment/member dependencies.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/post/package-info.java
    core/core-api/src/main/java/org/veri/be/post/service/package-info.java
    core/core-api/src/main/java/org/veri/be/post/controller/enums/package-info.java
    core/core-api/src/main/java/org/veri/be/post/dto/request/package-info.java
    core/core-api/src/main/java/org/veri/be/post/dto/response/package-info.java
    storage/db-core/src/main/java/org/veri/be/post/entity/package-info.java
    storage/db-core/src/main/java/org/veri/be/post/repository/package-info.java
    storage/db-core/src/main/java/org/veri/be/post/repository/dto/package-info.java
    ```
- **2025-12-30**: **Comment module reopened**. **comment-post cycle** logged and post dependency list expanded for cross-module DTO usage.
  - **Issue**:
    ```
    .agents/issue/modulith-comment-post-cycle.md
    ```
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/comment/package-info.java
    core/core-api/src/main/java/org/veri/be/post/package-info.java
    ```
- **2025-12-30**: **Image module closed**. Named interfaces added for service/client/dto/config/exception/entity/repository.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/image/package-info.java
    core/core-api/src/main/java/org/veri/be/image/service/package-info.java
    core/core-api/src/main/java/org/veri/be/image/client/package-info.java
    core/core-api/src/main/java/org/veri/be/image/dto/package-info.java
    core/core-api/src/main/java/org/veri/be/image/exception/package-info.java
    core/core-api/src/main/java/org/veri/be/image/config/package-info.java
    storage/db-core/src/main/java/org/veri/be/image/entity/package-info.java
    storage/db-core/src/main/java/org/veri/be/image/repository/package-info.java
    ```
- **2025-12-30**: **Global/Lib exposure prep**. Added named interfaces to global/lib subpackages for future closure.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/global/auth/package-info.java
    core/core-api/src/main/java/org/veri/be/global/auth/dto/package-info.java
    core/core-api/src/main/java/org/veri/be/global/auth/token/package-info.java
    core/core-api/src/main/java/org/veri/be/global/auth/context/package-info.java
    core/core-api/src/main/java/org/veri/be/global/auth/oauth2/package-info.java
    core/core-api/src/main/java/org/veri/be/global/auth/oauth2/dto/package-info.java
    core/core-api/src/main/java/org/veri/be/global/auth/guards/package-info.java
    core/core-api/src/main/java/org/veri/be/global/response/package-info.java
    core/core-api/src/main/java/org/veri/be/global/storage/package-info.java
    core/core-api/src/main/java/org/veri/be/global/storage/dto/package-info.java
    core/core-api/src/main/java/org/veri/be/global/storage/service/package-info.java
    core/core-api/src/main/java/org/veri/be/global/interceptors/package-info.java
    core/core-api/src/main/java/org/veri/be/global/config/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/response/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/exception/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/exception/handler/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/jwt/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/jwt/data/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/util/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/guard/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/time/package-info.java
    storage/db-core/src/main/java/org/veri/be/global/entity/package-info.java
    ```
- **2025-12-30**: **Named interface dependencies aligned**. Updated closed modules to allow global/lib named interface usage.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/auth/package-info.java
    core/core-api/src/main/java/org/veri/be/book/package-info.java
    core/core-api/src/main/java/org/veri/be/image/package-info.java
    core/core-api/src/main/java/org/veri/be/member/package-info.java
    core/core-api/src/main/java/org/veri/be/post/package-info.java
    ```
- **2025-12-30**: **Global/Lib closure attempt reverted**. Cycles with **auth/global/lib** and **global/member** persist; kept **OPEN** and updated issue log.
  - **Issue**:
    ```
    .agents/issue/modulith-global-lib-closure.md
    ```
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/global/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/package-info.java
    ```
