# Spring Modulith & jMolecules 아키텍처 제안서

이 문서는 Veri-BE 프로젝트를 기존의 계층형 아키텍처에서 **Spring Modulith**와 **jMolecules**를 사용하는 **모듈러 모놀리스(Modular Monolith)**로 전환하기 위한 아키텍처 제안을 개략적으로 설명합니다.

## 1. 아키텍처 원칙

### 1.1 핵심 목표
1.  **기능별 패키징 (Package by Feature)**: 최상위 패키지는 계층(`controller`, `service`)이 아닌 비즈니스 모듈(`member`, `reading`, `post`)을 나타냅니다.
2.  **엄격한 도메인/인프라 분리**:
    *   **도메인 모델**: 비즈니스 규칙을 포함하는 순수 자바 객체(POJO/Record). 프레임워크 어노테이션(JPA, Jackson 등)을 사용하지 않습니다.
    *   **영속성 모델**: 데이터베이스 매핑만을 담당하는 JPA 엔티티(`@Entity`).
3.  **명시적 아키텍처**: `jMolecules` 어노테이션을 사용하여 설계를 코드에 드러내고 검증 가능하게 만듭니다.
4.  **이벤트 기반 결합도 낮추기**: 도메인 이벤트를 사용하여 모듈 간의 부수 효과를 처리합니다 (예: Reading 공개 여부 변경 -> Card 공개 여부 변경).

---

## 2. 상위 레벨 모듈 구조

애플리케이션은 다음과 같은 논리적 모듈로 구성됩니다. Spring Modulith를 사용하여 이들은 최상위 패키지가 됩니다.

```text
org.veri.be
├── auth            (인증, 토큰, OAuth)
├── member          (사용자 프로필, 계정 관리)
├── book            (책 카탈로그, 검색 - 지원 서브도메인)
├── reading         (독서 진행, 서재 - 핵심 도메인)
├── card            (노트, 메모 - 핵심 도메인)
├── post            (소셜 피드, 공유)
├── comment         (소셜 상호작용)
└── common          (공유 커널, 원시 타입)
```

---

## 3. 구현 패턴: 도메인 vs. 인프라

요청된 분리를 달성하기 위해 각 모듈은 다음과 같은 헥사고날(유사) 내부 구조를 따릅니다.

### 예시: Member 모듈

**현재 상태 (혼합):**
`Member.java`가 도메인 엔티티와 데이터베이스 테이블 역할을 모두 수행함.

**제안 상태 (분리):**

#### A. 순수 도메인 모델
위치: `org.veri.be.member.domain`
*   `@Entity`, `@Column` 없음.
*   jMolecules `@AggregateRoot` 사용.
*   순수 로직 포함 (`updateInfo`, `authorize`).

```java
package org.veri.be.member.domain;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

@AggregateRoot
public class Member {
    @Identity
    private final MemberId id;
    private String nickname;
    private String email;
    private ProfileImage profileImage;
    private AuthProvider provider;

    // 불변식 로직
    public void updateInfo(String newNickname, String newImageUrl) {
        // 비즈니스 유효성 검사만 수행
        if (newNickname == null || newNickname.isBlank()) {
            throw new DomainException("Nickname cannot be empty");
        }
        this.nickname = newNickname;
        this.profileImage = new ProfileImage(newImageUrl);
    }
}
```

#### B. 영속성 모델 (인프라)
위치: `org.veri.be.member.adapter.out.persistence`
*   표준 Spring Data JPA.
*   데이터베이스 테이블에 매핑.

```java
package org.veri.be.member.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "member")
public class MemberJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nickname")
    private String nickname;
    
    // ... 매핑
    
    // 매퍼 메서드: JpaEntity -> Domain
    public Member toDomain() { ... }
    
    // 매퍼 메서드: Domain -> JpaEntity
    public static MemberJpaEntity from(Member member) { ... }
}
```

#### C. 리포지토리 포트 & 어댑터
*   **포트 (도메인)**: `interface MemberRepository` (`Member` 도메인 객체 반환).
*   **어댑터 (인프라)**: `class MemberPersistenceAdapter implements MemberRepository` (JpaRepository 사용, Entity <-> Domain 변환).

---

## 4. 리팩터링된 도메인 모델 (jMolecules)

코드베이스에서 추출한 핵심 도메인을 jMolecules를 사용하여 모델링하는 방법은 다음과 같습니다.

### 4.1 Member 모듈
*   **Aggregate Root**: `Member`
*   **Value Objects**: 
    *   `MemberId` (Typed ID)
    *   `AuthProvider` (ProviderType + ProviderId)
*   **Events**: `MemberInfoUpdatedEvent`

### 4.2 Reading 모듈 (핵심)
*   **Aggregate Root**: `Reading`
*   **Value Objects**:
    *   `ReadingId`
    *   `ReadingPeriod` (startedAt, endedAt)
    *   `ReadingScore`
*   **핵심 변경 사항**: 
    *   `Reading` 도메인 모델에서 **`List<Card>` 제거**. `Reading` 애그리거트는 물리적으로 `Card` 애그리거트를 포함해서는 안 됩니다.
    *   대신 `Card`가 `ReadingId`를 참조합니다.
*   **비즈니스 로직**:
    *   `start()`, `finish()` 메서드는 `ReadingPeriod`를 엄격하게 조작합니다.
    *   `status()`는 `ReadingPeriod`를 기반으로 한 파생 속성입니다.

### 4.3 Card 모듈
*   **Aggregate Root**: `Card`
*   **참조**: `ReadingId` (Reading과 연관), `MemberId` (소유자).
*   **규칙**:
    *   "Reading이 비공개면 Card는 공개될 수 없다"는 규칙을 위해 Card 모듈은 Reading의 상태를 알아야 합니다.
    *   **해결책**: `CardService`가 `ReadingQueryPort`를 호출하여 가시성을 확인하거나 이벤트를 수신합니다.

### 4.4 Post & Comment 모듈
*   **Aggregate Root**: `Post`
    *   `List<PostImage>` 포함 (애그리거트 내 엔티티).
    *   **리팩터링**: `Comment`는 현재 별도의 Entity이지만 밀접하게 연결되어 있습니다. 중첩 구조와 독립적인 수명 주기(댓글만 따로 로드 등)를 고려할 때, `Comment`는 `PostId`를 참조하는 자체적인 **Aggregate Root**로 동작해야 합니다.
*   **Aggregate Root**: `Comment`
    *   `PostId` 참조.
    *   `ParentCommentId` 참조 (답글용).

---

## 5. 도메인 이벤트를 통한 결합도 해결

현재 코드는 강한 결합을 가지고 있습니다: `Reading.setPrivate()`이 `cards.forEach(Card::setPrivate)`를 호출합니다.
Spring Modulith에서는 이러한 물리적 의존성을 끊습니다.

### 시나리오: 공개 범위 업데이트

1.  **액션**: 사용자가 `ReadingService.changeVisibility(readingId, private)`를 호출.
2.  **Reading 도메인**:
    ```java
    // Reading.java (Domain)
    public void setPrivate() {
        this.isPublic = false;
        // 이벤트 등록
        registerEvent(new ReadingVisibilityChangedEvent(this.id, false));
    }
    ```
3.  **영속성**: `ReadingRepository`가 Reading 상태를 저장. 이벤트가 발행됨 (Spring Modulith 자동 처리).
4.  **Card 모듈 리스너**:
    ```java
    @ApplicationModuleListener
    public void on(ReadingVisibilityChangedEvent event) {
        if (!event.isPublic()) {
            cardRepository.updateVisibilityByReadingId(event.readingId(), false);
        }
    }
    ```
5.  **이점**: `reading` 모듈은 더 이상 `card` 클래스를 import 하지 않습니다. 의존성이 역전되었습니다.

---

## 6. 마이그레이션 단계 (이상적인 접근)

1.  **의존성**: `spring-modulith-starter-core`, `spring-modulith-starter-jpa`, `jmolecules-bom` 추가.
2.  **패키지 구조조정**: 파일을 `org.veri.be.{module}` 패키지로 이동.
3.  **도메인 추출**:
    *   `domain` 하위 패키지 생성.
    *   현재 `@Entity` 클래스의 로직을 복사하여 순수 클래스 생성.
    *   현재 `@Entity` 클래스들을 `adapter.out.persistence`의 단순 데이터 홀더로 격하.
4.  **매퍼 구현**: Domain과 Entity 간의 변환기 작성 (MapStruct 또는 수동).
5.  **서비스 리팩터링**: 서비스가 도메인 리포지토리를 사용하도록 업데이트.
6.  **이벤트 통합**: 도메인 간 직접 호출을 `@ApplicationModuleListener`로 대체.

---

## 7. 실용적인 마이그레이션 및 결정 기록 (Pragmatic Migration & Decision Records)

이 섹션은 비용과 현실성을 고려하여 "순수 도메인 + 완전 분리"를 단계적으로 도입하기 위한 전략과 주요 아키텍처 의사결정을 기록합니다.

### 7.1 단계별 마이그레이션 전략 (추천)

"한 번에 모두 분리"하는 대신, 다음 단계에 따라 점진적으로 진행하는 것이 안전합니다.

*   **1단계 (즉시 효과/저위험): VO 도입 및 불변식 이동**
    *   JPA 엔티티(`@Entity`)를 유지하되, 내부 필드를 원시 타입에서 VO로 교체합니다.
    *   **목표**: `Reading`이 `status`를 스스로 계산하고, `Member`가 `Nickname` 객체를 통해 유효성을 보장하게 만듭니다.
    *   **대상**:
        *   `Member`: `Nickname`, `ProfileImage`
        *   `Reading`: `ReadingScore`, `ReadingPeriod`
        *   `Post`: `PostContent`

*   **2단계 (경계 확정): Modulith 테스트로 모듈 경계 강화**
    *   `ApplicationModules.of(Application.class).verify()`를 도입하여 모듈 간 불법적인 `import`를 막습니다.
    *   순환 참조나 잘못된 의존성을 컴파일/테스트 단계에서 차단합니다.

*   **3단계 (결합도 해소): 도메인 이벤트 도입**
    *   `Reading`이 `Card` 리스트를 직접 가지는 연관관계를 끊고, `ID 참조`로 변경합니다.
    *   `Reading` 상태 변경 시 `Card` 업데이트를 위해 도메인 이벤트를 발행합니다.

*   **4단계 (선택적): 순수 도메인과 JPA 엔티티 분리**
    *   비즈니스 로직이 매우 복잡해져서 영속성 계층과 분리가 절실한 모듈에 한해 선택적으로 적용합니다.

### 7.2 주요 의사결정 기록 (ADR)

#### (A) 교차 애그리거트 일관성 수준 (Consistency Level)
서로 다른 애그리거트 간의 규칙(예: Reading이 비공개면 Card도 비공개)을 어떻게 보장할 것인가?

*   **결정**: **작업의 성격에 따라 동기 검증과 최종 일관성을 분리**하여 적용합니다.
*   **상세**:
    *   **쓰기 경로 (Card 공개 전환)**: **동기 검증 (Synchronous Validation)**. Card를 `public`으로 설정하려는 요청은 반드시 `Reading`의 상태를 조회(Port)하여, 비공개 상태라면 **즉시 실패**시켜야 합니다. 보안/노출 규칙은 타협하지 않습니다.
    *   **전파 경로 (Reading 비공개 전환)**: **최종 일관성 (Eventual Consistency)**. `Reading`이 비공개로 전환될 때 수많은 `Card`를 업데이트하는 것은 도메인 이벤트를 통해 비동기적으로 처리합니다. (UI에서 수 초 내의 짧은 지연은 허용).
    *   **Post ↔ Comment 접근 제어**: 댓글 삭제/숨김 전파는 이벤트로 처리하되, **조회 시점에는 항상 Post의 공개 상태를 검증**하거나 조인하여, Post가 비공개/삭제된 경우 댓글이 노출되지 않도록 **즉시 차단**합니다.

#### (B) 애그리거트 경계 원칙
*   **원칙**: **"하나의 트랜잭션 내에서 즉시 일관성이 보장되어야 하는 것만 애그리거트 내부에 둔다."**
*   **적용**:
    *   `Post`와 `Comment`: 댓글은 독립적으로 로드되고 작성되므로 별도의 애그리거트로 분리합니다. 단, `PostId`를 통한 약한 참조를 유지합니다.
    *   `Reading`과 `Card`: 카드의 생명주기는 독서에 종속적이지 않으므로(독서가 끝나도 카드는 남을 수 있음 등), 분리하는 것이 유리합니다.

#### (C) 정책 상수의 소유권
*   **문제**: 이미지 용량(1MB vs 3MB), 만료 시간 등의 상수가 여러 곳에 흩어져 있음.
*   **결정**: **기술적 제약과 비즈니스 제약을 분리**하여 소유권을 정의합니다.
*   **상세**:
    *   **Common (Shared Kernel)**: **기술적 제약 (Technical Constraints)**만 정의합니다. (예: 인프라 업로드 허용 한계, Presigned URL TTL 상한, 지원하는 MIME 타입 등).
    *   **Module Policy**: **비즈니스 제약 (Business Constraints)**만 정의합니다. (예: `CardPolicy.MAX_IMAGE_SIZE = 3MB`, `PostPolicy.MAX_IMAGE_SIZE = 1MB`).
    *   이렇게 함으로써 인프라 변경과 기획 변경의 영향 범위를 격리합니다.
