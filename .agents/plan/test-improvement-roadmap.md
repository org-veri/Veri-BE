# 테스트 코드 고도화 로드맵

> **문서 기준일**: 2026-01-03
> **기반 문서**: `.agents/context/test-convention.md` (개정판)
> **목표**: 새로운 테스트 컨벤션(BDD, Fixture, Steps, Custom Assert)을 전체 테스트 스위트에 적용

---

## 1. 현황 분석 (Current State)

### 1.1 전체 현황

| 구분 | 현황 | 문제점 |
|------|------|--------|
| **Controller 테스트** | 7개 파일 (Member, Post, Card, Comment, Bookshelf, Image, SocialCard) | MockMvc 설정 반복, Helper 메서드 부재 |
| **단위 테스트** | 9개 파일 (Member, Card, Post, Comment, Image, Bookshelf, Auth) | Fixture 부재, Custom Assert 부재 |
| **통합 테스트** | 1개 파일 (SocialCard) | Steps 패턴 미적용, 데이터 생성 비체계적 |
| **Support 클래스** | ExceptionAssertions만 존재 | fixture, steps 패키지 전체 부재 |

### 1.2 세부 현황

#### A. Controller 테스트 (Slice Test)

**현재 패턴:**
```kotlin
@ExtendWith(MockitoExtension::class)
class MemberControllerTest {
    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        // MockMvc 수동 설정 반복...
    }

    @Test
    fun test() {
        mockMvc.perform(
            patch("/api/v1/members/me/info")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
        // ...andExpect 반복
    }
}
```

**개선 필요 사항:**
1. `ControllerTestSupport` 부재 - MockMvc 설정 코드가 모든 테스트 클래스에 반복
2. Helper 메서드 부재 (`postJson`, `putJson`, `get` 등)
3. BDD 스타일 미완성 - `given()`은 사용하나 `then().should()` 대신 `verify()` 사용

**영향받는 파일 (7개):**
- `MemberControllerTest.kt`
- `PostControllerTest.kt`
- `CardControllerTest.kt`
- `CommentControllerTest.kt`
- `BookshelfControllerTest.kt`
- `ImageControllerTest.kt`
- `SocialCardControllerTest.kt`

---

#### B. 단위 테스트 (Service Unit Test)

**현재 패턴:**
```kotlin
@ExtendWith(MockitoExtension::class)
class MemberCommandServiceTest {
    @Mock
    private lateinit var memberRepository: MemberRepository

    @Test
    fun test() {
        val member = Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()

        given(memberRepository.findById(1L)).willReturn(Optional.of(member))
        // ...
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
```

**개선 필요 사항:**
1. **Fixture 패턴 부재** - `Member.builder()`를 직접 호출하여 필수값 누락 위험
2. **Custom Assert 부재** - 복잡한 검증 로직이 테스트 메서드 내에 직접 작성
3. **헬퍼 메서드 비표준화** - 각 테스트 클래스마다 다른 방식으로 엔티티 생성
4. **BDD 검증 미완성** - `verify()` 대신 `then().should()` 사용 필요

**영향받는 파일 (9개):**
- `MemberCommandServiceTest.kt`, `MemberQueryServiceTest.kt`
- `CardCommandServiceTest.kt`, `CardQueryServiceTest.kt`
- `PostCommandServiceTest.kt`, `PostQueryServiceTest.kt`
- `CommentCommandServiceTest.kt`
- `ImageCommandServiceTest.kt`
- `BookshelfServiceTest.kt`

---

#### C. 통합 테스트 (Integration Test)

**현재 패턴:**
```kotlin
class SocialCardIntegrationTest : IntegrationTestSupport() {
    @Test
    fun getCardsFeedSuccess() {
        createCard(true)  // 데이터 생성

        mockMvc.perform(get("/api/v1/cards"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.cards[0].cardId").exists())
    }

    private fun createCard(isPublic: Boolean): Card {
        // 복잡한 데이터 생성 로직이 테스트 메서드 근처에 위치
        var book = Book.builder().title("T").image("I").isbn("ISBN").build()
        book = bookRepository.save(book)
        var reading = Reading.builder().member(getMockMember()).book(book).isPublic(true).build()
        reading = readingRepository.save(reading)
        // ...
    }
}
```

**개선 필요 사항:**
1. **Steps 패턴 부재** - API 호출과 검증 로직이 분리되지 않음
2. **Fixture와 혼재** - 데이터 생성 로직이 테스트 파일 내에 직접 작성
3. **가독성 저하** - "When-Then" 구분이 명확하지 않음

**영향받는 파일 (1개):**
- `SocialCardIntegrationTest.kt`

---

#### D. Support 인프라

**현재 상태:**
```
tests/src/test/kotlin/org/veri/be/support/
└── assertion/
    └── ExceptionAssertions.kt  ✅ (존재)
```

**부재한 인프라:**
```
tests/src/test/kotlin/org/veri/be/support/
├── fixture/          ❌ 부재
│   ├── MemberFixture.kt
│   ├── CardFixture.kt
│   ├── PostFixture.kt
│   └── ...
├── steps/            ❌ 부재
│   ├── MemberSteps.kt
│   ├── CardSteps.kt
│   └── ...
└── ControllerTestSupport.kt  ❌ 부재
```

---

## 2. 개선 우선순위 및 로드맵

### Phase 1: 인프라 구축 (Foundation) - **최우선**

#### 목표
모든 테스트 개선의 기반이 되는 인프라를 먼저 구축하여, 점진적 적용을 가능하게 함

#### 작업 항목

**1.1 ControllerTestSupport 생성**
- 위치: `tests/src/test/kotlin/org/veri/be/support/ControllerTestSupport.kt`
- 기능:
  - `postJson(url, request)` - POST 요청 간소화
  - `putJson(url, request)` - PUT 요청 간소화
  - `patchJson(url, request)` - PATCH 요청 간소화
  - `get(url, params)` - GET 요청 간소화
  - `delete(url)` - DELETE 요청 간소화

**1.2 Fixture 클래스 생성 (도메인별)**
- 위치: `tests/src/test/kotlin/org/veri/be/support/fixture/`
- 생성 순서 (Entity 사용 빈도 기준):
  1. `MemberFixture.kt` - 모든 테스트에서 사용
  2. `CardFixture.kt` - 카드 관련 테스트 3개
  3. `PostFixture.kt` - 게시글 관련 테스트 2개
  4. `BookFixture.kt` - 도서 관련 테스트
  5. `CommentFixture.kt` - 댓글 테스트
  6. `ReadingFixture.kt` - 독서 기록

**1.3 Steps 클래스 생성 (통합 테스트용)**
- 위치: `tests/src/test/kotlin/org/veri/be/support/steps/`
- 생성 순서:
  1. `MemberSteps.kt` - 회원가입, 로그인, 정보 수정
  2. `CardSteps.kt` - 카드 생성, 공개/비공개 전환
  3. `PostSteps.kt` - 게시글 CRUD, 공개/비공개

**1.4 Custom Assert 클래스 생성 (선택적)**
- 위치: `tests/src/test/kotlin/org/veri/be/support/assertion/`
- 대상: 복잡한 검증이 반복되는 도메인
  - `MemberAssert.kt` - 회원 상태 검증 (활성/탈퇴, 닉네임 등)
  - `CardAssert.kt` - 카드 소유권, 공개 여부 검증

**예상 작업 시간:** 2-3일
**완료 기준:**
- [ ] `ControllerTestSupport` 생성 및 단위 테스트
- [ ] 6개 Fixture 클래스 생성
- [ ] 3개 Steps 클래스 생성
- [ ] 2개 Custom Assert 클래스 생성 (옵션)

---

### Phase 2: Controller 테스트 리팩토링 - **우선순위 1**

#### 목표
모든 Controller 테스트를 `ControllerTestSupport`를 상속받도록 변경하여, 반복 코드 제거

#### 리팩토링 대상 파일 (7개)

| 순서 | 파일 | 우선순위 | 사유 |
|------|------|----------|------|
| 1 | `MemberControllerTest.kt` | 🔴 높음 | 가장 간단한 구조, 다른 테스트의 템플릿 역할 |
| 2 | `PostControllerTest.kt` | 🔴 높음 | 엔드포인트 많음 (9개), 효과 큼 |
| 3 | `CardControllerTest.kt` | 🟡 중간 | 카드 관련 기능 핵심 |
| 4 | `CommentControllerTest.kt` | 🟡 중간 | 게시글과 연계 |
| 5 | `BookshelfControllerTest.kt` | 🟢 낮음 | 비즈니스 로직 단순 |
| 6 | `ImageControllerTest.kt` | 🟢 낮음 | 단일 엔드포인트 |
| 7 | `SocialCardControllerTest.kt` | 🟢 낮음 | 이미 SocialCardIntegrationTest로 커버 |

#### 리팩토링 패턴 (Before → After)

**Before:**
```kotlin
@ExtendWith(MockitoExtension::class)
class MemberControllerTest {
    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        member = Member.builder()...
        val controller = MemberController(...)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(...)
            .build()
    }

    @Test
    fun test() {
        mockMvc.perform(
            patch("/api/v1/members/me/info")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
        // ...
    }
}
```

**After:**
```kotlin
@ExtendWith(MockitoExtension::class)
class MemberControllerTest : ControllerTestSupport() {

    @Mock
    private lateinit var memberCommandService: MemberCommandService
    @Mock
    private lateinit var memberQueryService: MemberQueryService

    @BeforeEach
    fun setUp() {
        val controller = MemberController(memberCommandService, memberQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(...)
            .build()
    }

    @Test
    fun test() {
        // When
        val response = patchJson("/api/v1/members/me/info", request)

        // Then
        response.andExpect(status().isOk)
    }
}
```

**변경 포인트:**
1. `ControllerTestSupport` 상속
2. `objectMapper` 초기화 코드 제거 (부모 클래스에 존재)
3. `patchJson()` Helper 메서드 사용
4. `verify()` → `then().should()`로 변경 (BDD 완성)

**예상 작업 시간:** 3-4일
**완료 기준:**
- [ ] 7개 Controller 테스트 파일 리팩토링 완료
- [ ] 모든 테스트 통과
- [ ] 코드 라인 수 30% 이상 감소

---

### Phase 3: 단위 테스트 리팩토링 (Service Unit Test) - **우선순위 2**

#### 목표
모든 단위 테스트에 Fixture와 Custom Assert를 적용하여 테스트 가독성과 유지보수성 향상

#### 리팩토링 대상 파일 (9개)

| 순서 | 파일 | 우선순위 | 사유 |
|------|------|----------|------|
| 1 | `MemberCommandServiceTest.kt` | 🔴 높음 | MemberFixture 적용 첫 번째 대상 |
| 2 | `CardCommandServiceTest.kt` | 🔴 높음 | CardFixture 적용 |
| 3 | `CardQueryServiceTest.kt` | 🔴 높음 | CardFixture 재사용 검증 |
| 4 | `PostCommandServiceTest.kt` | 🟡 중간 | PostFixture 적용 |
| 5 | `PostQueryServiceTest.kt` | 🟡 중간 | PostFixture 재사용 |
| 6 | `CommentCommandServiceTest.kt` | 🟡 중간 | CommentFixture 적용 |
| 7 | `ImageCommandServiceTest.kt` | 🟢 낮음 | 단순 로직 |
| 8 | `BookshelfServiceTest.kt` | 🟢 낮음 | BookFixture 적용 |
| 9 | `Auth 관련 테스트` | 🟢 낮음 | MemberFixture 재사용 |

#### 리팩토링 패턴 (Before → After)

**Before:**
```kotlin
class MemberCommandServiceTest {
    @Test
    fun updatesNicknameAndProfile() {
        val member = member(1L, "member@test.com", "old")  // ❌ 헬퍼 메서드
        val request = UpdateMemberInfoRequest("new", "https://example.com/new.png")

        given(memberRepository.findById(1L)).willReturn(Optional.of(member))

        val response = memberCommandService.updateInfo(request, member.id)

        verify(memberRepository).save(memberCaptor.capture())
        val saved = memberCaptor.value
        assertThat(saved.nickname).isEqualTo("new")  // ❌ 직접 검증
        assertThat(saved.profileImageUrl).isEqualTo("https://example.com/new.png")
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
```

**After:**
```kotlin
class MemberCommandServiceTest {
    @Test
    fun updatesNicknameAndProfile() {
        // Given
        val member = MemberFixture.aMember()
            .id(1L)
            .nickname("old")
            .build()
        val request = UpdateMemberInfoRequest("new", "https://example.com/new.png")

        given(memberRepository.findById(1L)).willReturn(Optional.of(member))

        // When
        val response = memberCommandService.updateInfo(request, member.id)

        // Then
        then(memberRepository).should().save(memberCaptor.capture())
        MemberAssert.assertThat(memberCaptor.value)
            .hasNickname("new")
            .hasProfileImageUrl("https://example.com/new.png")
    }
}
```

**변경 포인트:**
1. `member()` 헬퍼 메서드 → `MemberFixture.aMember()`로 변경
2. `verify()` → `then().should()`로 변경 (BDD 완성)
3. 직접 `assertThat()` 체이닝 → `MemberAssert.assertThat()`으로 변경

**예상 작업 시간:** 4-5일
**완료 기준:**
- [ ] 9개 단위 테스트 파일 리팩토링 완료
- [ ] MemberFixture, CardFixture, PostFixture 적용
- [ ] Custom Assert 2개 이상 적용
- [ ] 코드 라인 수 40% 이상 감소

---

### Phase 4: 통합 테스트 리팩토링 - **우선순위 3**

#### 목표
통합 테스트에 Steps 패턴을 적용하여 시나리오 가독성 확보

#### 리팩토링 대상 파일 (1개)

| 파일 | 우선순위 | 사유 |
|------|----------|------|
| `SocialCardIntegrationTest.kt` | 🟡 중간 | 유일한 통합 테스트, Steps 패턴 검증용 |

#### 리팩토링 패턴 (Before → After)

**Before:**
```kotlin
class SocialCardIntegrationTest : IntegrationTestSupport() {
    @Test
    fun getCardsFeedSuccess() {
        createCard(true)  // ❌ 데이터 생성 로직 노출

        mockMvc.perform(get("/api/v1/cards"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.cards[0].cardId").exists())
    }

    private fun createCard(isPublic: Boolean): Card {
        var book = Book.builder().title("T").image("I").isbn("ISBN").build()
        book = bookRepository.save(book)
        // ... 복잡한 데이터 생성 로직
    }
}
```

**After:**
```kotlin
class SocialCardIntegrationTest : IntegrationTestSupport() {
    @Test
    fun getCardsFeedSuccess() {
        // Given
        val cardId = CardSteps.공개_카드_생성(mockMvc)  // ✅ Steps 활용

        // When
        val response = CardSteps.카드_목록_조회(mockMvc)

        // Then
        CardSteps.카드_목록_응답_검증(response, cardId)
    }
}
```

**변경 포인트:**
1. `createCard()` 내부 메서드 → `CardSteps.공개_카드_생성()`으로 변경
2. MockMvc 호출 → `CardSteps.카드_목록_조회()`로 캡슐화
3. 검증 로직 → `CardSteps.카드_목록_응답_검증()`으로 분리

**예상 작업 시간:** 1-2일
**완료 기준:**
- [ ] SocialCardIntegrationTest 리팩토링 완료
- [ ] CardSteps, MemberSteps 적용
- [ ] 시나리오 흐름이 자연스러운지 확인

---

## 3. 단계별 실행 계획 (Execution Plan)

### Sprint 1: 인프라 구축 (3일)

| 날짜 | 작업 | 산출물 |
|------|------|--------|
| Day 1 | ControllerTestSupport, MemberFixture 생성 | `ControllerTestSupport.kt`, `MemberFixture.kt` |
| Day 2 | CardFixture, PostFixture, BookFixture 생성 | 3개 Fixture 클래스 |
| Day 3 | MemberSteps, CardSteps 생성 및 단위 테스트 | 2개 Steps 클래스 |

### Sprint 2: Controller 테스트 리팩토링 (4일)

| 날짜 | 작업 | 대상 파일 |
|------|------|----------|
| Day 1 | MemberControllerTest, PostControllerTest | 2개 파일 |
| Day 2 | CardControllerTest, CommentControllerTest | 2개 파일 |
| Day 3 | BookshelfControllerTest, ImageControllerTest | 2개 파일 |
| Day 4 | SocialCardControllerTest 및 검증 | 1개 파일 |

### Sprint 3: 단위 테스트 리팩토링 (5일)

| 날짜 | 작업 | 대상 파일 |
|------|------|----------|
| Day 1 | MemberCommandServiceTest, CardCommandServiceTest | 2개 파일 |
| Day 2 | CardQueryServiceTest, PostCommandServiceTest | 2개 파일 |
| Day 3 | PostQueryServiceTest, CommentCommandServiceTest | 2개 파일 |
| Day 4 | ImageCommandServiceTest, BookshelfServiceTest | 2개 파일 |
| Day 5 | Auth 관련 테스트 및 Custom Assert 추가 | 나머지 파일 |

### Sprint 4: 통합 테스트 리팩토링 (2일)

| 날짜 | 작업 | 대상 파일 |
|------|------|----------|
| Day 1 | SocialCardIntegrationTest 리팩토링 | 1개 파일 |
| Day 2 | 전체 테스트 실행 및 문서 업데이트 | 검증 |

**총 예상 기간:** 14일 (약 2주)

---

## 4. 성공 지표 (Success Metrics)

### 4.1 정량 지표

| 항목 | 현재 | 목표 | 측정 방법 |
|------|------|------|-----------|
| **테스트 코드 라인 수** | ~2,500줄 | ~1,500줄 (40% 감소) | `cloc` 명령어 |
| **Fixture 적용覆盖率** | 0% | 100% (도메인 단위 테스트) | Fixture import 존재 여부 |
| **Helper 메서드 재사용률** | 0% | 80% (Controller 테스트) | ControllerTestSupport 상속 여부 |
| **BDD 스타일 준수율** | 30% | 100% | `then().should()` 사용率 |
| **테스트 실행 시간** | 기준점 | +10% 이내 (성능 저하 방지) | Gradle test --scan |

### 4.2 정성 지표

- [ ] 신규 테스트 작성 시 기존 패턴을 그대로 따르기만 하면 됨
- [ ] 테스트 코드만 봐도 비즈니스 시나리오가 이해됨
- [ ] Fixture 변경 시 영향받는 테스트가 명확히 드러남
- [ ] 팀원들이 테스트 리팩토링에 동의하고 적극 참여

---

## 5. 리스크 및 완화 계획 (Risk Mitigation)

### 5.1 리스크 1: 과도한 작업 시간

**위험도:** 🟡 중간
**완화 계획:**
1. Sprint 단위로 나누어 진행 상황 공유
2. Phase 3(단위 테스트)은 일부만 리팩토링하고 나머지는 점진적 적용
3. AI 도구(Claude Code)를 활용하여 boilerplate 코드 자동 생성

### 5.2 리스크 2: 기능 회귀 (Regression)

**위험도:** 🟢 낮음
**완화 계획:**
1. 리팩토링 전후 테스트 결과 비교 (스크린샷 저장)
2. 각 Phase 완료 시 전체 테스트 스위트 실행
3. Git 커밋 메시지에 `[refactor]` 태그 사용하여 롤백 용이성 확보

### 5.3 리스크 3: 팀원 적응 부족

**위험도:** 🟡 중간
**완화 계획:**
1. `.agents/context/test-convention.md` 부록에 코드 예시 충실하게 작성 (완료됨)
2. 첫 번째 리팩토링(MemeberControllerTest)은 팀원들과 함께 Code Review
3. PR 템플릿에 "새로운 테스트 컨벤션 준수 여부" 체크리스트 추가

---

## 6. 다음 단계 (Next Actions)

### 즉시 실행 (이번 주)

1. [ ] **Phase 1.1 시작**: `ControllerTestSupport.kt` 생성
   - 위치: `tests/src/test/kotlin/org/veri/be/support/ControllerTestSupport.kt`
   - 참고: `.agents/context/test-convention.md` 부록의 예시 코드

2. [ ] **Phase 1.2 시작**: `MemberFixture.kt` 생성
   - 위치: `tests/src/test/kotlin/org/veri/be/support/fixture/MemberFixture.kt`
   - 메서드: `aMember()` - 필수값이预设된 Builder 반환

3. [ ] **PR 생성**: 생성된 Fixture 클래스를 팀원들과 리뷰
   - 제목: `[test] 테스트 인프라 구축 Phase 1 - Fixture & Support 추가`
   - 내용: 새로운 컨벤션 소개와 사용 예시

### 이번 달 목표

- [ ] Phase 1 완료 (인프라 구축)
- [ ] Phase 2 완료 (Controller 테스트 리팩토링 50%)

### 내년 Q1 목표

- [ ] 모든 Phase 완료
- [ ] 팀 내 테스트 컨벤션 정착
- [ ] 신규 기능 개발 시 새로운 패턴 자동 적용

---

## 7. 부록: 참고 자료

### A. 관련 문서
- `.agents/context/test-convention.md` - 테스트 컨벤션 전체 문서
- `.agents/plan/test-improvement-roadmap.md` - 본 문서

### B. 코드 예시 저장소
각 Phase의 Before/After 코드는 `.agents/context/test-convention.md` 부록에 상세히 기술됨

### C. 템플릿 파일
- `ControllerTestSupport.kt` 템플릿
- `MemberFixture.kt` 템플릿
- `CardSteps.kt` 템플릿

---

**문서 버전:** v1.0
**마지막 수정:** 2026-01-03
**수정자:** Claude (AI Agent)
**승인자:** [팀 리더/기술 리드] (필요 시)
