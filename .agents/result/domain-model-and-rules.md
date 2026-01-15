# 도메인 모델 및 규칙 문서

이 문서는 2026년 1월 기준으로 코드베이스에서 추출한 도메인 모델, 속성, 그리고 비즈니스 규칙에 대한 포괄적인 개요를 제공합니다.

## 1. 도메인 모델 (엔티티)

### 1.1 Member (회원)
시스템의 사용자를 나타냅니다.
- **속성**:
  - `id` (Long, PK)
  - `email` (String)
  - `nickname` (String)
  - `profileImageUrl` (String, 최대 2083자)
  - `providerId` (String)
  - `providerType` (Enum: `ProviderType.KAKAO`)
- **주요 동작**:
  - `Authorizable` 구현.
  - `updateInfo`: 닉네임과 프로필 이미지를 업데이트합니다.
  - `authorizeMember`: 요청자 ID가 회원 ID와 일치하는지 확인합니다.

### 1.2 Book (책)
책 카탈로그 항목을 나타냅니다.
- **속성**:
  - `id` (Long, PK)
  - `title`, `author`, `publisher` (String)
  - `isbn` (String, 고유 식별자)
  - `image` (String, URL)
- **규칙**:
  - 책은 `isbn`으로 식별되며 중복이 제거됩니다.

### 1.3 Reading (독서)
특정 책에 대한 회원의 진행 상황과 상호작용을 추적합니다.
- **속성**:
  - `id` (Long, PK)
  - `member` (Member, ManyToOne)
  - `book` (Book, ManyToOne)
  - `score` (Double)
  - `startedAt`, `endedAt` (LocalDateTime)
  - `status` (Enum: `ReadingStatus`)
  - `isPublic` (boolean, 기본값 `true`)
  - `cards` (List<Card>)
- **Enums**:
  - `ReadingStatus`: `NOT_START` (읽기 전), `READING` (읽는 중), `DONE` (완독)
- **주요 동작**:
  - **상태 로직**:
    - `DONE`: `endedAt`이 존재하는 경우.
    - `READING`: `startedAt`은 있지만 `endedAt`이 없는 경우.
    - `NOT_START`: 둘 다 없는 경우.
  - **공개 범위 전파**: `Reading`을 비공개(`setPrivate()`)로 설정하면 연관된 모든 `Card`도 자동으로 비공개 설정됩니다.
  - **진행 상황 업데이트**: 점수와 날짜를 업데이트한 후 상태를 재계산합니다.

### 1.4 Card (카드/기록)
사용자가 책을 읽는 동안 작성한 메모나 이미지 캡처입니다.
- **속성**:
  - `id` (Long, PK)
  - `content` (String, TEXT)
  - `image` (String, URL)
  - `reading` (Reading, ManyToOne)
  - `member` (Member, ManyToOne)
  - `isPublic` (boolean, 기본값 `false`)
- **주요 동작**:
  - **공개 범위 제약**: 상위 `Reading`이 `private`인 경우 `public`으로 설정할 수 없습니다. `READING_MS_NOT_PUBLIC` (C1005) 예외를 발생시킵니다.
  - **생성**: `Reading`의 공개 설정을 상속받습니다 (AND 로직).
  - **권한 부여**: 소유자(`member`)만이 업데이트하거나 삭제할 수 있습니다.

### 1.5 Post (포스트/게시물)
책을 공유하는 소셜 피드 항목입니다.
- **속성**:
  - `id` (Long, PK)
  - `title` (String, 최대 50자)
  - `content` (String, TEXT)
  - `author` (Member, ManyToOne)
  - `book` (Book, ManyToOne)
  - `isPublic` (boolean, 기본값 `true`)
  - `images` (List<PostImage>)
  - `comments` (List<Comment>)
- **주요 동작**:
  - `publishBy` / `unpublishBy`: 공개 여부를 토글합니다 (권한 필요).
  - `addImage`: 표시 순서와 함께 이미지를 추가합니다.

### 1.6 Comment (댓글)
포스트에 대한 상호작용입니다. 대댓글(중첩)을 지원합니다.
- **속성**:
  - `id` (Long, PK)
  - `content` (String, TEXT)
  - `post` (Post, ManyToOne)
  - `author` (Member, ManyToOne)
  - `parent` (Comment, ManyToOne, 답글용)
  - `replies` (List<Comment>)
  - `deletedAt` (LocalDateTime)
- **주요 동작**:
  - **소프트 삭제**: `deleteBy`는 레코드를 삭제하는 대신 `deletedAt` 타임스탬프를 설정합니다.
  - `isDeleted()`는 `deletedAt` 존재 여부를 확인합니다.
  - `replyBy`: 대댓글을 생성합니다.

### 1.7 Image / OcrResult
- **Image**: 회원과 연결된 일반적인 이미지 참조를 저장합니다.
- **OcrResult**: OCR 처리 데이터(`imageUrl`, `preProcessedUrl`, `resultText`)를 저장합니다.

### 1.8 Auth Tokens (인증 토큰)
- **RefreshToken**: `token` (String, 512자)과 `expiredAt`을 저장합니다.
- **BlacklistedToken**: 무효화된 토큰과 그 `expiredAt`을 저장합니다.

---

## 2. 비즈니스 규칙 및 로직

### 2.1 인증 및 보안
- **토큰 관리**:
  - 로그인은 Access 토큰과 Refresh 토큰을 발급합니다.
  - **블랙리스팅**: 로그아웃 시 Access 및 Refresh 토큰 모두 자연 만료 시점까지 블랙리스트에 추가됩니다.
  - **재발급**: 새로운 Access 토큰을 발급하기 전에 스토리지 및 블랙리스트에서 Refresh 토큰을 검증합니다.
- **OAuth2**:
  - 카카오를 지원합니다.
  - **닉네임 중복**: 새 사용자의 닉네임이 이미 존재하는 경우, 고유성을 보장하기 위해 `_{timestamp}`를 추가합니다.

### 2.2 책 및 독서 관리
- **책 생성**:
  - 생성 전 ISBN으로 기존 책을 확인합니다.
  - 데이터는 네이버 도서 검색 API를 사용합니다.
- **서재 (Reading)**:
  - **중복 방지**: 동일한 사용자가 동일한 책을 서재에 두 번 추가할 수 없습니다 (기존 항목 반환).
  - **상태 전이**: 명시적인 `start()` 및 `finish()` 메서드가 타임스탬프를 설정하고 상태(`READING`, `DONE`)를 업데이트합니다.

### 2.3 콘텐츠 생성 및 제한
- **카드 이미지**:
  - 최대 크기: **3 MB**.
  - 유형: 이미지만 가능.
  - Presigned URL 만료: **5분**.
- **포스트 이미지**:
  - 최대 크기: **1 MB** (참고: `PostCommandService`는 `Card`의 `3 * MB`와 달리 `MB` 상수를 사용함).
  - 유형: 이미지만 가능.
- **유효성 검사**:
  - **Card**: `content`는 비어 있을 수 없습니다. `memberBookId`는 필수입니다.
  - **Member**: `nickname`은 비어 있을 수 없습니다. `profileImageUrl`은 유효한 URL이어야 합니다.
- **좋아요 로직**: 토글 동작 (없으면 좋아요, 있으면 좋아요 취소).

### 2.4 오류 처리 (도메인별)
- **Card**:
  - `C1003`: 이미지가 너무 큽니다.
  - `C1004`: 지원하지 않는 이미지 타입입니다.
  - `C1005`: 독서 기록이 비공개 상태입니다 (카드를 공개로 설정 불가).
- **Book**:
  - `B1003`: 이미 등록된 책입니다.
- **Member**:
  - `M1003`: 이미 사용 중인 닉네임입니다.

## 3. 열거형 (Enumerations)

### 3.1 ReadingStatus
- `NOT_START`
- `READING`
- `DONE`

### 3.2 ProviderType
- `KAKAO`