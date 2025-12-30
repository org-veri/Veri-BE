# Plan: DDD Layered + Modulith Phase 1 Detailed Plan

**Status**: In Progress  
**Date**: 2025-12-30  
**Goal**: **DDD 레이어드 정비**와 **Spring Modulith 도입 1단계**를 현재 코드베이스에 맞춰 세부 실행 계획으로 구체화한다.

**Parent Task**:
```
.agents/work/backlog/modulith-migration.md
```

## Phase 0: 현재 구조 베이스라인 정리 (분석)
- [ ] **모듈 구조 인벤토리**: 멀티모듈 구성과 역할을 정리한다.
  - **핵심 모듈**
    - `core/core-app` (**Boot Entry**)
    - `core/core-api` (**Controller/Service/DTO/Global**)
    - `storage/db-core` (**JPA Entity/Repository**)
  - **지원 모듈**
    - `clients/*` (**외부 연동 클라이언트**)
    - `support/*` (**공통/로깅/모니터링**)
- [ ] **패키지 구조 스냅샷**: DDD 경계를 정의하기 위해 주요 패키지를 목록화한다.
  - **API 레이어**
    - `core/core-api/src/main/java/org/veri/be/api/common`
    - `core/core-api/src/main/java/org/veri/be/api/personal`
    - `core/core-api/src/main/java/org/veri/be/api/social`
  - **도메인 서비스/DTO**
    - `core/core-api/src/main/java/org/veri/be/domain/{auth,book,card,comment,image,member,post}`
  - **영속성**
    - `storage/db-core/src/main/java/org/veri/be/domain/{auth,book,card,comment,image,member,post}`
- [ ] **기술 스택 확인**: DDD 문서의 가정과 실제 상태를 비교한다.
  - **Fluent Query**: `storage/db-core`에 `me.miensoap:fluent:1.0-SNAPSHOT` 적용 확인.
  - **QueryDSL 제거 상태**: 기존 QueryDSL 의존성 없음 확인.
  - **Spring Boot 버전**: `gradle.properties`의 `springBootVersion=4.0.1` 확인.
- [ ] **경계 후보 매핑 초안**: 현재 패키지 기준으로 **Bounded Context** 후보를 정의한다.
  - **Auth**, **Member**, **Book**, **Card**, **Post**, **Comment**, **Image**
  - 공통: **global**, **lib** (횡단 관심사)

## Phase 1: Preparation & Dependencies (1단계 플랜 상세화)
- [x] **Spring Modulith 버전 호환성 확인**
  - **대상**: Boot `4.0.1`과 호환되는 Modulith BOM 버전.
  - **결과물**: **2.0.1**로 결정 기록.
  - **검증 위치**:
    ```
    gradle.properties
    ```
- [x] **BOM 및 Starter 적용 위치 결정**
  - **Boot Entry 모듈**: `core/core-app`
  - **설정 기준**:
    - **Runtime Starter**는 `core/core-app`에 추가.
    - **Test Starter**는 `core/core-app` 또는 `tests` 모듈 중 테스트 실행 위치에 맞춰 추가.
- [x] **Gradle 변경안 초안 작성**
  - **적용 파일 후보**:
    ```
    build.gradle.kts
    core/core-app/build.gradle.kts
    tests/build.gradle.kts
    ```
  - **변경 내용 초안**:
    - **BOM**: `implementation(platform("org.springframework.modulith:spring-modulith-bom:<version>"))`
    - **Starter**: `implementation("org.springframework.modulith:spring-modulith-starter-core")`
    - **Test**: `testImplementation("org.springframework.modulith:spring-modulith-starter-test")`
- [x] **최소 모듈 테스트 위치/형식 결정**
  - **대상 테스트**: `@ApplicationModuleTest` 기반 모듈 검증 테스트.
  - **후보 위치**:
    - `core/core-app/src/test/java/...`
    - 또는 `tests` 모듈 내 통합 테스트 경로
  - **결정 기준**: 현재 테스트 실행 경로와 Gradle 테스트 태스크 구성.
  - **결정**: **tests** 모듈에 `ModulithArchitectureTest` 배치.
- [x] **초기 모듈 경계 정의 방식 확정**
  - **선택**: **옵션 A**
    - `package-info.java`에 `@ApplicationModule` 선언.
  - **적용 범위**
    - 1차 적용은 **구조 리팩터링 이후** 모듈 패키지(`org.veri.be.<module>`)에 한정.
    - 현재 `api`, `domain`, `global`, `lib`에는 **즉시 적용하지 않음**.
  - **이유**
    - **모듈 경계가 확정된 이후** 선언해야 모듈 검증 실패를 최소화.

## Phase 1.5: DDD 문서 기준 상세 작업 항목 연결 (Week 1 연계)
- [ ] **스토리지 모듈 분해 준비**
  - **현재 상태**: `storage/db-core`가 JPA 엔티티/리포지토리를 포함.
  - **정리 대상**: DDD 문서의 **domain/infrastructure 분리** 요구 사항에 맞춰 **향후 이동 대상**을 리스트업.
  - **결과물**: 이동 대상 클래스 목록과 현재 경로 정리.
- [ ] **도메인 순수성 진단 지표 정의**
  - **목표**: 도메인 모듈 내 `@Entity` 사용 금지.
  - **진단 명령 예시**:
    ```
    rg "@Entity" -g "*.java" storage/db-core/src/main/java
    ```
- [ ] **DDD 단계 전환 체크리스트 작성**
  - **Week 1 마감 기준(프로젝트 맞춤)**:
    - 도메인 모델 이전 계획 수립 완료
    - 인프라 분리 경로 확정
    - 테스트 전략(도메인 POJO 테스트 위치) 합의

## Phase 2: 모듈 경계 후보 맵 (준비 산출물)
- [x] **컨텍스트별 모듈 매핑 테이블 작성**
  - **Module**: Auth
    - **Controllers**:
      ```
      core/core-api/src/main/java/org/veri/be/api/common/AuthController.java
      ```
    - **Services**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/auth/service/AuthService.java
      core/core-api/src/main/java/org/veri/be/domain/auth/service/TokenBlacklistStore.java
      core/core-api/src/main/java/org/veri/be/domain/auth/service/TokenStorageService.java
      ```
    - **Entities**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/auth/entity/BlacklistedToken.java
      storage/db-core/src/main/java/org/veri/be/domain/auth/entity/RefreshToken.java
      ```
    - **Repositories**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/auth/repository/BlacklistedTokenRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/auth/repository/RefreshTokenRepository.java
      ```
  - **Module**: Member
    - **Controllers**:
      ```
      core/core-api/src/main/java/org/veri/be/api/personal/MemberController.java
      ```
    - **Services**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/member/service/MemberCommandService.java
      core/core-api/src/main/java/org/veri/be/domain/member/service/MemberQueryService.java
      ```
    - **Converters/DTO**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/member/converter/MemberConverter.java
      core/core-api/src/main/java/org/veri/be/domain/member/dto/MemberResponse.java
      core/core-api/src/main/java/org/veri/be/domain/member/dto/UpdateMemberInfoRequest.java
      ```
    - **Entities**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/member/entity/Member.java
      ```
    - **Repositories/Query DTO**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/member/repository/MemberRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/member/repository/dto/MemberProfileQueryResult.java
      ```
  - **Module**: Book
    - **Controllers**:
      ```
      core/core-api/src/main/java/org/veri/be/api/personal/BookshelfController.java
      core/core-api/src/main/java/org/veri/be/api/social/SocialReadingController.java
      ```
    - **Services**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/book/service/BookService.java
      core/core-api/src/main/java/org/veri/be/domain/book/service/BookshelfService.java
      ```
    - **Clients/Exceptions**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/book/client/BookSearchClient.java
      core/core-api/src/main/java/org/veri/be/domain/book/client/NaverClientException.java
      core/core-api/src/main/java/org/veri/be/domain/book/exception/BookErrorCode.java
      ```
    - **DTO/Converters/Enums**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/book/controller/enums/ReadingSortType.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/AddBookRequest.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/BookConverter.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/BookPopularListResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/BookPopularListResponseV2.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/BookPopularResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/BookResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/BookSearchResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/NaverBookItem.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/book/NaverBookResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/ReadingConverter.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/request/ReadingModifyRequest.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/request/ReadingPageRequest.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/request/ReadingScoreRequest.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/response/ReadingAddResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/response/ReadingDetailResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/response/ReadingListResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/response/ReadingResponse.java
      core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/response/ReadingVisibilityUpdateResponse.java
      ```
    - **Entities**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/book/entity/Book.java
      storage/db-core/src/main/java/org/veri/be/domain/book/entity/Reading.java
      ```
    - **Repositories/Query DTO**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/book/repository/BookRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/book/repository/ReadingRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/book/repository/dto/BookPopularQueryResult.java
      storage/db-core/src/main/java/org/veri/be/domain/book/repository/dto/ReadingQueryResult.java
      ```
  - **Module**: Card
    - **Controllers**:
      ```
      core/core-api/src/main/java/org/veri/be/api/personal/CardController.java
      core/core-api/src/main/java/org/veri/be/api/personal/CardControllerV2.java
      core/core-api/src/main/java/org/veri/be/api/social/SocialCardController.java
      ```
    - **Services**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/card/service/CardCommandService.java
      core/core-api/src/main/java/org/veri/be/domain/card/service/CardQueryService.java
      ```
    - **DTO/Converters/Enums**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/card/controller/enums/CardSortType.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/CardConverter.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/request/CardCreateRequest.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/request/CardUpdateRequest.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/response/CardCreateResponse.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/response/CardDetailResponse.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/response/CardListResponse.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/response/CardUpdateResponse.java
      core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/response/CardVisibilityUpdateResponse.java
      ```
    - **Entities**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/card/entity/Card.java
      storage/db-core/src/main/java/org/veri/be/domain/card/entity/CardErrorInfo.java
      ```
    - **Repositories/Query DTO**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/card/repository/CardRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/card/repository/dto/CardFeedItem.java
      storage/db-core/src/main/java/org/veri/be/domain/card/repository/dto/CardListItem.java
      ```
  - **Module**: Comment
    - **Controllers**:
      ```
      core/core-api/src/main/java/org/veri/be/api/social/CommentController.java
      ```
    - **Services**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/comment/service/CommentCommandService.java
      core/core-api/src/main/java/org/veri/be/domain/comment/service/CommentQueryService.java
      ```
    - **DTO**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/comment/dto/request/CommentEditRequest.java
      core/core-api/src/main/java/org/veri/be/domain/comment/dto/request/CommentPostRequest.java
      core/core-api/src/main/java/org/veri/be/domain/comment/dto/request/ReplyPostRequest.java
      ```
    - **Entities**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/comment/entity/Comment.java
      ```
    - **Repositories**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/comment/repository/CommentRepository.java
      ```
  - **Module**: Post
    - **Controllers**:
      ```
      core/core-api/src/main/java/org/veri/be/api/social/PostController.java
      ```
    - **Services**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/post/service/PostCommandService.java
      core/core-api/src/main/java/org/veri/be/domain/post/service/PostQueryService.java
      core/core-api/src/main/java/org/veri/be/domain/post/service/LikePostQueryService.java
      ```
    - **DTO/Enums**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/post/controller/enums/PostSortType.java
      core/core-api/src/main/java/org/veri/be/domain/post/dto/request/PostCreateRequest.java
      core/core-api/src/main/java/org/veri/be/domain/post/dto/response/LikeInfoResponse.java
      core/core-api/src/main/java/org/veri/be/domain/post/dto/response/PostDetailResponse.java
      core/core-api/src/main/java/org/veri/be/domain/post/dto/response/PostFeedResponse.java
      core/core-api/src/main/java/org/veri/be/domain/post/dto/response/PostFeedResponseItem.java
      core/core-api/src/main/java/org/veri/be/domain/post/dto/response/PostListResponse.java
      ```
    - **Entities**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/post/entity/LikePost.java
      storage/db-core/src/main/java/org/veri/be/domain/post/entity/Post.java
      storage/db-core/src/main/java/org/veri/be/domain/post/entity/PostImage.java
      ```
    - **Repositories/Query DTO**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/post/repository/LikePostRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/post/repository/PostRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/post/repository/dto/DetailLikeInfoQueryResult.java
      storage/db-core/src/main/java/org/veri/be/domain/post/repository/dto/LikeInfoQueryResult.java
      storage/db-core/src/main/java/org/veri/be/domain/post/repository/dto/PostFeedQueryResult.java
      ```
  - **Module**: Image
    - **Controllers**:
      ```
      core/core-api/src/main/java/org/veri/be/api/common/ImageController.java
      ```
    - **Services**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/image/service/AbstractOcrService.java
      core/core-api/src/main/java/org/veri/be/domain/image/service/ImageCommandService.java
      core/core-api/src/main/java/org/veri/be/domain/image/service/ImageQueryService.java
      core/core-api/src/main/java/org/veri/be/domain/image/service/MistralOcrService.java
      core/core-api/src/main/java/org/veri/be/domain/image/service/OcrService.java
      ```
    - **Client/Config/DTO/Exception**:
      ```
      core/core-api/src/main/java/org/veri/be/domain/image/client/OcrPort.java
      core/core-api/src/main/java/org/veri/be/domain/image/config/OcrConfig.java
      core/core-api/src/main/java/org/veri/be/domain/image/dto/OcrResult.java
      core/core-api/src/main/java/org/veri/be/domain/image/exception/ImageErrorCode.java
      ```
    - **Entities**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/image/entity/Image.java
      storage/db-core/src/main/java/org/veri/be/domain/image/entity/OcrResult.java
      ```
    - **Repositories**:
      ```
      storage/db-core/src/main/java/org/veri/be/domain/image/repository/ImageRepository.java
      storage/db-core/src/main/java/org/veri/be/domain/image/repository/OcrResultRepository.java
      ```
- [x] **공통 레이어 분리 기준 정리**
  - **global**: 인증, 스토리지, 예외 처리, 인터셉터
  - **lib**: 인증 유틸, 응답 포맷, 시간 유틸
  - **기준**: 모듈 내부 vs 전역 공유 여부 명확화
  - **현재 매핑**
    - **global**
      - `core/core-api/src/main/java/org/veri/be/global/auth`
      - `core/core-api/src/main/java/org/veri/be/global/config`
      - `core/core-api/src/main/java/org/veri/be/global/interceptors`
      - `core/core-api/src/main/java/org/veri/be/global/response`
      - `core/core-api/src/main/java/org/veri/be/global/storage`
    - **lib**
      - `core/core-api/src/main/java/org/veri/be/lib/auth`
      - `core/core-api/src/main/java/org/veri/be/lib/exception`
      - `core/core-api/src/main/java/org/veri/be/lib/response`
      - `core/core-api/src/main/java/org/veri/be/lib/time`

## Phase 3: 산출물 정리
- [ ] **결정 사항 기록**
  - **BOM 버전**
  - **Starter 적용 모듈**
  - **모듈 경계 선언 방식**
  - **테스트 배치 위치**
- [ ] **다음 단계 인수인계 준비**
  - Phase 2(구조 리팩터링) 실행을 위한 **이동 리스트**와 **의존성 영향도 요약** 작성
  - **이동 리스트 초안 (컨트롤러)**
    - `org.veri.be.api.common.AuthController` -> `org.veri.be.auth.AuthController`
    - `org.veri.be.api.common.ImageController` -> `org.veri.be.image.ImageController`
    - `org.veri.be.api.common.HealthController` -> `org.veri.be.global.HealthController` (전역 진단)
    - `org.veri.be.api.personal.MemberController` -> `org.veri.be.member.MemberController`
    - `org.veri.be.api.personal.BookshelfController` -> `org.veri.be.book.BookshelfController`
    - `org.veri.be.api.personal.CardController` -> `org.veri.be.card.CardController`
    - `org.veri.be.api.personal.CardControllerV2` -> `org.veri.be.card.CardControllerV2`
    - `org.veri.be.api.social.SocialCardController` -> `org.veri.be.card.SocialCardController`
    - `org.veri.be.api.social.SocialReadingController` -> `org.veri.be.book.SocialReadingController`
    - `org.veri.be.api.social.PostController` -> `org.veri.be.post.PostController`
    - `org.veri.be.api.social.CommentController` -> `org.veri.be.comment.CommentController`
  - **이동 리스트 초안 (서비스/DTO/컨버터)**
    - `org.veri.be.domain.auth` -> `org.veri.be.auth`
    - `org.veri.be.domain.member` -> `org.veri.be.member`
    - `org.veri.be.domain.book` -> `org.veri.be.book`
    - `org.veri.be.domain.card` -> `org.veri.be.card`
    - `org.veri.be.domain.comment` -> `org.veri.be.comment`
    - `org.veri.be.domain.post` -> `org.veri.be.post`
    - `org.veri.be.domain.image` -> `org.veri.be.image`
  - **이동 리스트 초안 (엔티티/리포지토리)**
    - `storage/db-core/src/main/java/org/veri/be/domain/auth` -> `storage/db-core/src/main/java/org/veri/be/auth`
    - `storage/db-core/src/main/java/org/veri/be/domain/member` -> `storage/db-core/src/main/java/org/veri/be/member`
    - `storage/db-core/src/main/java/org/veri/be/domain/book` -> `storage/db-core/src/main/java/org/veri/be/book`
    - `storage/db-core/src/main/java/org/veri/be/domain/card` -> `storage/db-core/src/main/java/org/veri/be/card`
    - `storage/db-core/src/main/java/org/veri/be/domain/comment` -> `storage/db-core/src/main/java/org/veri/be/comment`
    - `storage/db-core/src/main/java/org/veri/be/domain/post` -> `storage/db-core/src/main/java/org/veri/be/post`
    - `storage/db-core/src/main/java/org/veri/be/domain/image` -> `storage/db-core/src/main/java/org/veri/be/image`

## Phase 4: Phase 2 작업 플로우 (리팩터링 단위화)
- [ ] **리팩터링 단위 정의**
  - **원칙**: 모듈 1개씩 이동 → 컴파일 → 테스트 최소 단위 검증
  - **완료 기준**: 패키지 이동 + import 정리 + 컴파일 성공
- [ ] **Step 1: Auth 모듈 전환**
  - **컨트롤러 이동**
    - `core/core-api/src/main/java/org/veri/be/api/common/AuthController.java`
      -> `core/core-api/src/main/java/org/veri/be/auth/AuthController.java`
  - **서비스/도메인 이동**
    - `core/core-api/src/main/java/org/veri/be/domain/auth/**`
      -> `core/core-api/src/main/java/org/veri/be/auth/**`
  - **엔티티/리포지토리 이동**
    - `storage/db-core/src/main/java/org/veri/be/domain/auth/**`
      -> `storage/db-core/src/main/java/org/veri/be/auth/**`
  - **검증**
    - 컴파일 (`:core:core-api:compileJava`, `:storage:db-core:compileJava`)
  - **Status**: 패키지 이동 및 import 수정 완료, 컴파일 검증 대기
- [ ] **Step 2: Member 모듈 전환**
  - **컨트롤러 이동**
    - `core/core-api/src/main/java/org/veri/be/api/personal/MemberController.java`
      -> `core/core-api/src/main/java/org/veri/be/member/MemberController.java`
  - **서비스/도메인 이동**
    - `core/core-api/src/main/java/org/veri/be/domain/member/**`
      -> `core/core-api/src/main/java/org/veri/be/member/**`
  - **엔티티/리포지토리 이동**
    - `storage/db-core/src/main/java/org/veri/be/domain/member/**`
      -> `storage/db-core/src/main/java/org/veri/be/member/**`
  - **검증**
    - 컴파일 + `tests`의 `Member` 관련 테스트 최소 실행
  - **Status**: 패키지 이동 및 import 수정 완료, 컴파일 검증 대기
- [ ] **Step 3: Book 모듈 전환**
  - **컨트롤러 이동**
    - `BookshelfController`, `SocialReadingController`
  - **서비스/도메인 이동**
    - `core/core-api/src/main/java/org/veri/be/domain/book/**`
      -> `core/core-api/src/main/java/org/veri/be/book/**`
  - **엔티티/리포지토리 이동**
    - `storage/db-core/src/main/java/org/veri/be/domain/book/**`
      -> `storage/db-core/src/main/java/org/veri/be/book/**`
  - **검증**
    - 컴파일 + `Book/Reading` 슬라이스 테스트
  - **Status**: 패키지 이동 및 import 수정 완료, 컴파일 검증 대기
- [ ] **Step 4: Card 모듈 전환**
  - **컨트롤러 이동**
    - `CardController`, `CardControllerV2`, `SocialCardController`
  - **서비스/도메인 이동**
    - `core/core-api/src/main/java/org/veri/be/domain/card/**`
      -> `core/core-api/src/main/java/org/veri/be/card/**`
  - **엔티티/리포지토리 이동**
    - `storage/db-core/src/main/java/org/veri/be/domain/card/**`
      -> `storage/db-core/src/main/java/org/veri/be/card/**`
  - **검증**
    - 컴파일 + `Card` 슬라이스 테스트
  - **Status**: 패키지 이동 및 import 수정 완료, 컴파일 검증 대기
- [ ] **Step 5: Comment 모듈 전환**
  - **컨트롤러 이동**
    - `CommentController`
  - **서비스/도메인 이동**
    - `core/core-api/src/main/java/org/veri/be/domain/comment/**`
      -> `core/core-api/src/main/java/org/veri/be/comment/**`
  - **엔티티/리포지토리 이동**
    - `storage/db-core/src/main/java/org/veri/be/domain/comment/**`
      -> `storage/db-core/src/main/java/org/veri/be/comment/**`
  - **검증**
    - 컴파일 + `Comment` 슬라이스 테스트
- [ ] **Step 6: Post 모듈 전환**
  - **컨트롤러 이동**
    - `PostController`
  - **서비스/도메인 이동**
    - `core/core-api/src/main/java/org/veri/be/domain/post/**`
      -> `core/core-api/src/main/java/org/veri/be/post/**`
  - **엔티티/리포지토리 이동**
    - `storage/db-core/src/main/java/org/veri/be/domain/post/**`
      -> `storage/db-core/src/main/java/org/veri/be/post/**`
  - **검증**
    - 컴파일 + `Post` 슬라이스/단위 테스트
- [ ] **Step 7: Image 모듈 전환**
  - **컨트롤러 이동**
    - `ImageController`
  - **서비스/도메인 이동**
    - `core/core-api/src/main/java/org/veri/be/domain/image/**`
      -> `core/core-api/src/main/java/org/veri/be/image/**`
  - **엔티티/리포지토리 이동**
    - `storage/db-core/src/main/java/org/veri/be/domain/image/**`
      -> `storage/db-core/src/main/java/org/veri/be/image/**`
  - **검증**
    - 컴파일 + `Image` 슬라이스 테스트
- [ ] **Step 8: 공통/전역 정리**
  - **대상**
    - `api/common/HealthController`는 `global`로 이동 유지
    - `global`, `lib` 패키지는 **모듈 외부 전역 공유**로 유지
  - **정리**
    - 사용되지 않는 `api`/`domain` 패키지 제거
    - import 정리 및 패키지 참조 수정
  - **검증**
    - `:tests:test` 최소 실행 또는 `:core:core-api:test` 범위 확인

## History
- **2025-12-30**: **Plan created**. Detailed Phase 1 plan aligned with current module structure and DDD Week 1 guidance.
- **2025-12-30**: **Phase 1 started**. Modulith **2.0.1** 선택, **core/core-app**에 런타임 Starter 추가, **tests** 모듈에 테스트 Starter 추가, 모듈 검증 테스트 배치.
  - **Modified Files**:
    ```
    core/core-app/build.gradle.kts
    tests/build.gradle.kts
    tests/src/test/java/org/veri/be/modulith/ModulithArchitectureTest.java
    ```
- **2025-12-30**: **Module mapping expanded**. Bounded context별 **Controller/Service/DTO/Entity/Repository** 매핑 상세화.
- **2025-12-30**: **Boundary declaration decision** 및 **공통 레이어 분리 기준** 확정. Phase 2 이동 리스트 초안 추가.
- **2025-12-30**: **Phase 2 workflow** 확장. 모듈별 리팩터링 단위와 검증 절차 상세화.
- **2025-12-30**: **Auth 모듈 이동 적용**. 패키지 이동 및 import 갱신 완료, 컴파일 검증 대기.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/auth/AuthController.java
    core/core-api/src/main/java/org/veri/be/auth/service/AuthService.java
    core/core-api/src/main/java/org/veri/be/auth/service/TokenBlacklistStore.java
    core/core-api/src/main/java/org/veri/be/auth/service/TokenStorageService.java
    storage/db-core/src/main/java/org/veri/be/auth/entity/BlacklistedToken.java
    storage/db-core/src/main/java/org/veri/be/auth/entity/RefreshToken.java
    storage/db-core/src/main/java/org/veri/be/auth/repository/BlacklistedTokenRepository.java
    storage/db-core/src/main/java/org/veri/be/auth/repository/RefreshTokenRepository.java
    core/core-api/src/main/java/org/veri/be/global/auth/AuthConfig.java
    core/core-api/src/main/java/org/veri/be/lib/auth/jwt/JwtFilter.java
    tests/src/test/java/org/veri/be/slice/web/AuthControllerTest.java
    tests/src/test/java/org/veri/be/unit/auth/AuthServiceTest.java
    tests/src/test/java/org/veri/be/unit/auth/AuthConfigTest.java
    tests/src/test/java/org/veri/be/unit/auth/TokenStorageServiceTest.java
    tests/src/test/java/org/veri/be/integration/usecase/AuthIntegrationTest.java
    ```
- **2025-12-30**: **Member 모듈 이동 적용**. 패키지 이동 및 import 갱신 완료, 컴파일 검증 대기.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/member/MemberController.java
    core/core-api/src/main/java/org/veri/be/member/converter/MemberConverter.java
    core/core-api/src/main/java/org/veri/be/member/dto/MemberResponse.java
    core/core-api/src/main/java/org/veri/be/member/dto/UpdateMemberInfoRequest.java
    core/core-api/src/main/java/org/veri/be/member/exception/MemberErrorCode.java
    core/core-api/src/main/java/org/veri/be/member/service/MemberCommandService.java
    core/core-api/src/main/java/org/veri/be/member/service/MemberQueryService.java
    storage/db-core/src/main/java/org/veri/be/member/entity/Member.java
    storage/db-core/src/main/java/org/veri/be/member/repository/MemberRepository.java
    storage/db-core/src/main/java/org/veri/be/member/repository/dto/MemberProfileQueryResult.java
    tests/src/test/java/org/veri/be/slice/web/MemberControllerTest.java
    tests/src/test/java/org/veri/be/unit/auth/AuthServiceTest.java
    tests/src/test/java/org/veri/be/integration/usecase/AuthIntegrationTest.java
    tests/src/test/java/org/veri/be/integration/usecase/MemberIntegrationTest.java
    tests/src/test/java/org/veri/be/slice/persistence/member/MemberRepositoryTest.java
    ```
- **2025-12-30**: **Book 모듈 이동 적용**. 패키지 이동 및 import 갱신 완료, 컴파일 검증 대기.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/book/BookshelfController.java
    core/core-api/src/main/java/org/veri/be/book/SocialReadingController.java
    core/core-api/src/main/java/org/veri/be/book/client/BookSearchClient.java
    core/core-api/src/main/java/org/veri/be/book/client/NaverClientException.java
    core/core-api/src/main/java/org/veri/be/book/controller/enums/ReadingSortType.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/AddBookRequest.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/BookConverter.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/BookPopularListResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/BookPopularListResponseV2.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/BookPopularResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/BookResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/BookSearchResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/NaverBookItem.java
    core/core-api/src/main/java/org/veri/be/book/dto/book/NaverBookResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/ReadingConverter.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/request/ReadingModifyRequest.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/request/ReadingPageRequest.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/request/ReadingScoreRequest.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingAddResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingDetailResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingListResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingResponse.java
    core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingVisibilityUpdateResponse.java
    core/core-api/src/main/java/org/veri/be/book/exception/BookErrorCode.java
    core/core-api/src/main/java/org/veri/be/book/service/BookService.java
    core/core-api/src/main/java/org/veri/be/book/service/BookshelfService.java
    storage/db-core/src/main/java/org/veri/be/book/entity/Book.java
    storage/db-core/src/main/java/org/veri/be/book/entity/Reading.java
    storage/db-core/src/main/java/org/veri/be/book/repository/BookRepository.java
    storage/db-core/src/main/java/org/veri/be/book/repository/ReadingRepository.java
    storage/db-core/src/main/java/org/veri/be/book/repository/dto/BookPopularQueryResult.java
    storage/db-core/src/main/java/org/veri/be/book/repository/dto/ReadingQueryResult.java
    tests/src/test/java/org/veri/be/slice/web/BookshelfControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/SocialReadingControllerTest.java
    tests/src/test/java/org/veri/be/slice/persistence/book/BookRepositoryTest.java
    tests/src/test/java/org/veri/be/slice/persistence/reading/ReadingRepositoryTest.java
    tests/src/test/java/org/veri/be/unit/book/BookshelfServiceTest.java
    tests/src/test/java/org/veri/be/unit/book/ReadingConverterTest.java
    tests/src/test/java/org/veri/be/unit/book/ReadingTest.java
    tests/src/test/java/org/veri/be/integration/usecase/BookshelfIntegrationTest.java
    tests/src/test/java/org/veri/be/integration/usecase/SocialReadingIntegrationTest.java
    ```
- **2025-12-30**: **Card 모듈 이동 적용**. 패키지 이동 및 import 갱신 완료, 컴파일 검증 대기.
  - **Modified Files**:
    ```
    core/core-api/src/main/java/org/veri/be/card/CardController.java
    core/core-api/src/main/java/org/veri/be/card/CardControllerV2.java
    core/core-api/src/main/java/org/veri/be/card/SocialCardController.java
    core/core-api/src/main/java/org/veri/be/card/controller/enums/CardSortType.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/CardConverter.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/request/CardCreateRequest.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/request/CardUpdateRequest.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/response/CardCreateResponse.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/response/CardDetailResponse.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/response/CardListResponse.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/response/CardUpdateResponse.java
    core/core-api/src/main/java/org/veri/be/card/controller/dto/response/CardVisibilityUpdateResponse.java
    core/core-api/src/main/java/org/veri/be/card/service/CardCommandService.java
    core/core-api/src/main/java/org/veri/be/card/service/CardQueryService.java
    storage/db-core/src/main/java/org/veri/be/card/entity/Card.java
    storage/db-core/src/main/java/org/veri/be/card/entity/CardErrorInfo.java
    storage/db-core/src/main/java/org/veri/be/card/repository/CardRepository.java
    storage/db-core/src/main/java/org/veri/be/card/repository/dto/CardFeedItem.java
    storage/db-core/src/main/java/org/veri/be/card/repository/dto/CardListItem.java
    tests/src/test/java/org/veri/be/slice/web/CardControllerTest.java
    tests/src/test/java/org/veri/be/slice/web/CardControllerV2Test.java
    tests/src/test/java/org/veri/be/slice/web/SocialCardControllerTest.java
    tests/src/test/java/org/veri/be/slice/persistence/card/CardEntityMappingTest.java
    tests/src/test/java/org/veri/be/slice/persistence/card/CardRepositoryTest.java
    tests/src/test/java/org/veri/be/unit/card/CardCommandServiceTest.java
    tests/src/test/java/org/veri/be/unit/card/CardQueryServiceTest.java
    tests/src/test/java/org/veri/be/unit/card/CardResponseMappingTest.java
    tests/src/test/java/org/veri/be/unit/card/CardTest.java
    tests/src/test/java/org/veri/be/integration/usecase/CardIntegrationTest.java
    tests/src/test/java/org/veri/be/integration/usecase/SocialCardIntegrationTest.java
    ```
