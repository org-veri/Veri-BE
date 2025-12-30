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
- [x] **Order of tightening**: Global/Lib first, then core domains, then social modules.

## Phase 2: Module-by-Module Closure
- [x] **Global/Lib**: identify shared abstractions and stabilize public APIs.
- [x] **Auth**: reduce dependence on Global DTOs and Authenticator coupling.
- [x] **Member**: isolate entity exposure and create boundary DTOs where needed.
- [x] **Member**: isolate entity exposure and create boundary DTOs where needed.
- [x] **Book/Card/Comment/Post/Image**: replace cross-entity exposure with DTOs or service interfaces.
  - **Card**: closed with DTO/service/entity/repository exposures and member/book dependencies.
  - **Book**: closed with explicit member service dependency for reading count.
  - **Note**: Book module closure is blocked by **book-member cycle**; module remains **OPEN** for now.
  - **Update**: Book/card cycles resolved; both modules are **CLOSED** with explicit dependencies.

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
- **2025-12-30**: **Auth blacklist boundary cleanup**. `TokenBlacklistStore` moved to **global** to remove lib/auth dependency.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/global/auth/token/TokenBlacklistStore.java
    core/core-api/src/main/java/org/veri/be/global/auth/AuthConfig.java
    core/core-api/src/main/java/org/veri/be/lib/auth/jwt/JwtFilter.java
    core/core-api/src/main/java/org/veri/be/auth/service/AuthService.java
    core/core-api/src/main/java/org/veri/be/auth/service/TokenStorageService.java
    tests/src/test/java/org/veri/be/unit/auth/AuthConfigTest.java
    tests/src/test/java/org/veri/be/unit/auth/AuthServiceTest.java
    ```
- **2025-12-30**: **Lib module closed**. Allowed global auth context/token dependencies.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/lib/package-info.java
    ```
- **2025-12-30**: **Global closure attempt reverted**. Cycles with **lib** and **member** remain; updated issue log.
  - **Issue**:
    ```
    .agents/issue/modulith-global-lib-closure.md
    ```
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/global/package-info.java
    ```
- **2025-12-30**: **Comment-post cycle resolved**. Comment stores `postId`, post maps comments internally, and comment module closed.
  - **Issue**:
    ```
    .agents/issue/completed/modulith-comment-post-cycle.md
    ```
  - **Modified Files**:
    ```
    storage/db-core/src/main/java/org/veri/be/comment/entity/Comment.java
    storage/db-core/src/main/java/org/veri/be/comment/repository/CommentRepository.java
    core/core-api/src/main/java/org/veri/be/comment/service/CommentCommandService.java
    core/core-api/src/main/java/org/veri/be/comment/service/CommentQueryService.java
    core/core-api/src/main/java/org/veri/be/comment/service/PostExistenceProvider.java
    core/core-api/src/main/java/org/veri/be/comment/package-info.java
    core/core-api/src/main/java/org/veri/be/post/service/PostExistenceProviderService.java
    core/core-api/src/main/java/org/veri/be/post/service/PostCommandService.java
    core/core-api/src/main/java/org/veri/be/post/service/PostQueryService.java
    core/core-api/src/main/java/org/veri/be/post/dto/response/PostDetailResponse.java
    core/core-api/src/main/java/org/veri/be/post/package-info.java
    storage/db-core/src/main/java/org/veri/be/post/entity/Post.java
    storage/db-core/src/main/java/org/veri/be/post/repository/PostRepository.java
    tests/src/test/java/org/veri/be/unit/comment/CommentCommandServiceTest.java
    tests/src/test/java/org/veri/be/unit/comment/CommentQueryServiceTest.java
    tests/src/test/java/org/veri/be/unit/comment/CommentTest.java
    tests/src/test/java/org/veri/be/slice/persistence/comment/CommentRepositoryTest.java
    tests/src/test/java/org/veri/be/slice/persistence/post/PostRepositoryTest.java
    tests/src/test/java/org/veri/be/unit/post/PostCommandServiceTest.java
    tests/src/test/java/org/veri/be/unit/post/PostQueryServiceTest.java
    tests/src/test/java/org/veri/be/unit/post/PostResponseMappingTest.java
    ```
- **2025-12-30**: **Book-card cycle resolved**. Reading no longer owns cards; card summaries/visibility are provided via book service interface.
  - **Issue**:
    ```
    .agents/issue/completed/modulith-book-card-cycle.md
    ```
  - **Modified Files**:
    ```
    storage/db-core/src/main/java/org/veri/be/book/entity/Reading.java
    storage/db-core/src/main/java/org/veri/be/book/repository/ReadingRepository.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/ReadingConverter.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingDetailResponse.java
    core/core-api/src/main/java/org/veri/be/book/service/BookshelfService.java
    core/core-api/src/main/java/org/veri/be/book/service/ReadingCardSummaryProvider.java
    core/core-api/src/main/java/org/veri/be/book/package-info.java
    core/core-api/src/main/java/org/veri/be/card/service/ReadingCardSummaryProviderService.java
    storage/db-core/src/main/java/org/veri/be/card/repository/CardRepository.java
    tests/src/test/java/org/veri/be/slice/persistence/reading/ReadingRepositoryTest.java
    tests/src/test/java/org/veri/be/unit/book/BookshelfServiceTest.java
    tests/src/test/java/org/veri/be/unit/book/ReadingConverterTest.java
    tests/src/test/java/org/veri/be/unit/book/ReadingTest.java
    ```
- **2025-12-30**: **Global/member closure analysis**. Logged next refactor steps for auth-context ownership and global-lib decoupling.
  - **Issue**:
    ```
    .agents/issue/modulith-global-lib-closure.md
    ```
- **2025-12-30**: **Auth context moved to member**. Member auth context/guards moved to member module, global context slimmed to memberId/token, and resolver cleanup interceptor added.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/global/auth/context/MemberContext.java
    core/core-api/src/main/java/org/veri/be/global/config/InterceptorConfig.java
    core/core-api/src/main/java/org/veri/be/member/auth/context/AuthenticatedMember.java
    core/core-api/src/main/java/org/veri/be/member/auth/context/AuthenticatedMemberResolver.java
    core/core-api/src/main/java/org/veri/be/member/auth/context/CurrentMemberAccessor.java
    core/core-api/src/main/java/org/veri/be/member/auth/context/MemberRequestContext.java
    core/core-api/src/main/java/org/veri/be/member/auth/context/ThreadLocalCurrentMemberAccessor.java
    core/core-api/src/main/java/org/veri/be/member/auth/context/package-info.java
    core/core-api/src/main/java/org/veri/be/member/auth/guards/MemberGuard.java
    core/core-api/src/main/java/org/veri/be/member/auth/guards/package-info.java
    core/core-api/src/main/java/org/veri/be/member/config/MemberArgumentResolverConfig.java
    core/core-api/src/main/java/org/veri/be/member/config/MemberContextCleanupInterceptor.java
    core/core-api/src/main/java/org/veri/be/book/BookshelfController.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/ReadingConverter.java
    core/core-api/src/main/java/org/veri/be/book/service/BookshelfService.java
    core/core-api/src/main/java/org/veri/be/book/package-info.java
    core/core-api/src/main/java/org/veri/be/comment/CommentController.java
    core/core-api/src/main/java/org/veri/be/comment/package-info.java
    core/core-api/src/main/java/org/veri/be/image/ImageController.java
    core/core-api/src/main/java/org/veri/be/image/package-info.java
    core/core-api/src/main/java/org/veri/be/member/MemberController.java
    core/core-api/src/main/java/org/veri/be/post/PostController.java
    core/core-api/src/main/java/org/veri/be/post/package-info.java
    tests/src/test/java/org/veri/be/integration/IntegrationTestSupport.java
    tests/src/test/java/org/veri/be/integration/usecase/GlobalUnauthorizedTest.java
    tests/src/test/java/org/veri/be/slice/web/BookshelfControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/CardControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/CommentControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/ImageControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/MemberControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/PostControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/SocialCardControllerTest.java
    tests/src/test/java/org/veri/be/unit/auth/CurrentMemberAccessorTest.java
    tests/src/test/java/org/veri/be/unit/auth/MemberContextTest.java
    tests/src/test/java/org/veri/be/unit/auth/MemberGuardTest.java
    tests/src/test/java/org/veri/be/unit/auth/ThreadLocalCurrentMemberAccessorTest.java
    tests/src/test/java/org/veri/be/unit/book/BookshelfServiceTest.java
    tests/src/test/java/org/veri/be/unit/book/ReadingConverterTest.java
    ```
- **2025-12-30**: **Global/lib decoupling**. Moved token interfaces and member-id context into **lib**, updated module dependencies, and closed **global** with explicit allowed dependencies.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/global/package-info.java
    core/core-api/src/main/java/org/veri/be/global/auth/AuthConfig.java
    core/core-api/src/main/java/org/veri/be/global/auth/token/JwtExceptionHandlingTokenProvider.java
    core/core-api/src/main/java/org/veri/be/global/auth/token/TokenProviderConfig.java
    core/core-api/src/main/java/org/veri/be/lib/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/context/MemberContext.java
    core/core-api/src/main/java/org/veri/be/lib/auth/context/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/token/TokenBlacklistStore.java
    core/core-api/src/main/java/org/veri/be/lib/auth/token/TokenProvider.java
    core/core-api/src/main/java/org/veri/be/lib/auth/token/package-info.java
    core/core-api/src/main/java/org/veri/be/lib/auth/jwt/JwtFilter.java
    core/core-api/src/main/java/org/veri/be/lib/auth/jwt/JwtService.java
    core/core-api/src/main/java/org/veri/be/member/auth/context/ThreadLocalCurrentMemberAccessor.java
    core/core-api/src/main/java/org/veri/be/member/package-info.java
    core/core-api/src/main/java/org/veri/be/auth/package-info.java
    core/core-api/src/main/java/org/veri/be/auth/service/AuthService.java
    core/core-api/src/main/java/org/veri/be/auth/service/TokenStorageService.java
    core/core-api/src/main/java/org/veri/be/mock/MockTokenController.java
    tests/src/test/java/org/veri/be/unit/auth/AuthConfigTest.java
    tests/src/test/java/org/veri/be/unit/auth/AuthServiceTest.java
    tests/src/test/java/org/veri/be/unit/auth/MemberContextTest.java
    tests/src/test/java/org/veri/be/unit/auth/ThreadLocalCurrentMemberAccessorTest.java
    tests/src/test/java/org/veri/be/integration/usecase/AuthIntegrationTest.java
    ```
- **2025-12-30**: **Card module closed**. Added explicit allowed dependencies after resolving book-card cycle.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/card/package-info.java
    ```
