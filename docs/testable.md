## 테스트 용이성 진단

### 제어 불가능한 값 주입

- BookshelfService는 주차 인기 조회와 읽기 시작/종료 시 여러 곳에서 LocalDateTime.now(...)를 직접 호출합니다 (
  src/main/java/org/veri/be/domain/book/service/BookshelfService.java:96,165,187). Clock 혹은 TimeProvider를 주입해 테스트에서
  원하는 시점을 주입할 수 있게 하고, Reading.start()/finish() 같은 도메인 메서드로 시간을 계산하도록
  옮기세요.
    - ✅ 적용: `ClockConfig`로 시스템 Clock 빈을 등록하고 `BookshelfService`에서 주입받아 사용하도록 수정했습니다. 독서 상태 전환/점수 업데이트 로직은 `Reading` 엔티티
      메서드(`updateProgress`, `start`, `finish`)로 이동해 테스트 시 외부 시간을 통제할 수 있습니다.
- 토큰 만료 계산과 로그아웃 시점이 AuthService에 고정되어 있어 Instant.now()/ System.currentTimeMillis()를 스텁할 방법이 없습니다 (
  src/main/java/org/veri/be/domain/auth/service/AuthService.java:51-90). Clock을 생성자 주입해 access/refresh 만료와 닉네임 충돌 해결
  로직을 테스트에서 고정된 시간으로 검증하세요.
    - ✅ 적용: `AuthService`에 `Clock`을 주입해 로그아웃 잔여 시간 계산과 닉네임 중복 처리에서 시스템 시간을 직접 호출하지 않도록 수정했습니다.
- TokenStorageService 역시 모든 만료 비교를 Instant.now()에 의존합니다 (
  src/main/java/org/veri/be/domain/auth/service/TokenStorageService.java:19-52). Clock을 주입해 리프레시/블랙리스트 저장과 만료 체크를
  결정적(deterministic)하게 만드세요.
    - ✅ 적용: `TokenStorageService`에서 `Clock`을 생성자 주입받아 토큰 저장/블랙리스트 등록 및 만료 검증 시 `Instant.now(clock)`을 사용하도록 변경했습니다.
- Comment.delete()와 MistralOcrService.doExtract()는 각각 LocalDateTime.now()와 Thread.sleep(500)을 직접 호출합니다 (
  src/main/java/org/veri/be/domain/comment/entity/Comment.java:67,
  src/main/java/org/veri/be/domain/image/service/MistralOcrService.java:49-55). 삭제 시각과 OCR 대기 시간을 외부에서 주입하거나 전략 형태로 감싸면
  재현 가능한 단위 테스트가 가능합니다.
    - ✅ 적용: `CommentCommandService`에서 `Clock`을 주입해 삭제 시각을 `Comment.delete(clock)`으로 기록하고, `MistralOcrService`는
      `SleepSupport` 빈을 주입받아 테스트에서 슬립 전략을 교체할 수 있도록 했습니다.

### 의존성 주입 보강

- BookService가 ObjectMapper를 직접 생성해 테스트 doubles나 공통 설정을 적용하기 어렵습니다 (
  src/main/java/org/veri/be/domain/book/service/BookService.java:36). 스프링 빈으로 등록된 ObjectMapper를 생성자 주입하면 모킹·커스터마이징이
  쉬워집니다.
    - ✅ 적용: `BookService`가 스프링이 관리하는 `ObjectMapper`를 생성자로 주입받도록 변경했습니다. 이제 테스트에서 커스텀 매퍼를 주입하거나 공통 직렬화 설정을 손쉽게 재사용할 수 있습니다.
- CardController는 수정·삭제 요청에서 로그인 사용자를 주입하지 않아 서비스가 MemberContext에 묶입니다 (
  src/main/java/org/veri/be/api/personal/CardController.java:70-94). @AuthenticatedMember를 메서드 인자로 받아
  CardCommandService에 넘기면 서비스도 순수 자바 객체로 테스트할 수 있습니다.
    - ✅ 적용: 카드 수정/삭제/공개 여부 변경 API 모두에서 `@AuthenticatedMember Member`를 주입하고, `CardCommandService`는 해당 멤버를 인자로 받아 권한을 검사하도록 변경했습니다.
- PostController의 삭제/공개/비공개 엔드포인트도 동일하게 인증 사용자를 주입하지 않아 서비스가 정적 컨텍스트를 조회합니다 (
  src/main/java/org/veri/be/api/social/PostController.java:67-92). Controller에서 명시적으로 멤버를 받고 서비스 시그니처를 publishPost(
  postId, Member actor) 형태로 바꾸세요.
    - ✅ 적용: 게시글 삭제/공개/비공개 API가 모두 인증 멤버를 주입하고, `PostCommandService`는 전달된 멤버를 사용해 권한을 확인하도록 리팩토링했습니다.
- CommentController 전 엔드포인트가 인증 멤버를 파라미터로 받지 않아 CommentCommandService에서 MemberContext를 직접 참조합니다 (
  src/main/java/org/veri/be/api/social/CommentController.java:22-46). DI 원칙에 맞게 필요한 의존성(로그인 사용자)을 생성자/메서드 인자로 전달하세요.
    - ✅ 적용: 댓글 작성/수정/삭제/답글 작성 모두 인증 멤버를 컨트롤러에서 주입하며, `CommentCommandService`는 더 이상 `MemberContext`에 의존하지 않습니다.

### 비즈니스 로직 POJO 분리 (서비스 쪽)

- BookshelfService가 읽기 상태 결정, 시작/종료 시간 절삭, 가시성 변경까지 모두 수행합니다 (
  src/main/java/org/veri/be/domain/book/service/BookshelfService.java:121-220). Reading 엔티티에 updateProgress(
  Command), start(Clock), finish(Clock) 같은 메서드를 추가해 서비스는 영속성/트랜잭션만 담당하도록 분리하세요.
    - ✅ 적용: `Reading`에 상태 전환 전용 메서드를 추가해 비즈니스 규칙을 엔티티 내부에서 보유하도록 정리했습니다. BookshelfService는 이제 도메인 메서드를 호출하는 얇은 조정자 역할만 수행합니다.
- CardCommandService는 카드 공개 여부 규칙과 소유자 검증, 저장을 동시에 처리합니다 (
  src/main/java/org/veri/be/domain/card/service/CardCommandService.java:35-90). Card 혹은 별도 도메인 서비스에 “읽기가 비공개면 카드도
  비공개” 규칙과 authorize 호출을 맡기고, 애플리케이션 서비스는 card.changeVisibility(Member actor, boolean request) 등
  을 호출하는 식으로 단순화하세요.
    - 🔧 제안: `Card` 엔티티에 `changeVisibility(Member actor, boolean request)`를 도입해, 가시성 제약과 소유자 검증을 한 곳에서 처리하도록 만드세요. 서비스는 엔티티를 불러오고 명령만 위임하면 되므로 테스트도 쉬워집니다.
- PostCommandService는 게시글 공개/비공개와 삭제를 하면서 MemberContext의 정적 상태를 직접 읽습니다 (
  src/main/java/org/veri/be/domain/post/service/PostCommandService.java:54-74). Post 엔티티에 publishBy(Member),
  deleteBy(Member) 같은 메서드를 추가하면 비즈니스 규칙을 POJO에서 검증할 수 있습니다.
    - 🔧 제안: `Post` 엔티티에 `publishBy(Member)`, `unpublishBy(Member)`, `deleteBy(Member)` 메서드를 추가하고, 권한 검사를 엔티티 내부로 옮기면 `PostCommandService`는 단순히 도메인 명령을 호출하고 저장만 하면 됩니다.
- CommentCommandService는 댓글 생성·수정·삭제 로직을 모두 프레임워크 의존적 코드 안에 두고 있습니다 (
  src/main/java/org/veri/be/domain/comment/service/CommentCommandService.java:17-65). CommentThread 혹은 Comment 엔티티에
  reply(Member, String), editBy(Member, String) 등을 도입해 Mockito 없이 단위 테스트 가능한 구조로 만들면 복잡한 트
  랜잭션 없이도 검증 가능합니다.
    - 🔧 제안: `Comment` 엔티티에 `replyBy(Member, String)`, `editBy(Member, String)`, `deleteBy(Member, Clock)` 등을 추가하고, 서비스는 트랜잭션 경계만 관리하도록 바꾸세요. 그러면 댓글 도메인을 순수 POJO 테스트로 검증할 수 있습니다.
- CardQueryService.getCardDetail는 카드 접근 권한을 서비스 층에서 검사합니다 (
  src/main/java/org/veri/be/domain/card/service/CardQueryService.java:24-41). 조회 서비스가 아닌 Card/Reading 도메인에 “비공개 카드
  접근 정책”을 담고, 메서드 파라미터로 전달된 요청자와 함께 판별하도록 옮기세요.
    - 🔧 제안: `Card`에 `assertReadableBy(Member actor)` 같은 메서드를 정의하고, 서비스는 단순히 요청자를 넘겨 호출하게 하세요. 이렇게 하면 권한 정책을 한 곳에서 유지하고 단위 테스트도 간편해집니다.

### 비즈니스 로직 POJO 분리 (조회/DTO 층)

- ReadingConverter가 정적 메서드 안에서 MemberContext를 호출해 카드 요약 노출을 필터링합니다 (
  src/main/java/org/veri/be/domain/book/dto/reading/ReadingConverter.java:10-36). Converter를 빈으로 만들고 호출자에게 viewerId를
  인자로 받으면 순수 자바 테스트가 가능합니다.
    - ✅ 적용: `ReadingConverter`를 `@Component`로 전환하고 `CurrentMemberAccessor`를 주입하여, 현재 사용자 정보를 의존성 주입을 통해 전달하도록 변경했습니다.
- PostQueryService.getPostDetail가 서비스 내부에서 현재 사용자를 조회합니다 (
  src/main/java/org/veri/be/domain/post/service/PostQueryService.java:40-55). Controller에서 조회 주체를 넘겨주고, 서비스는 단순히
  도메인/리포지토리 호출만 하도록 분리하면 MockMvc 없이도 테스트할 수 있습니다.
    - 🔧 제안: `PostQueryService#getPostDetail(Long postId, Member requester)` 형태로 바꾸고, 컨트롤러에서 인증 사용자를 주입하세요. 서비스는 전달받은 requester만 활용하므로 단위 테스트에서 더미 멤버를 사용해 다양한 케이스를 검증할 수 있습니다.
- CardController/PostController 일부 엔드포인트는 응답 DTO 생성을 위해 Converter나 Response 객체에 직접 비즈니스 룰을 넣습니다 (예: CardConverter,
  PostFeedResponse). DTO 생성기는 POJO이므로, 내부에서 다시 정적 컨텍스트에 의존하지 않도록 필요한 데이터(예: viewerId, 권한 플래그)를 생성자 인자로 받도록 인터페이스를
  정리하세요.
    - 🔧 제안: 모든 DTO 팩토리에 `viewerId`, `isOwner` 같은 정보를 명시적으로 넘기고, DTO는 전달된 데이터만 사용하도록 제한하세요. 이렇게 하면 DTO 변환기를 완전히 POJO화할 수 있습니다.
- Reading과 관련된 로직 대부분이 서비스 레이어에 존재하여 엔티티가 단순 데이터 컨테이너로만 쓰이고 있습니다 (
  src/main/java/org/veri/be/domain/book/entity/Reading.java:1-61). ‘도서 공개/비공개 시 카드 동기화’처럼 이미 일부 규칙이 엔티티에 있는 만큼,
  진행률/점수 변경 같은 나머지 규칙도 엔티티 메서드로 끌어오면 테스트 대상이 줄어듭니다.
    - ✅ 적용: 진행률·점수 업데이트 로직을 `Reading` 엔티티로 이동하여 상태 결정 규칙을 POJO로 격리했습니다. 남은 규칙(예: 카드 동기화 정책)도 동일한 패턴으로 이관하면 됩니다.

### static 남용 최소화

- MemberContext는 정적 ThreadLocal을 노출하고 서비스·컨버터에서 직접 접근하게 합니다 (
  src/main/java/org/veri/be/global/auth/context/MemberContext.java:13-39). 요청 범위 빈 혹은 인터페이스로 감싸고 필요한 곳에 주입해야 테스트 시
  ThreadLocal 초기화 없이도 사용할 수 있습니다.
    - ✅ 적용: `CurrentMemberAccessor` 인터페이스와 `ThreadLocalCurrentMemberAccessor` 구현을 추가해, 서비스/컨버터는 더 이상 `MemberContext` 정적 메서드를 직접 호출하지 않고 주입된 인터페이스에만 의존합니다.
- JwtUtil이 정적 상태에 시크릿/만료 시간을 저장해두어 테스트마다 초기화하기 어렵습니다 (src/main/java/org/veri/be/lib/auth/jwt/JwtUtil.java:17-72).
  JwtService 빈으로 변경하고 필요한 구성(JwtProperties, Clock)을 생성자 주입하면 스파이/페이크를 사용할 수 있습니다.
    - 🔧 제안: `JwtService` 클래스를 만들고, `generateAccessToken`, `parseAccessToken` 등을 인스턴스 메서드로 옮긴 뒤 `Clock`과 `JwtProperties`를 생성자에서 주입하세요. 정적 상태 제거만으로도 테스트 간 간섭을 차단할 수 있습니다.
- ReadingConverter와 기타 Converter 들이 모두 정적 메서드로 구현되어 있어 테스트 시 의존성 주입이 불가능합니다 (
  src/main/java/org/veri/be/domain/book/dto/reading/ReadingConverter.java:10-37). Bean으로 등록하거나 Component와 Mapper
  인터페이스를 사용해 목킹 가능한 구조로 바꾸세요.
    - ✅ 적용: `ReadingConverter`를 빈으로 등록해 테스트에서 목킹 가능한 구조를 마련했습니다. 다른 Converter도 동일 패턴으로 이전할 수 있습니다.
- StorageUtil.generateUniqueKey는 UUID.randomUUID()를 직접 호출해 presigned URL 테스트를 불안정하게 만듭니다 (
  src/main/java/org/veri/be/global/storage/service/StorageUtil.java:7-13). 랜덤 키 생성기를 전략/함수로 주입하면 결정적인 테스트가 가능합니다.
    - 🔧 제안: `StorageKeyGenerator` 인터페이스를 도입하고 기본 구현은 UUID를 사용하도록 하되, 테스트에서는 고정 키를 반환하는 Fake를 주입하세요. 그러면 Presigned URL 생성 테스트가 안정적으로 수행됩니다.

### 외부 시스템 추상화

- BookService.searchBook은 Naver OpenAPI 호출·파싱을 서비스에 직접 묶어두었습니다 (
  src/main/java/org/veri/be/domain/book/service/BookService.java:60-95). BookSearchClient 인터페이스를 도입해 Naver 구현과 Fake
  구현을 분리하면 통합 테스트 없이도 실패 케이스를 재현할 수 있습니다.
- MistralOcrService가 HTTP 호출/Thread.sleep/CompletableFuture를 직접 제어하고 있어 네트워크·동시성까지 함께 테스트해야 합니다 (
  src/main/java/org/veri/be/domain/image/service/MistralOcrService.java:49-121). OcrClient 인터페이스, Sleeper, Executor를
  주입해 외부 호출과 동시성 전략을 분리하면 순수 단위 테스트 작성이 가능합니다.
- ImageCommandService는 항상 실제 MistralOcrService를 호출합니다 (
  src/main/java/org/veri/be/domain/image/service/ImageCommandService.java:15-31). 도메인 계층에서는 OcrService 인터페이스만 보게 하고
  Fake OCR 구현을 테스트에서 주입하세요.
- 인증/인가 로직이 JwtUtil 정적 유틸과 Redis(?) 저장소에 직접 결합돼 있습니다 (
  src/main/java/org/veri/be/domain/auth/service/AuthService.java:27-74,
  src/main/java/org/veri/be/domain/auth/service/TokenStorageService.java:19-52). 토큰 발급/검증 인터페이스, 블랙리스트 저장소 인터페이스를
  도입하면 외부 인프라 없이 인증 시나리오를 검증할
  수 있습니다.
