# 풍부한 도메인(Rich Domain) & 값 객체(VO) 마이그레이션 제안서

이 문서는 현재의 비즈니스 로직을 분석하고 **값 객체(Value Objects, VOs)**를 갖춘 **풍부한 도메인 모델(Rich Domain Model)**을 채택하기 위한 구체적인 리팩터링 방안을 제안합니다. 목표는 유효성 검사와 상태 전이 로직을 서비스 계층에 분산시키는 대신 도메인 객체 내부에 캡슐화하는 것입니다.

## 1. Member 도메인

### 현재 상태
- `Member` 엔티티가 원시 타입들을 포함: `nickname` (String), `email` (String), `profileImageUrl` (String).
- 유효성 검사(예: 빈 값 확인)가 DTO나 서비스에서 부분적으로 발생.
- 공급자(Provider) 정보가 분리됨: `providerId`, `providerType`.

### 제안된 마이그레이션

#### A. 값 객체 (Value Objects)
| VO 이름 | 필드 | 불변식 / 로직 |
| :--- | :--- | :--- |
| **`Nickname`** | `value` (String) | • null이거나 공백일 수 없음.<br>• 최대 길이 검증.<br>• 비속어 필터링 (향후). |
| **`ProfileImage`** | `url` (String) | • 유효한 URL 형식이어야 함.<br>• 기본 이미지 처리. |
| **`AuthProvider`** | `id`, `type` | • OAuth 상세 정보를 함께 캡슐화. |

#### B. 풍부한 도메인 로직
*   **이동**: `updateInfo(String, String)` → `updateProfile(Nickname, ProfileImage)`
    *   **이점**: `Member` 엔티티는 잘못된 닉네임을 절대 가지지 않음을 보장합니다.

```java
// 변경 전: 서비스 유효성 검사
if (request.nickname() == null) throw new Exception();
member.updateInfo(request.nickname(), ...);

// 변경 후: 도메인 유효성 검사
member.updateProfile(new Nickname(request.nickname()), ...); // VO 생성자가 유효하지 않으면 예외 발생
```

---

## 2. Reading (Book) 도메인

### 현재 상태
- `Reading` 엔티티가 `score`, `startedAt`, `endedAt`, `status`를 개별적으로 관리.
- `decideStatus()` 로직이 존재하지만 원시 날짜 데이터로 작동함.
- `BookshelfService`가 `updateProgress`, `start`, `finish`를 조정함.

### 제안된 마이그레이션

#### A. 값 객체 (Value Objects)
| VO 이름 | 필드 | 불변식 / 로직 |
| :--- | :--- | :--- |
| **`ReadingPeriod`** | `startedAt`, `endedAt` | • `endedAt`은 `startedAt`보다 이전일 수 없음.<br>• `ReadingStatus`를 계산 (파생 속성).<br>• `isDone()`, `isReading()` 로직 포함. |
| **`ReadingScore`** | `value` (Double) | • 범위 확인 (0.0 - 5.0). |

#### B. 풍부한 도메인 로직
*   **리팩터링**: Entity에서 `status` 필드 제거 (또는 파생/저장 전용으로 변경).
*   **로직**:
    *   `Reading.start()`: `ReadingPeriod` 업데이트.
    *   `Reading.finish()`: `ReadingPeriod` 업데이트.
    *   **상태 계산**: `ReadingPeriod` 내부로 완전히 이동.

```java
// 제안
public class Reading {
    private ReadingPeriod period; // VO
    private ReadingScore score;   // VO

    public ReadingStatus getStatus() {
        return period.deriveStatus();
    }
    
    public void finish(Clock clock) {
        this.period = this.period.finish(LocalDateTime.now(clock));
    }
}
```

---

## 3. Card 도메인

### 현재 상태
- `Card`가 `content`, `image`를 가짐.
- 가시성 로직(`isPublic`)이 `Reading`과 강하게 결합됨.
- `CardCommandService`가 카드를 공개로 설정하기 전에 `reading.isPublic()`을 확인함.

### 제안된 마이그레이션

#### A. 값 객체 (Value Objects)
| VO 이름 | 필드 | 불변식 / 로직 |
| :--- | :--- | :--- |
| **`CardContent`** | `text`, `imageUrl` | • `text` 비어있지 않음.<br>• `imageUrl` 유효한 URL.<br>• "텍스트 업데이트" 로직이 새 VO 반환. |
| **`Visibility`** | `isPublic` (boolean) | • boolean 플래그 캡슐화 (나중에 특정 범위로 확장 가능). |

#### B. 풍부한 도메인 로직
*   **불변식**: "Reading이 비공개면 Card는 공개될 수 없다"는 규칙은 **애그리거트 간 불변식(Cross-Aggregate Invariant)**입니다.
    *   **서비스 계층**: `Reading` (또는 그 상태)을 `Card` 도메인 메서드에 전달하여 검증.
    *   **도메인**: `card.changeVisibility(newVisibility, parentReadingVisibility)`
    *   **로직**: 서비스가 미리 확인하는 것이 아니라, 조합이 유효하지 않으면 `Card` 엔티티 자체가 예외를 발생시킵니다.

---

## 4. Post & Comment 도메인

### 현재 상태
- `Post`가 `PostImage` 목록을 관리.
- `Comment`가 계층 구조 로직을 처리.
- `PostCommandService`가 수동 이미지 추가 루프로 `Post`를 생성함.

### 제안된 마이그레이션

#### A. 값 객체 (Value Objects)
| VO 이름 | 필드 | 불변식 / 로직 |
| :--- | :--- | :--- |
| **`PostImages`** | `List<PostImage>` | • 일급 컬렉션(First Class Collection).<br>• 최대 이미지 수 제한 (예: 10장).<br>• 순서 유지 (인덱스). |
| **`PostContent`** | `title`, `body` | • 제목 길이 제한 (50자). |

#### B. 풍부한 도메인 로직
*   **팩토리 메서드**: `Post.create(Author, Book, PostContent, List<String> imageUrls)`
    *   생성 로직(이미지 반복, 순서 설정)을 서비스에서 **도메인 팩토리** 또는 **엔티티 생성자**로 이동.
*   **댓글 계층 구조**:
    *   `Comment` 엔티티는 이미 잘 수행 중 (`replyBy`).
    *   개선점: 빈 값 확인 및 최대 길이를 처리하는 `CommentContent` VO 도입.

---

## 5. 이점 요약

1.  **빠른 실패 (Fail-Fast)**: 잘못된 데이터(예: 빈 닉네임, 미래의 종료 날짜)가 VO 생성 시점에 포착되어 도메인 로직으로 진입하는 것을 방지합니다.
2.  **테스트 용이성**: `ReadingPeriod`나 `Nickname` 같은 VO는 리포지토리 모킹 없이 격리된 상태에서 단위 테스트가 가능합니다.
3.  **표현력**: 메서드 시그니처가 자체적으로 문서화됩니다 (예: `update(Double)` 대신 `update(ReadingScore)`).
4.  **응집도**: 현재 `Service`에 있는 로직(예: ReadingStatus 계산)이 데이터가 있는 곳으로 이동합니다.

## 6. 구현 우선순위

1.  **1단계 (저위험)**: `Nickname`, `ProfileImage` (Member) 및 `ReadingScore` (Reading) 적용.
2.  **2단계 (로직 집중)**: `ReadingPeriod`를 리팩터링하여 상태 계산 로직 캡슐화.
3.  **3단계 (구조적)**: `Post` 이미지 처리를 일급 컬렉션으로 리팩터링.