현재 프로젝트 구조는 **도메인(기능)별로 패키지가 잘 나뉘어 있어(`domain/auth`, `domain/book` 등)** Spring Modulith로 전환하기에 **매우 유리한 구조**입니다.

다만, 현재 구조에서 가장 큰 특징인 **`api` 계층(Controller)과 `domain` 계층(Service/Repository)의 물리적 분리**를 **논리적 응집(Colocation)** 형태로 합치는 것이 전환의 핵심이 될 것입니다.

DDD 관점에서 분석한 Spring Modulith 전환 전략과 분석 내용을 정리해 드립니다.

-----

### 1\. Spring Modulith 전환 전략: 구조 재편성

Spring Modulith의 핵심은 \*\*"관련된 모든 것(UI, Business Logic, Data Access)을 하나의 패키지(모듈)에 넣고, 외부에는 필요한 것만 노출한다"\*\*입니다.

#### 구조 변경 제안

현재의 `api` 패키지를 해체하고, 각 컨트롤러를 해당하는 `domain` 패키지 내부로 이동시켜야 합니다.

  * **Before (현재):** 계층별 분리가 우선됨
      * `api/personal/MemberController` (Presentation)
      * `domain/member/service/MemberService` (Business)
  * **After (Modulith):** 도메인 응집이 우선됨
      * `org.veri.be.member` (패키지명 간소화 추천)
          * `MemberController`
          * `MemberService`
          * `MemberRepository`
          * `entity/Member`

#### 추천 패키지 구조 (Modulith 적용 시)

```text
org.veri.be
├── auth            // [Module] 인증 (AuthService, TokenStorage, AuthController 등)
├── book            // [Module] 도서 (BookService, BookController 등)
├── card            // [Module] 카드
├── comment         // [Module] 댓글
├── image           // [Module] 이미지 처리
├── member          // [Module] 회원
├── post            // [Module] 게시글
└── global          // [Shared Kernel] 공통 설정, 유틸리티 (Modulith에서는 최소화 권장)
```

-----

### 2\. DDD 관점에서의 기대 효과

#### ① Bounded Context의 명확한 경계 구현

  * **현재:** `domain` 폴더 내부는 잘 나뉘어 있지만, `api` 계층이 분리되어 있어 물리적인 Bounded Context가 흐릿합니다. 또한, 개발자가 실수로 `PostService`에서 `MemberRepository`를 직접 주입받아 써도 패키지 구조상 막을 방법이 없습니다.
  * **Modulith:** 각 패키지(`member`, `post`)가 하나의 Bounded Context가 됩니다. **`package-private` (default visibility)** 접근 제어자를 활용하여, 외부 모듈이 내 내부 구현(Entity, Repository)에 접근하는 것을 컴파일 레벨에서 차단할 수 있습니다.
      * *예:* `MemberRepository`는 `member` 패키지 내에서만 접근 가능(`public` 제거). 타 모듈은 오직 `public MemberService`만 호출 가능.

#### ② 도메인 이벤트 기반의 결합도 감소

  * **현재:** `CommentCommandService`에서 댓글 생성 시 알림을 보내야 한다면, `NotificationService`를 직접 의존(주입)해야 합니다. 이는 모듈 간 강한 결합을 유발합니다.
  * **Modulith:** `ApplicationEventPublisher`를 통해 이벤트를 발행하고, `@ApplicationModuleListener`로 처리합니다. 이를 통해 \*\*"댓글 모듈은 알림 모듈을 몰라도 된다"\*\*는 DDD의 철학을 구현할 수 있습니다.

#### ③ Living Documentation (살아있는 문서)

  * Spring Modulith는 코드를 분석하여 \*\*모듈 간의 의존성 그래프(C4 Model)\*\*와 문서(AsciiDoc)를 자동으로 생성해 줍니다. 아키텍처가 의도한 대로 유지되고 있는지 시각적으로 확인할 수 있습니다.

-----

### 3\. 전환 시 주의점 (Risk & Precautions)

#### ① 순환 참조 (Circular Dependency) 해결

  * **상황:** `Member` 모듈이 `Post`를 참조하고, `Post`가 `Member`를 참조하는 경우.
  * **Spring Modulith:** 순환 참조를 **엄격하게 금지**하며, 애플리케이션 실행 시 에러를 발생시킵니다.
  * **해결책:** 공통 부분을 `Shared Kernel`로 분리하거나, \*\*이벤트(Event)\*\*를 사용하여 의존 방향을 한쪽으로 흐르게 해야 합니다.

#### ② `api` 패키지의 도메인 매핑

  * 현재 `api` 패키지 하위에 `common`, `personal`, `social`로 나뉘어 있습니다. 이는 URL 설계 관점의 분류로 보입니다.
  * Modulith로 이동 시 `social/PostController`는 `post` 패키지로 가야 합니다. 이렇게 되면 "Social"이라는 개념이 코드 구조상에서 사라질 수 있습니다. 이를 유지하려면 패키지 구조 대신 **URL Prefix**(`/api/social/posts`)로 개념을 유지해야 합니다.

#### ③ `global` 패키지의 비대화 경계

  * `org.veri.be.global`은 일종의 **Shared Kernel**입니다. DDD에서 Shared Kernel은 모든 Bounded Context가 의존하므로 변경 시 파급력이 큽니다.
  * Modulith 전환 시, 특정 도메인에만 속하는 로직이 `global`에 숨어있지 않도록 최대한 각 모듈로 끄집어내야 합니다.

-----

### 4\. 장단점 요약 분석

| 구분 | 장점 (Pros) | 단점 (Cons) |
| :--- | :--- | :--- |
| **아키텍처** | **강제적 모듈화:** 개발자가 아키텍처 규칙을 위반(내부 클래스 직접 참조 등)하면 컴파일/테스트 실패로 즉시 피드백. | **초기 러닝커브:** 패키지 가시성(visibility) 관리와 이벤트 기반 처리에 대한 이해가 필요함. |
| **테스트** | **독립적 모듈 테스트:** `@ApplicationModuleTest`를 통해 전체 컨텍스트를 띄우지 않고 해당 모듈만 띄워 빠르고 격리된 테스트 가능. | **리팩토링 비용:** 현재 `api`와 `domain`이 분리된 구조를 합치는 물리적인 파일 이동 작업이 필요함. |
| **유지보수** | **명확한 경계:** 프로젝트가 커져도 스파게티 코드가 되는 것을 방지. 마이크로서비스(MSA)로 분리하기 매우 쉬운 구조가 됨. | **유연성 감소:** 단순히 데이터를 조회해서 반환하는 로직도 모듈 경계를 넘어야 하므로 코드가 다소 뻣뻣해질 수 있음. |

### 🚀 결론 및 추천

현재 님의 프로젝트 구조(`domain` 하위가 이미 잘 나뉘어 있음)는 **Spring Modulith로 전환하기에 최적의 상태**입니다.

1.  **1단계 (구조 통합):** `api` 패키지의 컨트롤러들을 `domain` 패키지 내부로 옮기세요. (이때 패키지명에서 `domain`을 빼고 `org.veri.be.auth` 처럼 간결하게 가는 것을 추천합니다.)
2.  **2단계 (캡슐화):** `Repository`, `Entity`, `DTO` 등의 접근 제어자에서 `public`을 제거하여 다른 패키지에서의 접근을 차단하세요.
3.  **3단계 (검증):** `Spring Modulith Test` 의존성을 추가하여 모듈 간 참조 위반이 없는지 테스트(`Modules.verify()`)를 돌려보세요.

이 과정만으로도 \*\*"무늬만 DDD"\*\*가 아닌, **코드 레벨에서 강제되는 진짜 DDD 아키텍처**를 구축하실 수 있을 것입니다.
