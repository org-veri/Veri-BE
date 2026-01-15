# ADR 구현 가이드: 교차 애그리거트 일관성 및 쿼리 전략

본 문서는 **7.2 주요 의사결정 기록 (ADR)**에 명시된 정책(동기 검증, 최종 일관성, 조회 차단)을 실제 코드 레벨에서 강제하기 위한 구체적인 구현 가이드입니다.

## 1. Reading ↔ Card 관계 구현

### 1.1 데이터 모델 및 엔티티 변경

**목표**: `Reading`과 `Card`의 물리적 결합(`@OneToMany`)을 끊고, 조회 시에만 조인하여 정합성을 보장한다.

#### (1) Reading Entity
*   `List<Card> cards` 필드를 **삭제**합니다. (Aggregate Boundary 분리)
*   **이유**: `Reading` 로드 시 불필요한 `Card` 로딩 방지 및 도메인 결합 제거.

#### (2) Card Entity
*   `Reading` 참조는 유지하되(`@ManyToOne`), 조회 최적화를 위한 인덱스 설계가 필수적입니다.
*   **권장 인덱스**:
    *   `idx_card_reading` : `(reading_id)` - *전파 경로(Bulk Update)용*
    *   `idx_card_public_created` : `(is_public, created_at DESC)` - *공개 피드용 (단, Reading 조인 필수)*

### 1.2 쓰기 경로 (동기 검증: "공개 불변식")

Card를 `public`으로 생성하거나 변경할 때, **반드시 Reading의 상태를 확인**해야 합니다.

#### (A) Card 공개 전환 (`PATCH /cards/{id}/visibility`)
*   **트랜잭션**: `CardCommandService` 트랜잭션 내에서 실행.
*   **구현**:
    ```java
    // CardCommandService.java
    @Transactional
    public void changeVisibility(Long cardId, boolean isPublic) {
        Card card = cardRepository.findById(cardId)...;
        
        if (isPublic) {
            // [중요] ReadingPort를 통해 최신 상태 동기 조회
            boolean isReadingPublic = readingQueryPort.isPublic(card.getReadingId());
            if (!isReadingPublic) {
                throw new ApplicationException(CardErrorInfo.READING_MS_NOT_PUBLIC); // 즉시 실패
            }
        }
        card.changeVisibility(isPublic);
    }
    ```

### 1.3 전파 경로 (최종 일관성: "비공개 전파")

Reading이 `private`로 변할 때, 수십~수백 개의 카드를 한 번에 업데이트하는 것은 **비동기**로 처리합니다.

#### (A) 이벤트 발행 및 처리
1.  `Reading` 상태 변경 후 `ReadingVisibilityChangedEvent` 발행.
2.  `Card` 모듈의 리스너가 이를 수신.
3.  **Idempotent Bulk Update** 실행:
    ```sql
    -- 이미 private인 것은 건드리지 않음 (DB 부하 최소화)
    UPDATE card SET is_public = false 
    WHERE reading_id = :readingId AND is_public = true;
    ```
4.  **SLO 준수**: 이 작업은 99.9% 확률로 10초 이내에 완료되어야 합니다.

### 1.4 조회 경로 (안전 장치: "조회 차단")

이벤트 전파 지연 시간(최대 10초) 동안에도 **비공개 Reading의 카드가 노출되어서는 안 됩니다.**

#### (A) 안전한 쿼리 레포지토리 (PublicCardFeedQueryRepository)
*   모든 공개 카드 조회 쿼리는 **반드시 Reading을 조인**해야 합니다.
*   **잘못된 예 (현재 코드)**:
    ```sql
    SELECT c FROM Card c WHERE c.isPublic = true -- 위험: Reading이 private여도 노출됨
    ```
*   **올바른 예 (구현 가이드)**:
    ```sql
    SELECT c FROM Card c 
    JOIN c.reading r 
    WHERE c.isPublic = true AND r.isPublic = true -- 안전: 즉시 차단됨
    ```
*   **강제 방안**: 공개 피드 조회용 별도 Repository(`PublicCardFeedQueryRepository`)를 만들고, API는 이것만 사용하도록 강제합니다.

---

## 2. Post ↔ Comment 관계 구현

### 2.1 데이터 모델 및 엔티티 변경

**목표**: `Post`와 `Comment`를 독립된 Aggregate로 분리. `Post` 삭제 시 `Comment`가 고아 객체가 되지 않도록 논리적 연결만 유지.

#### (1) Post Entity
*   `List<Comment> comments` 필드 **삭제** 권장 (또는 읽기 전용으로만 유지).
*   이유: 댓글이 수천 개일 때 `Post` 로딩 성능 저하 방지.

#### (2) Comment Entity
*   `Post` 참조 유지.
*   **권장 인덱스**:
    *   `idx_comment_post` : `(post_id, created_at ASC)` - *댓글 목록 조회용*

### 2.2 쓰기 경로 (동기 검증)

#### (A) 댓글 작성 (`POST /posts/{postId}/comments`)
*   **검증 로직**:
    *   `Post`가 존재하는가?
    *   `Post`가 삭제되지 않았는가? (`deleted_at IS NULL`)
    *   (정책) `Post`가 비공개인가? -> 비공개면 댓글 작성 불가 처리.
*   이 검증은 `CommentService`에서 `PostQueryPort`를 통해 수행합니다.

### 2.3 전파 및 조회 경로

#### (A) 조회 차단 (Access Blocking)
*   Post가 삭제되거나 비공개 처리된 직후, 댓글 삭제 이벤트가 돌기 전이라도 **댓글은 조회되지 않아야 합니다.**
*   **필수 쿼리 패턴**:
    ```sql
    SELECT c FROM Comment c 
    JOIN c.post p 
    WHERE c.post.id = :postId 
      AND p.deletedAt IS NULL 
      AND (p.isPublic = true OR p.author.id = :currentUserId) -- 작성자는 볼 수 있음
    ```
*   **구현**: `CommentRepository`의 조회 메서드는 항상 `JOIN Post`를 포함하도록 수정해야 합니다.

---

## 3. 요약: 개발자가 지켜야 할 3가지 규칙

1.  **쓰기는 깐깐하게**: `public`으로 전환하거나 생성할 때는 부모(Reading/Post) 상태를 **동기적**으로 확인하고 실패시켜라.
2.  **전파는 느긋하게**: 부모가 `private/deleted`가 되면 자식 상태 변경은 **이벤트**로 던져라. (DB 락 방지)
3.  **조회는 의심하라**: `isPublic=true`만 믿지 말고, **부모 테이블을 조인**해서 부모 상태(`r.isPublic`, `p.deletedAt`)를 같이 확인하라.
