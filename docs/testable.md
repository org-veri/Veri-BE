## 테스트 용이성 진단

### 제어 불가능한 값 주입

- [x] BookshelfService가 `LocalDateTime.now(...)`를 직접 호출 (see `src/main/java/org/veri/be/domain/book/service/BookshelfService.java:96,165,187`) — 완료: `ClockConfig`로 시스템 `Clock` 빈을 등록하고 `BookshelfService`에서 주입받아 사용하도록 수정. Reading 엔티티로 시간 관련 로직 이동.
- [x] AuthService가 토큰 만료 계산에 `Instant.now()`/`System.currentTimeMillis()` 의존 (see `src/main/java/org/veri/be/domain/auth/service/AuthService.java:51-90`) — 완료: `Clock` 생성자 주입으로 로그아웃 잔여시간/닉네임 중복 처리 고정시간 검증 가능.
- [x] TokenStorageService가 만료 비교를 `Instant.now()`에 의존 (see `src/main/java/org/veri/be/domain/auth/service/TokenStorageService.java:19-52`) — 완료: `Clock` 생성자 주입으로 `Instant.now(clock)` 사용.
- [x] `Comment.delete()`와 `MistralOcrService.doExtract()`가 각각 `LocalDateTime.now()`와 `Thread.sleep(500)` 직접 호출 (see `src/main/java/org/veri/be/domain/comment/entity/Comment.java:67`, `src/main/java/org/veri/be/domain/image/service/MistralOcrService.java:49-55`) — 완료: 삭제 시각을 `Clock`으로 주입, OCR 슬립을 `SleepSupport` 빈으로 추상화.

### 의존성 주입 보강

- [x] BookService가 `ObjectMapper`를 직접 생성 — 완료: 스프링 빈 `ObjectMapper` 생성자 주입으로 변경.
- [x] CardController가 로그인 사용자 주입 누락(서비스가 `MemberContext`에 묶임) (see `src/main/java/org/veri/be/api/personal/CardController.java:70-94`) — 완료: `@AuthenticatedMember Member`를 메서드 인자로 받아 `CardCommandService`에 전달.
- [x] PostController의 삭제/공개/비공개 엔드포인트가 인증 사용자 주입 누락 (see `src/main/java/org/veri/be/api/social/PostController.java:67-92`) — 완료: 컨트롤러에서 인증 사용자 주입 후 서비스에 전달.
- [x] CommentController 전 엔드포인트가 인증 멤버 파라미터 미사용 (see `src/main/java/org/veri/be/api/social/CommentController.java:22-46`) — 완료: 컨트롤러에서 인증 멤버 주입, `CommentCommandService`의 `MemberContext` 의존 제거.

### 비즈니스 로직 POJO 분리 (서비스 쪽)

- [x] BookshelfService가 상태 결정·시간 절삭·가시성 변경까지 모두 처리 (see `src/main/java/org/veri/be/domain/book/service/BookshelfService.java:121-220`) — 완료: `Reading` 엔티티에 상태 전환 메서드 추가, 서비스는 단순 조정자 역할.
- [x] CardCommandService가 공개 여부 규칙·소유자 검증·저장을 동시에 처리 (see `src/main/java/org/veri/be/domain/card/service/CardCommandService.java:35-90`) — 완료: `Card` 엔티티에 권한 검사·변경 메서드 추가.
- [x] PostCommandService가 `MemberContext` 정적 상태를 직접 참조 (see `src/main/java/org/veri/be/domain/post/service/PostCommandService.java:54-74`) — 완료: `Post` 엔티티에 `publishBy`, `deleteBy` 등 추가.
- [x] CommentCommandService가 프레임워크 의존 코드에 비즈니스 로직 혼재 (see `src/main/java/org/veri/be/domain/comment/service/CommentCommandService.java:17-65`) — 완료: `Comment.replyBy`, `editBy`, `deleteBy` 등으로 도메인에 위임.
- [x] CardQueryService가 카드 접근 권한을 서비스에서 검사 (see `src/main/java/org/veri/be/domain/card/service/CardQueryService.java:24-41`) — 완료: `Card.assertReadableBy` 도입, 컨트롤러에서 인증 사용자 전달.

### 비즈니스 로직 POJO 분리 (조회/DTO 층)

- [x] ReadingConverter가 정적 메서드 안에서 `MemberContext` 호출 (see `src/main/java/org/veri/be/domain/book/dto/reading/ReadingConverter.java:10-36`) — 완료: `ReadingConverter`를 빈으로 전환하고 `CurrentMemberAccessor` 주입.
- [x] PostQueryService.getPostDetail이 서비스 내부에서 현재 사용자 조회 (see `src/main/java/org/veri/be/domain/post/service/PostQueryService.java:40-55`) — 완료: 컨트롤러가 조회 주체를 전달하도록 수정.
- [x] Converter/Response 객체에 비즈니스 룰 직접 포함 (예: `CardConverter`, `PostFeedResponse`) — 완료: Converter를 빈으로 전환하고 `viewer` 정보를 인자로 받도록 변경, 응답 모델에 `mine` 플래그 추가.
- [x] Reading 관련 로직이 서비스 레이어에 과도하게 있음 (see `src/main/java/org/veri/be/domain/book/entity/Reading.java:1-61`) — 완료: 진행률·점수 업데이트 로직을 `Reading` 엔티티로 이동.

### static 남용 최소화

- [x] `MemberContext`가 정적 `ThreadLocal`을 노출 (`src/main/java/org/veri/be/global/auth/context/MemberContext.java:13-39`) — 완료: `CurrentMemberAccessor` 인터페이스와 `ThreadLocalCurrentMemberAccessor` 구현 추가, 주입 사용.
- [x] `JwtUtil`이 정적 상태에 시크릿/만료 시간 저장 (`src/main/java/org/veri/be/lib/auth/jwt/JwtUtil.java:17-72`) — 완료: `JwtService` 빈으로 변경, 토큰 로직 인스턴스 메서드로 이동.
- [x] Converter들이 정적 메서드로 구현되어 DI 불가 (예: `ReadingConverter`) — 완료: Converter들을 빈으로 등록.
- [x] `StorageUtil.generateUniqueKey`가 `UUID.randomUUID()` 직접 호출 (`src/main/java/org/veri/be/global/storage/service/StorageUtil.java:7-13`) — 완료: `StorageKeyGenerator`/`UuidStorageKeyGenerator` 도입, `AwsStorageService`에 주입.

### 외부 시스템 추상화

- [x] BookService.searchBook가 Naver OpenAPI 호출·파싱에 직접 결합 (see `src/main/java/org/veri/be/domain/book/service/BookService.java:60-95`) — 완료: `BookSearchClient`/`NaverBookSearchClient` 도입하여 HTTP 호출을 외부 클라이언트로 위임하고 서비스는 DTO 변환만 담당.
- [x] MistralOcrService가 HTTP 호출·`Thread.sleep`·`CompletableFuture`를 직접 제어 (see `src/main/java/org/veri/be/domain/image/service/MistralOcrService.java:49-121`) — 완료: `OcrClient`, `SleepSupport`, `ocrExecutor` 빈 주입으로 HTTP/비동기 제어 분리, 서비스는 추상 `AbstractOcrService` 기반으로 텍스트 저장만 처리.
- [x] ImageCommandService가 항상 실제 `MistralOcrService`를 호출 (see `src/main/java/org/veri/be/domain/image/service/ImageCommandService.java:15-31`) — 완료: `OcrService` 인터페이스 의존하도록 변경해 테스트 더블 주입 가능.
- [x] 인증/인가 로직이 `JwtUtil` 정적 유틸 및 저장소에 결합 (see `src/main/java/org/veri/be/domain/auth/service/AuthService.java:27-74`, `src/main/java/org/veri/be/domain/auth/service/TokenStorageService.java:19-52`) — 완료: `TokenProvider`/`TokenBlacklistStore` 인터페이스 도입, `JwtService` 구현은 토큰 발급·파싱만 담당, 블랙리스트 처리는 `TokenStorageService`가 구현.
