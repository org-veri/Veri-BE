# 테스트 컨벤션 / 가이드라인

1. 다음 3가지 유형의 테스트를 사용
    - 슬라이스 테스트
      - 퍼시스턴스 레이어
      - 컨트롤러 레이어
    - 통합 테스트
        - h2 메모리 db 이용한 모킹 최소화한 테스트
        - 컨트롤러 하위 계층부터 테스트
    - 유닛 테스트

2. 테스트 코드는 @Nested, @DisplayName 등을 활용해 가독성 높게 작성
    - 같은 메서드나 엔드포인트에 대한 테스트는 prefix를 붙이는 것이 아닌 @Nested로 묶음
    - @DisplayName은 한국어로"상황 → 기대 결과" 형태로 작성
3. 테스트 작업 마지막은 항상 아래의 테스트 리뷰 방식으로 검증

## 테스트 패키지 구조

```shell
├── java
│   └── com
│       └── smoody
│           └── chat
│               ├── integration
│               │   ├── ExtendedIntegrationTestSupport.java
│               │   ├── IntegrationTestSupport.java
│               │   └── usecase
│               ├── slice
│               │   ├── persistence
│               │   └── web
│               ├── support
│               │   ├── assertion   # Custom Assertions (복잡한 검증 로직 재사용)
│               │   ├── fixture     # Test Data Builders (엔티티 생성 표준화)
│               │   ├── steps       # Integration Test Steps (API 호출/검증 캡슐화)
│               │   └── ControllerTestSupport.java  # Helper 메서드 (postJson 등)
│               └── unit
│                   ├── canvas
│                   ├── chat
│                   ├── common
│                   ├── file
│                   ├── global
│                   ├── lib
│                   └── work
└── resources
    ├── application-persistence.yml
    ├── application.yml
    └── fixtures
```

## 테스트 유형 및 가이드라인

*   **통합 테스트 (Integration tests)**: `IntegrationTestSupport`를 확장하여 사용. 가짜 멤버(`mockMember`)와 `MemberContext`가 사전 설정된 `@SpringBootTest` 환경 제공.
    *   **[추가]** 복잡한 시나리오 검증 시 API 호출과 검증 로직을 분리한 **`Steps` 패턴**을 적극 활용하여 테스트 메서드의 가독성 유지
*   **컨트롤러 슬라이스 테스트 (Controller slice tests)**: `@WebMvcTest`를 사용하여 파라미터 유효성 검사, HTTP 상태 코드, 에러 타입을 검증.
    *   **[추가]** `MockMvc`의 반복적인 호출(`perform`, `content` 등)은 `ControllerTestSupport` 내의 **Helper 메서드(`postJson` 등)**를 사용하여 단순화
*   **영속성 테스트 (Persistence tests)**: `@DataJpaTest`를 사용하여 엔티티 매핑, 제약 조건, Cascade, 고아 제거 및 리포지토리 쿼리 검증.
*   **단위 테스트 (Unit tests)**: Spring 컨텍스트 없이 도메인 로직을 검증하는 빠른 POJO 테스트.
    *   **[추가]** 검증 로직이 복잡하거나 재사용되는 경우 **Custom Assertion**(`MemberAssert`)을 작성하여 활용

## 테스트 조직 및 규칙
*   **패키지 구조**: `test/java/org/veri/be/{integration|slice|unit}/{domain}/`
*   **가독성**: `@Nested` + `@DisplayName` 적극 활용.
*   **패턴**: `Given-When-Then` 패턴 준수.

### BDD 스타일 구현 가이드
*   **Mockito 설정**: `Mockito.when` 대신 **`BDDMockito.given`**을 사용하여 문맥을 통일
*   **검증**: `verify` 대신 **`then().should()`**를 사용하여 자연어 흐름 유지

### Fixture 및 Builder 사용 의무화
*   테스트 내에서 엔티티 생성 시 `new` 키워드나 기본 `@Builder` 사용 지양
*   대신 `support/fixture` 내의 **Test Builder**(`MemberFixture.aMember()`)를 사용하여 필수값 누락 방지 및 가독성 향상
*   복잡한 검증 로직은 **Custom Assertion**(`MemberAssert.assertThat()`)으로 분리하여 재사용

## 테스트 체크포인트 워크플로우 (Claude Code 전용)
기능 구현 후 다음 명령어를 사용하여 테스트 커버리지를 유지합니다.

```bash
# 미검증 변경 사항 분석 및 테스트 생성
/sc:test-checkpoint
```

1.  `.claude/scripts/get_target_diff.sh`를 실행하여 마지막 `test.` 커밋 이후의 변경 사항 식별.
2.  본 문서의 컨벤션에 따라 테스트 생성.
3.  `test.` 접두사가 붙은 커밋으로 체크포인트 생성.

**주의**: 모든 기능 작업 후에는 테스트 체크포인트 커밋을 남겨 테스트 정합성을 유지해야 합니다.

## 퍼시스턴스 레이어 테스트
### 검증 대상

- 엔티티 매핑: 필드–컬럼 매핑, 연관관계(@OneToMany 등), 제약조건
    - 연관관계가 의도대로 저장/삭제되는지(cascade, orphanRemoval, 양방향 편의 메서드 누락)
    - N+1, fetch join, entity graph 등 성능에 직결되는 접근 패턴이 기대대로인지
    - Auditing(@CreatedDate 등)이나 soft delete, @Where 같은 프로젝트 커스텀 규칙이 제대로 적용되는지
- Repository 동작: Spring Data JPA 쿼리 메서드, JPQL/QueryDSL
    - 쿼리 메서드/JPQL/QueryDSL이 원하는 결과를 실제로 내는지(조인, 조건, 정렬, 페이징)
- JPA 설정: 영속성 컨텍스트, flush/dirty checking, 트랜잭션 적용 여부
- DB 제약: unique, not null, cascade, orphanRemoval 동작
    - DB 제약(UNIQUE/FK)과 예외 발생 타이밍이 우리 설계와 맞는지

### 제외할 테스트 (가성비 낮음)

A. “JPA 설정” 중 dirty checking / 1차 캐시 자체 검증
B. “Repository CRUD” 중 단순 save/find/delete의 기계적 검증

아래는 주신 4개 범주 중 **slice test(@DataJpaTest)에서 “가성비(회귀 방지 효율/유지비)”가 낮은 항목은 제외**하고, **남길 항목만으로 구성한 상세 테스트 작성 플랜**입니다.

---

## 0) 먼저 제외할 항목(가성비 낮음)

### A. “JPA 설정” 중 `dirty checking / 1차 캐시` 자체 검증

* Hibernate/JPA 구현 자체 동작을 “원리 수준”으로 확인하는 테스트는 대부분 **프로젝트 변경으로 깨지지 않고**, 깨져도 **실제 장애 형태로 먼저 드러나며**, 테스트가 **취약(flush 타이밍,
  영속성 컨텍스트 상태 의존)** 해 유지비가 큽니다.
* 단, 아래 플랜에서처럼 **우리 코드의 사용 방식 때문에 회귀가 생기는 지점**(예: flush 시 제약 위반, LAZY 접근 시점, orphanRemoval)만 남기는 게 효율적입니다.

### B. “Repository CRUD” 중 단순 save/find/delete의 기계적 검증

* Spring Data JPA 기본 CRUD를 “잘 된다”는 수준으로 모두 테스트하는 것은 ROI가 낮습니다.
* 대신 **우리 엔티티/제약/연관관계/커스텀 쿼리 때문에 깨질 수 있는 CRUD 시나리오**만 남깁니다.

---

## 최우선 테스트 대상

**1. 엔티티 매핑 + 제약조건(필드–컬럼, NOT NULL/UNIQUE, 길이/정밀도)**

**목표:** “운영 DB에서 깨지는 매핑/제약 오류”를 CI에서 조기 검출

**테스트 케이스**

1. **NOT NULL 위반이 의도대로 발생**
    - 필수 필드 누락 상태로 저장 → `flush()` 시점에 예외 발생 확인
        - 포인트: 예외 “발생 여부” + “발생 시점(flush)”만 검증(메시지 문자열 고정 X)
2. **UNIQUE 위반 검증**
    - 동일 유니크 키로 2개 저장 후 flush → 예외 발생 확인
    - 포인트: 유니크가 “컬럼 단일”인지 “복합”인지(복합이면 조합으로 검증)
3. **컬럼 길이/정밀도(예: VARCHAR 길이, DECIMAL scale)**
    - 운영에서 사고가 잦은 필드만 선별해서 경계값 검증
    - 포인트: H2로는 동작이 달라질 수 있으니, 실제 MySQL Testcontainers를 쓰는 경우만 권장(미사용이면 제외 가능)

**데이터 준비 가이드**

* “하나의 최소 유효 엔티티(valid baseline)” 팩토리를 두고, 케이스별로 필수값만 의도적으로 깨뜨립니다.

---

**2. 연관관계 + 생명주기(cascade, orphanRemoval)**

**목표:** “삭제/수정 시 데이터가 남거나 과삭제되는” 고위험 회귀 방지

**테스트 케이스**

1. **cascade persist가 의도대로 동작**
    - 부모 저장 시 자식이 함께 저장되어야 하는 관계면: 부모만 save 후 flush → 자식도 존재 확인
    - 반대로 “자식은 별도 저장” 의도면: 부모만 save 후 flush → 자식이 저장되지 않는 것 확인(실수 방지)
2. **orphanRemoval = true 동작**
    - 부모의 컬렉션에서 자식을 제거 → flush → 자식 row가 실제로 삭제되었는지 확인
    - 포인트: 단순히 컬렉션에서 빠졌는지 말고, **DB에 남았는지**를 확인해야 의미가 있습니다.
3. **cascade remove(또는 삭제 정책)**
    - 부모 삭제 시 자식이 함께 삭제되어야 하는지/남아야 하는지 정책을 테스트로 고정
    - 포인트: FK 제약 때문에 운영에서 터지는 유형을 사전에 잡습니다.

**주의**

* `@OneToMany` 기본이 LAZY라서 테스트가 트랜잭션 내에서만 통과하는데, 이 자체는 slice test에서 굳이 잡기보다(서비스 경계 문제), 여기서는 **삭제/고아 제거/영속화 정책**만 고정하는 게
  ROI가 좋습니다.

---

**3. Repository 커스텀 쿼리(쿼리 메서드/JPQL/QueryDSL) “정확성”**

**목표:** 실제로 가장 자주 깨지는 영역(조건 누락, 조인 조건 실수, 정렬/페이징 오류)을 빠르게 방지

**테스트 케이스 템플릿(각 쿼리당 최소 세트)**

1. **필터 조건 정확성**
    - 조건을 만족하는 데이터/경계값/불만족 데이터를 섞어 넣고 결과 집합이 정확한지
2. **정렬**
    - 동점 케이스를 넣고 2차 정렬 기준까지 검증(정렬 누락 회귀가 흔합니다)
3. **페이징**
    - page size 2~3으로 작게 두고, 총 건수/페이지별 내용이 기대와 같은지
4. **조인/중복**
    - 조인으로 중복 row가 생길 수 있는 쿼리면 distinct/그룹핑 의도대로인지
    - (가능하면) 결과 건수뿐 아니라 “ID 집합”으로 검증

**선별 기준**

* “운영 트래픽/핵심 화면/배치 로직”에 쓰이는 쿼리만 slice test에 올리고, 나머지는 통합 테스트나 기능 테스트에서 커버해도 됩니다.

---

#### 선택 테스트(조건부로만)

**1. flush 시점/제약 위반 타이밍(“트랜잭션 적용 여부”를 이 관점으로만)**

**목표:** 저장 직후가 아니라 flush/commit에서 터지는 유형을 미리 고정

* 예: “중복 저장 후 마지막에 한꺼번에 터짐” 같은 케이스가 중요한 도메인에만 추가

> 이건 ‘트랜잭션 경계가 서비스에 있다’와 충돌하지 않습니다. 여기서 검증하는 것은 경계 설계가 아니라, **Repository/엔티티 사용 패턴에서 예외가 언제 발생하는지**입니다.

---

#### 3) 테스트 구성 방식(실행/유지비 고려)

**테스트 묶음(패키지/클래스)**

* `EntityMappingAndConstraintsTest` : NOT NULL / UNIQUE / (선별) 길이·정밀도
* `CascadeAndOrphanRemovalTest` : cascade/orphanRemoval/삭제 정책
* `RepositoryQueryTest` : 커스텀 쿼리별 정확성(필터·정렬·페이징·중복)

**DB 선택**

* **쿼리 정확성/연관관계**: H2로도 대부분 의미가 있으나,
* **제약/인덱스/길이/정밀도**까지 신뢰하려면: **MySQL Testcontainers** 쪽이 가성비가 올라갑니다(운영과 동일하므로 실패 원인 설명도 쉬움)

---

#### 4) 실제로 “몇 개”를 만들면 좋은가

* 엔티티 1개당:
    * 제약 2~3개(필수/유니크 위주)
    * 연관관계 정책 2~3개(cascade/orphanRemoval/삭제)
* Repository 쿼리:
    * “핵심 쿼리”만 선정해서 **쿼리당 1개 테스트 클래스 또는 2~4개 케이스**로 고정

---

## 성능 관련 테스트 상세

### 1) N+1 / 쿼리 폭증을 테스트로 잡는 방법

#### 무엇을 검증하나

* 특정 유스케이스(또는 Repository 메서드) 1회 호출 시 **실행된 SQL 개수**가 기대치 이내인지
* 컬렉션/연관을 접근했을 때 **추가 쿼리가 더 나가지 않는지**(fetch join/EntityGraph가 실제로 먹혔는지)

#### 테스트 시나리오 패턴

* **Given**: 부모 10개, 각 부모당 자식 3개 같은 데이터 세팅
* **When**: `findAll()` 또는 `findBy...()` 실행 후 결과를 순회하면서 `getChildren().size()` 같은 접근을 “의도적으로” 수행
* **Then**: SQL이 `1(조인 1번)` 또는 `2(부모 1 + IN 쿼리 1)` 같이 **상한**을 넘지 않는지 확인

#### 고정 기준(실무 추천)

* 단건 상세 조회: `<= 1~2개`
* 리스트 조회(부모 + 자식 필요): `1개(fetch join)` 또는 `2개(batch/IN)`
* “목록에 자식까지 항상 필요”면 fetch join/EntityGraph로 1개 고정, “상황 따라 필요”면 DTO 조회나 별도 쿼리로 2개 고정이 흔합니다.

#### 주의점

* 2차 캐시, 배치 사이즈, 영속성 컨텍스트 상태에 따라 쿼리 수가 달라질 수 있어 **테스트는 항상 같은 입력/정렬/순회 방식**으로 고정해야 합니다.
* 쿼리 수 검증은 DB가 H2여도 돌아가지만, 실제 MySQL과의 차이를 줄이려면 **Testcontainers(MySQL)** 조합이 더 안전합니다.

---

### 2) 인덱스 사용(실행계획)을 테스트로 잡는 방법

#### 무엇을 검증하나

* 특정 “핵심 쿼리”가 MySQL에서 **예상한 인덱스(key)** 를 타는지
* 풀스캔(`type=ALL`) 같은 퇴행이 생기면 CI에서 바로 깨지게 하기

#### 테스트 시나리오 패턴

* **Given**: 충분한 데이터 볼륨(최소 수천~수만)을 넣어 카디널리티가 생기게 세팅
* **When**: 문제 쿼리에 대해 `EXPLAIN`(또는 `EXPLAIN FORMAT=JSON`)을 실행
* **Then**:

    * `type`이 `ALL`이 아닌지(보통 `range/ref/eq_ref/const` 기대)
    * `key`가 기대 인덱스명인지
    * (가능하면) `rows`가 비정상적으로 크지 않은지

#### 어떤 DB에서 해야 하나

* 실행계획은 DB 엔진에 종속적이라 **Testcontainers로 실제 MySQL**에서만 의미가 있습니다. (H2로는 실행계획/인덱스 판단이 거의 무의미)

#### 주의점

* 데이터 분포가 다르면 옵티마이저 선택이 바뀔 수 있어서, 테스트 데이터는 “인덱스 타는 게 합리적인 분포”로 세팅해야 합니다.
* 쿼리 작성이 조금만 바뀌어도(함수 사용, like 패턴, 컬럼 순서) 인덱스가 안 탈 수 있으니, **핵심 where 조건과 인덱스 컬럼 순서**를 테스트 대상에 맞게 고정해야 합니다.

---

1. **쿼리 수 상한 테스트**(N+1/폭증)
2. **EXPLAIN으로 인덱스/풀스캔 방지**(핵심 쿼리 몇 개만)

* 지연시간(ms)은 별도 성능/부하 테스트에서 관리


## 컨트롤러 레이어 테스트

컨트롤러 레이어 슬라이스 테스트(@WebMvcTest 등)에서 하위를 전부 모킹한다면, 목적은 “비즈니스가 맞다”가 아니라 **HTTP 계약(Contract)이 깨지지 않았는지**를 검증하는 것

- **요청을 어떻게 받는지**
- **응답을 어떻게 내보내는지**
- **에러를 어떻게 표준화하는지**
- **보안/필터가 어떻게 적용되는지**

## 무엇을 테스트해야 하나

엔드포인트 기준으로 **최소 케이스 세트(성공 1, 검증 실패 1, 예외 매핑 1, 권한 1)** 형태

### 1) 라우팅·요청 매핑 계약

* URL, HTTP method, path variable, query param이 **의도한 핸들러로 연결되는지**
* `consumes/produces`(JSON 등)와 content-type/accept에 따른 동작

### 2) 입력 바인딩과 검증

* `@RequestBody`, `@ModelAttribute`, `@RequestParam`, `@PathVariable` 바인딩이 정확한지
* `@Valid` 검증 실패 시 **400**이 나오는지, 에러 바디 포맷이 일정한지
* 날짜/enum/숫자 포맷 오류 같은 **타입 변환 실패**가 400으로 처리되는지

### 3) 응답 계약

* 성공 시 **status code(200/201/204)** 가 맞는지
* 응답 JSON의 **필드명/구조/nullable 여부/리스트 형태**가 맞는지(클라이언트 계약)
* 헤더(Location, Cache-Control 등)와 쿠키, 리다이렉트 동작

### 4) 예외 처리 표준화

* 서비스(모킹)가 특정 예외를 던졌을 때 컨트롤러 조합(`@ControllerAdvice`)이

    * 올바른 HTTP 코드(404/409/422/500 등)로 매핑하는지
    * 표준 에러 포맷(code/message/traceId 등)이 유지되는지

### 5) 보안·권한·필터(적용 중이라면)

* 인증 없을 때 401/리다이렉트, 권한 없을 때 403
* 특정 role에서만 접근 가능한 엔드포인트가 제대로 막히는지
* CSRF, CORS, 헤더 기반 인증 등 필터가 기대대로 동작하는지(최소 케이스)

### 6) 서비스 호출 “연결” 검증(모킹의 핵심 가치)

* 컨트롤러가 서비스에 넘기는 값이 **정확히 매핑**되는지(파라미터 조합, 기본값, page/size/sort 등)
* 멱등성 키, 사용자 컨텍스트(로그인 유저 id) 전달 여부

## 무엇을 굳이 테스트하지 말아야 하나(ROI 낮음)

* 서비스/도메인 로직의 정합성(이미 서비스 단위/통합 테스트의 책임)
* Jackson 자체 동작처럼 프레임워크가 보장하는 “원리 테스트”(단, **우리의 커스텀 ObjectMapper 설정**이 있다면 계약 관점에서만 검증)

### 컨벤션

- nested root의 DisplayName을 모두 HTTP 메서드 + path로 통일했고, 같은 엔드포인트끼리 @Nested로 정리
    - 테스트 메서드의 DisplayName은 "상황 → 기대 결과" 형태로 작성, prefix 없이


# 테스트 코드 리뷰

**[Role Definition]**
당신은 까다로운 **'Senior QA Engineer'**이자 **'Spring Boot Test Expert'**입니다.
방금 작성된 테스트 코드들이 앞서 제공된 **[목표와 원칙]** 및 **[패키지별 실행 지침]**을 100% 준수했는지 엄격하게 감사(Audit)하고, 기준에 미달하는 부분은 즉시 리팩토링해야 합니다.

### 원칙

---

#### 1. 기본 원칙: F.I.R.S.T 원칙

- Fast (빠르게): 테스트는 자주 돌려야 하므로 실행 속도가 빨라야 합니다. (DB나 네트워크 의존성을 최소화)
- Independent (독립적으로): 각 테스트는 서로 의존하면 안 됩니다. A 테스트의 결과(데이터 상태 등)가 B 테스트에 영향을 주어서는 안 됩니다.
- Repeatable (반복 가능하게): 어떤 환경(로컬, CI 서버, 네트워크 단절 상태)에서든 항상 같은 결과를 내야 합니다. (날짜/시간, 랜덤 값 제어 필요)
- Self-validating (자가 검증): 테스트는 성공(true) 아니면 실패(false)로 끝라야 합니다. 로그를 직접 읽어서 확인해야 한다면 잘못된 테스트입니다.
- Timely (적시에): 이상적으로는 실제 코드를 구현하기 직전(TDD)에 작성되어야 합니다.

#### 2. 유지보수성 (Maintainability): 리팩토링 내성

- 테스트 코드가 유지보수하기 좋은지 판단하는 핵심 질문은 **"내부 구현을 바꿨을 때 테스트가 깨지는가?"**입니다.
- 구현 세부사항(Implementation Detail)이 아닌 행위(Behavior)를 테스트하라:
  - 나쁜 예: private 메서드를 리플렉션으로 테스트하거나, 내부 변수의 상태 변화를 검증. (구현을 조금만 바꿔도 테스트가 깨짐)
  - 좋은 예: 공개 메서드(Public Interface)의 입력과 출력(반환값, 예외)을 검증.

- Over-Mocking 방지:
  - 모든 협력 객체를 Mock으로 대체하면, 실제 객체 간의 연동이 깨져도 테스트는 통과할 수 있습니다. 정말 제어할 수 없는 외부(DB, API)만 Mocking하고, 도메인 로직은 실제 객체를 사용하는 것이 좋습니다.

#### 3. 가독성 (Readability): DAMP vs DRY
- 프로덕션 코드에서는 중복 제거(DRY - Don't Repeat Yourself)가 핵심이지만, 테스트 코드에서는 가독성이 더 중요할 때가 있습니다.
- DAMP (Descriptive And Meaningful Phrases):
  - 테스트 코드는 그 자체로 문서 역할을 해야 합니다. 약간의 중복(설정 코드 등)이 있더라도, 테스트 함수 하나만 읽었을 때 문맥을 완벽히 이해할 수 있어야 합니다.
  - 구조화된 패턴 (AAA 또는 Given-When-Then):
    - Arrange (Given): 테스트 데이터 및 상황 설정
    - Act (When): 실제 메서드 실행
    - Assert (Then): 결과 검증
    이 세 단계가 시각적으로 명확히 구분되어 있는지 확인합니다.

#### 4. 유효성 (Validity): 테스트가 정말 버그를 잡는가?

- 커버리지가 100%여도 버그가 있을 수 있습니다.

- 경계값 분석 (Boundary Value Analysis):
  - 단순한 성공 케이스 외에 0, null, 빈 문자열, 최대값, 최소값 등 경계 조건에서 올바르게 동작하는지 확인합니다.
- One Assert per Test (논리적 단위):
  - 물리적으로 assert 문이 하나여야 한다는 뜻이 아니라, 한 테스트는 하나의 논리적 개념만 검증해야 한다는 원칙입니다. 한 테스트 안에서 너무 많은 것을 검증하면 실패 원인을 파악하기 어렵습니다.
- 뮤테이션 테스트 (Mutation Testing) 고려:
  - 도구(예: Java의 Pitest)를 사용하여 프로덕션 코드를 임의로 고장 냈을 때, 테스트가 실패하는지 확인합니다. (코드를 망가뜨렸는데 테스트가 통과한다면, 그 테스트는 유효성이 없는 것입니다.)

#### 5. 분기 커버리지 관련
- 특히 파일 파서·보안·검색 초기화처럼 외부 상태나 라이브러리 예외에 의존하는 분기는, 의도적으로 실패 상황을 만들어야 해서 유지보수 부담이 큽니다.
- 브랜치 미커버의 상당수는 의미 없는 방어 코드(null 체크, 로깅용 catch, fallback)라서, 비즈니스 리스크 대비 테스트 가치가 낮은 경우가 많습니다. 
- 이 영역까지 채우려면 mock·spy가 늘고 테스트가 구현 세부사항에 묶이면서 변경에 취약해집니다.

---

### 테스트 체크리스트

1. **속도:** 비즈니스 로직 테스트는 0.1초 이내에 끝나는가? (POJO 테스트)
2. **격리:** `@SpringBootTest`는 꼭 필요한 통합 구간에만 사용했는가?
3. **결정성:** 네트워크가 끊겨도 테스트가 통과하는가? (Flaky Test 제거)
4. **리팩토링 내성:** 내부 구현을 바꿨을 때(기능은 동일), 테스트가 깨지지 않는가? (과도한 Mocking 지양)

- 결정성 언제, 어디서 실행해도 결과가 같은가? Mocking, Time Freeze
- 독립성 테스트 실행 순서를 바꿔도 통과하는가? Clean up, Transaction Rollback
- 가독성 메서드 이름만 보고 무엇을 검증하는지 알 수 있는가? Naming Convention
- 명확성 실패했을 때, 왜 실패했는지 바로 알 수 있는가? Assert Message, Focus
- 유연성 비즈니스 로직은 그대로인데 리팩토링했다고 테스트가 깨지는가? Black-box Testing

### 예시 태스크 (필요에 따라 이것보다 많은 작업을 진행해야함)

```markdown
**1. 구조 및 패턴 검증 (Maintainability)**

* **Fixture/Builder 사용 여부:** `Given` 절에서 객체를 직접 `new` 하거나 복잡한 세터로 생성하는 코드가 있다면, 즉시 `TestFileBuilder`, `AuthFixture` 등의
  빌더 패턴으로 교체하여 가독성을 높이세요.
* **중복 제거:** 중복되는 `setup`이나 `assert` 로직이 보이면 공통 유틸 메서드나 부모 클래스, 혹은 커스텀 Assert(`assertBusinessException` 등)로 분리하세요.
* **테스트 이름:** 모든 테스트 메서드명이 `method_condition_expectedResult` 형식을 따르고 있는지, `DisplayName`이 명확한지 확인하고 수정하세요.
* **구조화:** `@Nested`를 사용하여 성공/실패/검증/권한 케이스가 그룹핑되어 있는지 확인하세요.

**2. 커버리지 및 분기 검증 (Coverage & Branch)**

* **Parameterized Test:** 단순 반복되는 분기(확장자 매핑, Null/Empty 체크, Role 분기)가 각각 별도의 메서드로 흩어져 있다면 `@ParameterizedTest`와
  `Arguments`를 사용하여 하나로 통합하세요.
* **Controller 3종 세트:** 모든 Controller 테스트에 최소 3가지 케이스(Happy Path, Validation Fail, Guard/Auth Fail)가 존재하는지 확인하세요. 하나라도
  누락되었다면 추가하세요.

**3. 도메인 및 예외 처리 검증 (Robustness)**

* **복합 검증:** 실패 케이스에서 단순히 예외 타입만 확인하고 끝났는지 체크하세요. 반드시 **"예외 타입 + 에러 코드 + 상태 변경(FileStatus 등) + 저장(Repository) 호출 여부"**를
  한 번에 검증하는 코드로 강화하세요.
* **써드파티 격리:** 외부 의존성(파서, 추출기) 테스트에서 불필요하게 복잡한 Mocking을 하고 있다면, "동작/계약" 위주의 간단한 Mock으로 변경하고, 예외 래핑(Wrapping) 여부를 검증하는지
  확인하세요.

**4. 패키지별 상세 DoD 준수 여부**

* **ODT:** `OdtExtractionService` 테스트에서 결과 JSON의 핵심 섹션 Null 체크가 포함되었나요?
* **File:** `FileExtractService` 등에서 상태 변경 후 `save()`가 호출되었는지 검증하나요? 미지원 확장자 예외가 올바른 ErrorCode로 매핑되었나요?
* **Auth/Guard:** `ThreadLocal` 컨텍스트가 테스트 후 확실히 `clear()` 되는지 확인하세요.

**5. Flaky Test 제거**

* `Thread.sleep`이 사용된 곳이 있다면 `Awaitility`로 변경하거나 제거하세요.
* 실행 순서나 랜덤 값에 의존하는 코드가 있다면 고정값(Fixture)을 사용하도록 수정하세요.
```

**[Output Format]**
분석 결과와 수정된 코드를 아래 형식으로 정리해서 새 md 파일에 작성하세요:

1. **[Audit Summary]**: 위반 사항 요약 (예: "FileServiceTest에서 Parameterized 미적용 발견", "Controller에 Auth 실패 케이스 누락")
2. **[Refactored Code]**: 리팩토링된 전체 테스트 코드 (주석으로 수정 사유 명시)

* *예: // Refactor: 개별 테스트를 @ParameterizedTest로 통합하여 분기 커버리지 확보*
* *예: // Refactor: new File() 대신 TestFileBuilder 사용*


3. **[Remaining Gaps]**: 현재 컨텍스트에서 해결하지 못한 부분이나 추가로 필요한 데이터가 있다면 명시.

---

# 부록: 테스트 작성 패턴 예시 (Best Practices)

이 섹션에서는 팀원들이 바로 복사해서 사용할 수 있는 코드 예시를 제공합니다. 말로 설명하는 것보다 코드 예시 하나가 더 강력하기 때문입니다.

## 1. BDD + Fixture + Custom Assert 패턴 (단위 테스트)

```java
@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberQueryService memberQueryService;

    @Test
    void 존재하는_회원을_ID로_조회한다() {
        // Given
        Member member = MemberFixture.aMember()
                .id(1L)
                .nickname("testUser")
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // When
        MemberResponse result = memberQueryService.getMember(1L);

        // Then
        then(memberRepository).should().findById(1L);
        MemberAssert.assertThat(result)
                .hasId(1L)
                .hasNickname("testUser")
                .isActive();
    }

    @Test
    void 존재하지_않는_회원_ID로_조회하면_예외가_발생한다() {
        // Given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberQueryService.getMember(999L))
                .isInstanceOf(ApplicationException.class)
                .hasErrorCode(ErrorCode.MEMBER_NOT_FOUND);
    }
}
```

**핵심 포인트:**
- `BDDMockito.given()` 사용으로 문맥 명확화
- `MemberFixture.aMember()`로 필수값 누락 방지
- `MemberAssert.assertThat()`으로 복잡한 검증 로직 재사용

## 2. Steps 패턴 (통합 테스트)

```java
@SpringBootTest
@AutoConfigureMockMvc
class MemberIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 회원가입_전체_시나리오() {
        // Given
        var request = MemberSteps.회원가입_정보_생성();

        // When
        var response = MemberSteps.회원가입_요청(mockMvc, request);

        // Then
        MemberSteps.회원_생성_검증(response, request);
        MemberSteps.데이터베이스에_저장됨(memberRepository, request.getEmail());
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateMemberInfo {

        @Test
        void 닉네임을_변경한다() {
            // Given
            var memberId = MemberSteps.회원가입_후_ID_반환(mockMvc);
            var updateRequest = MemberSteps.닉네임_변경_정보_생성("newNickname");

            // When
            MemberSteps.닉네임_변경_요청(mockMvc, memberId, updateRequest);

            // Then
            MemberSteps.닉네임_변경_확인(mockMvc, memberId, "newNickname");
        }
    }
}
```

**Steps 클래스 예시:**

```java
public final class MemberSteps {

    public static MemberSignupRequest 회원가입_정보_생성() {
        return MemberSignupRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .nickname("testUser")
                .build();
    }

    public static ResultActions 회원가입_요청(MockMvc mockMvc, MemberSignupRequest request) throws Exception {
        return mockMvc.perform(post("/api/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    public static void 회원_생성_검증(ResultActions response, MemberSignupRequest request) throws Exception {
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.nickname").value(request.getNickname()));
    }

    public static void 데이터베이스에_저장됨(MemberRepository repository, String email) {
        boolean exists = repository.existsByEmail(email);
        assertThat(exists).isTrue();
    }
}
```

**핵심 포인트:**
- API 호출 로직을 `Steps` 클래스로 캡슐화하여 테스트 코드를 시나리오 중심으로 작성
- `ResultActions` 반환 체이닝으로 추가 검증 가능
- `@Nested`와 조합하여 관련 시나리오 그룹화

## 3. Controller Test Support + Helper 패턴 (슬라이스 테스트)

```java
@WebMvcTest(MemberController.class)
class MemberControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberCommandService memberCommandService;
    @MockBean
    private MemberQueryService memberQueryService;

    @Nested
    @DisplayName("POST /api/members/signup")
    class Signup {

        @Test
        void 정상적인_회원가입_요청이면_201을_반환한다() throws Exception {
            // Given
            var request = MemberFixture.회원가입_요청();
            given(memberCommandService.signup(any())).willReturn(1L);

            // When
            var response = postJson("/api/members/signup", request);

            // Then
            response.andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));
        }

        @Test
        void 이메일_형식이_올바르지_않으면_400을_반환한다() throws Exception {
            // Given
            var request = MemberFixture.회원가입_요청()
                    .email("invalid-email");

            // When
            var response = postJson("/api/members/signup", request);

            // Then
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }
}
```

**ControllerTestSupport 예시:**

```java
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected ResultActions postJson(String url, Object request) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    protected ResultActions putJson(String url, Object request) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    protected ResultActions get(String url, Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder builder = get(url);
        params.forEach(builder::param);
        return mockMvc.perform(builder);
    }
}
```

**핵심 포인트:**
- `postJson()`, `putJson()` 등 헬퍼 메서드로 반복적인 `MockMvc` 설정 코드 제거
- 테스트 메서드가 "When-Then"에 집중할 수 있도록 지원
- URL과 request만 전달하여 테스트 코드 간결화

## 4. Custom Assert 패턴

```java
public class MemberAssert {

    private final MemberResponse actual;

    private MemberAssert(MemberResponse actual) {
        this.actual = actual;
    }

    public static MemberAssert assertThat(MemberResponse actual) {
        return new MemberAssert(actual);
    }

    public MemberAssert hasId(Long expectedId) {
        assertThat(actual.getId()).isEqualTo(expectedId);
        return this;
    }

    public MemberAssert hasNickname(String expectedNickname) {
        assertThat(actual.getNickname()).isEqualTo(expectedNickname);
        return this;
    }

    public MemberAssert hasEmail(String expectedEmail) {
        assertThat(actual.getEmail()).isEqualTo(expectedEmail);
        return this;
    }

    public MemberAssert isActive() {
        assertThat(actual.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        return this;
    }

    public MemberAssert isDeleted() {
        assertThat(actual.getStatus()).isEqualTo(MemberStatus.DELETED);
        return this;
    }

    public void wasCreatedAt(LocalDateTime expectedTime) {
        assertThat(actual.getCreatedAt()).isEqualTo(expectedTime);
    }
}
```

**사용 예시:**

```java
@Test
void 회원_정보를_조회한다() {
    // When
    MemberResponse response = service.getMember(1L);

    // Then
    MemberAssert.assertThat(response)
            .hasId(1L)
            .hasEmail("test@example.com")
            .hasNickname("testUser")
            .isActive();
}
```

**핵심 포인트:**
- Method Chaining으로 유연한 검증 지원
- 비즈니스 용어로 된 검증 메서드로 가독성 향상
- 복잡한 검증 로직을 한 곳에서 관리하여 유지보수성 확보

---

## 패턴 적용 우선순위

1. **모든 단위 테스트**: BDDMockito + Fixture 패턴 적용
2. **복잡한 통합 테스트**: Steps 패턴으로 리팩토링 (3개 이상의 API 호출이 있을 때)
3. **모든 컨트롤러 테스트**: ControllerTestSupport 상속 + Helper 메서드 사용
4. **재사용되는 검증 로직**: Custom Assert로 추출 (2번 이상 반복 시)

