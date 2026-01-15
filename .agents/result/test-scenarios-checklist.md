# 릴리즈 게이트 테스트 시나리오 체크리스트

본 문서는 **Modulith 아키텍처 규칙**과 **교차 애그리거트 불변식**, 그리고 **이벤트 전파 SLO**를 검증하기 위한 구체적인 테스트 시나리오 목록입니다. 개발 및 QA 단계에서 릴리즈 가부(Go/No-Go)를 판단하는 기준으로 사용합니다.

---

## 1. Modulith 아키텍처 검증 (Static Analysis)

**목표**: 모듈 간 경계 위반 및 순환 참조 방지. CI 파이프라인에서 가장 먼저 실행되어야 합니다.

| ID | Test Name | 시나리오 (Given-When-Then) | 비고 |
| :--- | :--- | :--- | :--- |
| **A-01** | `verifyModulithStructure` | **When** `ApplicationModules.of(Application::class).verify()` 실행<br>**Then** 예외가 발생하지 않아야 함 (순환 참조, 불법 접근 없음) | Spring Modulith 기본 기능 |
| **A-02** | `verifyReadingDoesNotDependOnCardDomain` | **Given** `reading` 모듈 분석<br>**Then** `org.veri.be.card.domain` 패키지의 클래스를 import 하지 않음 | ArchUnit 또는 Modulith |
| **A-03** | `verifyCommentDoesNotDependOnPostDomain` | **Given** `comment` 모듈 분석<br>**Then** `org.veri.be.post.domain` 패키지의 클래스를 import 하지 않음 | ArchUnit 또는 Modulith |

---

## 2. 교차 불변식 & 동기 검증 (Integration Test)

**목표**: 쓰기 요청 시 부모(Reading, Post)의 상태를 **동기적으로** 확인하고, 보안/노출 규칙 위반 시 **즉시 차단**하는지 검증합니다.

### 2.1 Reading ↔ Card (보안 불변식)

| ID | Test Name | 시나리오 (Given-When-Then) | 픽스처 / 데이터 |
| :--- | :--- | :--- | :--- |
| **B-01** | `card_visibility_update_fails_when_reading_is_private` | **Given** `Reading(isPublic=false)` 및 소속 `Card(isPublic=false)` 존재<br>**When** `PATCH /cards/{id}/visibility {isPublic:true}` 요청<br>**Then** 응답 상태 코드 `403 Forbidden` (Code: C1005)<br>**And** DB에서 `card.isPublic`은 여전히 `false` | `privateReading`, `privateCard` |
| **B-02** | `card_creation_fails_when_reading_is_private` | **Given** `Reading(isPublic=false)` 존재<br>**When** `POST /readings/{id}/cards {isPublic:true}` 요청<br>**Then** 응답 상태 코드 `403 Forbidden` (또는 정책에 따라 강제 `false` 생성 확인) | `privateReading` |

### 2.2 Post ↔ Comment (작성 제한)

| ID | Test Name | 시나리오 (Given-When-Then) | 픽스처 / 데이터 |
| :--- | :--- | :--- | :--- |
| **B-03** | `comment_creation_fails_when_post_is_deleted` | **Given** `Post(deletedAt=NOW)` 존재<br>**When** `POST /posts/{id}/comments` 요청<br>**Then** 응답 상태 코드 `404 Not Found` (또는 410 Gone)<br>**And** `Comment` 테이블 count 증가 없음 | `deletedPost` |
| **B-04** | `comment_creation_fails_when_post_is_private` | **Given** `Post(isPublic=false)` 존재, 요청자는 작성자 아님<br>**When** `POST /posts/{id}/comments` 요청<br>**Then** 응답 상태 코드 `403 Forbidden`<br>**And** `Comment` 테이블 count 증가 없음 | `privatePost`, `otherUser` |

---

## 3. 조회 차단 (Safe Query)

**목표**: 이벤트 전파 지연이나 장애 상황에서도, **조회 쿼리 레벨**에서 부모 상태를 확인하여 데이터 노출을 원천 차단하는지 검증합니다.

| ID | Test Name | 시나리오 (Given-When-Then) | 픽스처 / 데이터 |
| :--- | :--- | :--- | :--- |
| **C-01** | `public_feed_excludes_cards_of_private_reading` | **Given** `Reading(isPublic=false)`이지만 `Card(isPublic=true)`인 데이터 강제 삽입 (전파 지연 시뮬레이션)<br>**When** `GET /cards/feed?scope=public` 요청<br>**Then** 결과 리스트에 해당 Card가 포함되지 않음 (0건) | `inconsistentCardData` |
| **C-02** | `comment_list_hides_comments_of_deleted_post` | **Given** `Post(deletedAt=NOW)`이지만 `Comment(deletedAt=null)`인 데이터 존재<br>**When** `GET /posts/{id}/comments` 요청<br>**Then** 결과 리스트가 비어있음 (또는 404 응답) | `orphanCommentData` |
| **C-03** | `my_comments_excludes_comments_on_deleted_posts` | **Given** 내가 쓴 댓글이 `deletedPost`에 존재<br>**When** `GET /me/comments` 요청<br>**Then** 결과 리스트에 해당 댓글이 포함되지 않음 | `myOrphanComment` |

---

## 4. 이벤트 전파 & SLO 검증 (Async Integration)

**목표**: 부모 상태 변경 시 자식 데이터의 상태가 **최종적으로 일치**하는지, 그리고 그 시간이 **SLO(10초)**를 만족하는지 검증합니다.

| ID | Test Name | 시나리오 (Given-When-Then) | 픽스처 / 데이터 |
| :--- | :--- | :--- | :--- |
| **D-01** | `reading_private_event_propagates_to_cards_within_slo` | **Given** `Reading(isPublic=true)`에 `Card(isPublic=true)` 100개 존재<br>**When** `PATCH /readings/{id}/visibility {isPublic:false}` 요청<br>**Then** (Polling) 10초 이내에 `count(card where isPublic=true)`가 0이 됨<br>**And** `ReadingVisibilityChangedEvent` 리스너가 호출됨 확인 | `readingWithManyCards` |
| **D-02** | `propagation_is_idempotent` | **Given** `Reading(isPublic=false)` 상태<br>**When** `ReadingVisibilityChangedEvent(isPublic=false)`를 2회 연속 발행<br>**Then** 에러 없이 처리됨<br>**And** 데이터 상태 변화 없음 (No-Op) | `alreadyPrivateData` |

---

## 5. 카오스/장애 주입 테스트 (Resilience)

**목표**: 이벤트 리스너가 실패하거나 지연될 때도 핵심 불변식(보안)은 지켜지는지 확인합니다.

| ID | Test Name | 시나리오 (Given-When-Then) | 픽스처 / 데이터 |
| :--- | :--- | :--- | :--- |
| **E-01** | `read_safety_maintained_during_propagation_delay` | **Given** `CardModuleListener`에 5초 `Thread.sleep` 주입 (지연)<br>**When** `Reading` 비공개 전환 직후 (1초 뒤) `GET /cards/feed` 요청<br>**Then** 해당 카드가 피드에 노출되지 않음 (조회 차단 작동 확인) | `mockListenerWithDelay` |
| **E-02** | `write_validation_maintained_during_propagation_failure` | **Given** `CardModuleListener`가 예외를 던지도록 설정 (전파 실패)<br>**When** `Reading` 비공개 전환 요청 -> (실패 로그 발생) -> `PATCH /cards/{id}/visibility {true}` 요청<br>**Then** 카드 공개 전환 요청은 여전히 거부됨 (동기 검증 작동 확인) | `mockListenerWithFailure` |

---

## 6. 테스트 구현 가이드 (Kotlin)

### 6.1 Modulith 테스트 예시
```kotlin
@ApplicationModuleTest
class ArchitectureTest {
    @Test
    fun verifyModulith(modules: ApplicationModules) {
        modules.verify() // A-01
    }
}
```

### 6.2 조회 차단 테스트 예시 (C-01)
```kotlin
@Test
fun `public feed excludes cards of private reading`() {
    // Given: Inconsistent state (Simulating lag)
    val privateReading = readingRepository.save(ReadingFixture.private())
    // Forcefully save a public card (bypassing service validation for test setup)
    val publicCard = cardRepository.save(CardFixture.public(reading = privateReading))

    // When
    val result = publicCardFeedQueryRepository.findPublicCards(PageRequest.of(0, 10))

    // Then
    assertThat(result.content).extracting("id").doesNotContain(publicCard.id)
}
```

### 6.3 전파 SLO 테스트 예시 (D-01)
```kotlin
@Test
fun `reading private event propagates to cards within slo`() {
    // Given
    val reading = readingRepository.save(ReadingFixture.public())
    cardRepository.saveAll((1..100).map { CardFixture.public(reading = reading) })

    // When
    readingService.changeVisibility(reading.id, false)

    // Then (Awaitility 사용 권장)
    await().atMost(10, TimeUnit.SECONDS).until {
        cardRepository.countByReadingIdAndIsPublicTrue(reading.id) == 0L
    }
}
```
