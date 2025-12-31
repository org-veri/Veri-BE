# Veri-BE 도메인 모델 정의서 (최종본)

**버전**: 2.1 (치명적 이슈 수정 + 정책 명확화)
**날짜**: 2025-12-31
**기준**: 실제 엔티티 구조 분석 + 동시성/불변성/설계 충돌 해결

---

## 0. 수정 사항 요약

### v1 → v2 주요 변경

1. **ID 생성 정책**: JPA `@GeneratedValue` 위임, 도메인은 `restore` 전용
2. **불변성 모델**: Kotlin `copy` 패턴 적용, `val` 유지
3. **Post-Comment 관계**: `Post.addComment()` 제거, Comment 독립 AR로 명확화
4. **공개 정책**: Reading 공개가 Card 공개의 전제 조건임을 명시
5. **Book/Member 정의**: JPA Entity이나 도메인 행위 없음을 명확히 구분

### v2 → v2.1 주요 변경 (치명적 이슈 수정)

1. **ID 정책 (치명적 수정)**: `placeholder()` 제거, 영속 전에는 `id`를 `Nullable`로 모델링
   - `data class Reading(val id: ReadingId?, ...)` 형태로 변경
   - `require(value > 0)` 제약 유지, 0L placeholder 제거로 충돌 해결
2. **불변성 모델 (치명적 수정)**: 모든 도메인 POJO를 `data class`로 선언
   - `copy()` 메서드는 data class에서만 자동 생성됨
   - `class Reading(...)` → `data class Reading(...)` 수정
3. **공개 정책 명확화**: 카드 공개/비공개 전이는 Reading(AR)에서만 수행
   - ReadingCard는 순수 상태 객체로, Reading이 호출을 통제
4. **시간 정책 추가**: 도메인 생성 시각은 Application에서 `Clock` 주입으로 결정
5. **JPA 저장 가이드 추가**: aggregate 단위 저장 시 mapper 설계 원칙 명시

---

## 1. 핵심 도메인 관계 (One-Liner)

**Reading**이 "유저-Book 매핑 + Card 파생"의 중심 루트

**Post**는 Book을 직접 참조하는 독립 루트

**Comment**는 Post와 분리된 독립 루트 (대댓글 1단)

**Book**은 공유 레퍼런스 (별도 Catalog 컨텍스트/참조 전용)

---

## 2. Aggregate Root 목록과 하위 관계

### A. Book (Reference / Shared Resource)

**성격**: 공유 자원 (카탈로그)

**정의**: Book은 **JPA Entity로 존재하지만 도메인 행위/규칙을 갖지 않는 Reference 모델**이며, 사용자 도메인의 트랜잭션 루트로 취급하지 않는다.

```java
// 실제 코드 확인
@Entity
@Table(name = "book")
class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String image;

    // 행동 없음, 데이터만 보유
}
```

**권장 전략**:
- **AR로 두지 않음** (별도 Bounded Context)
- Book 자체의 규칙/변경은 최소화
- 외부 동기화 (책 검색 서비스 기원)

**관계**:
```
Reading → Book: bookId (ID 참조)
Post → Book: bookId (ID 참조)
```

**요약**: Book은 "누가 소유하고 변경하는" 루트가 아니라, 조회/참조되는 기준 데이터

---

### B. Reading (AR) = User ↔ Book 매핑 도메인

**성격**: 유저가 특정 Book을 "읽기/정리/아카이빙"하는 도메인 루트

**Card가 여기서 파생**되므로 Reading이 사용자 행위의 중심

```java
// 실제 코드 확인
@Entity
@Table(name = "reading")
class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private ReadingStatus status;
    private boolean isPublic;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Book book;  // ID 참조

    @OneToMany(mappedBy = "reading")
    private List<Card> cards;  // 1:N 하위

    // 핵심 행동
    public void start(Clock clock) { ... }
    public void finish(Clock clock) { ... }
    public void updateProgress(...) { ... }
    public void setPublic() { ... }
    public void setPrivate() {
        this.isPublic = false;
        this.cards.forEach(Card::setPrivate);  // 하위에 전파
    }
}
```

**루트 규칙**:
1. 유저가 해당 book에 대해 reading을 만들 수 있는 조건
2. Reading 상태 전이 (NOT_START → READING → DONE)
3. Reading에 속한 카드 생성/삭제/정렬/제한
4. **공개 정책**: "Reading이 비공개면 하위 카드도 비공개 강제" (v1)

**관계**:
```
Reading(AR)
  ├─→ Member: memberId (Many-to-One)
  ├─→ Book: bookId (Many-to-One, ID 참조)
  └─→ Card: 1:N (하위 Entity)
```

---

### C. Card 전략: Reading 내부 Entity (권장)

**분석 기준**:
- Card는 Reading 없이 존재 의미가 약함
- 대부분의 변경이 Reading 단위로 발생 (정렬, 묶음, 공개/비공개)
- 현재 코드: Card → Reading 종속 (foreignKey 제약)

**권장: Reading(AR) 내부에 ReadingCard(Entity)**

```java
// Reading 도메인 POJO (Kotlin v2.1)
data class Reading(
    val id: ReadingId?,  // v2.1: 영속 전엔 null (placeholder 제거)
    val memberId: Long,
    val bookId: Long,
    val status: ReadingStatus,
    val isPublic: Boolean,
    val startedAt: LocalDateTime?,
    val endedAt: LocalDateTime?,
    val score: Double?,
    val cards: List<ReadingCard>
) {
    // 행동: 상태 전이 (copy 패턴)
    fun start(clock: Clock): Reading {
        val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
        return copy(
            status = ReadingStatus.READING,
            startedAt = now
        )
    }

    fun finish(clock: Clock): Reading {
        val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
        return copy(
            status = ReadingStatus.DONE,
            endedAt = now
        )
    }

    // 행동: 공개/비공개 (하위에 전파)
    fun makePublic(): Reading {
        return copy(
            isPublic = true,
            cards = this.cards.map { it.copy(isPublic = true) }  // v2.1: Reading이 통제
        )
    }

    fun makePrivate(): Reading {
        return copy(
            isPublic = false,
            cards = this.cards.map { it.copy(isPublic = false) }  // v2.1: Reading이 통제
        )
    }

    // 행동: 카드 관리
    fun addCard(card: ReadingCard): Reading {
        validateCardLimit()
        val newCards = this.cards.toMutableList()
        newCards.add(card)
        return copy(cards = newCards)
    }

    fun removeCard(cardId: CardId): Reading {
        val newCards = this.cards.filter { it.id != cardId }
        return copy(cards = newCards)
    }

    fun reorderCards(cardIds: List<CardId>): Reading {
        val cardMap = this.cards.associateBy { it.id }
        val reordered = cardIds.mapNotNull { cardMap[it] }
        return copy(cards = reordered)
    }

    // 규칙 검증
    private fun validateCardLimit() {
        require(this.cards.size < MAX_CARD_COUNT) {
            "최대 100장까지 생성 가능합니다"
        }
    }

    companion object {
        const val MAX_CARD_COUNT = 100

        // v2.1: 영속 전엔 id가 null인 상태로 생성
        fun create(memberId: Long, bookId: Long, clock: Clock): Reading {
            val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
            return Reading(
                id = null,  // 영속 전엔 null (placeholder 제거)
                memberId = memberId,
                bookId = bookId,
                status = ReadingStatus.NOT_START,
                isPublic = true,
                startedAt = null,
                endedAt = null,
                score = null,
                cards = emptyList()
            )
        }

        // v2.1: 영속 후 ID로 복원
        fun restore(
            id: Long,
            memberId: Long,
            bookId: Long,
            status: ReadingStatus,
            isPublic: Boolean,
            startedAt: LocalDateTime?,
            endedAt: LocalDateTime?,
            score: Double?,
            cards: List<ReadingCard>
        ): Reading {
            return Reading(
                id = ReadingId.of(id),  // 영속 후엔 항상 non-null
                memberId = memberId,
                bookId = bookId,
                status = status,
                isPublic = isPublic,
                startedAt = startedAt,
                endedAt = endedAt,
                score = score,
                cards = cards
            )
        }
    }
}

// Reading Card (하위 Entity) v2.1
data class ReadingCard(
    val id: CardId?,  // v2.1: 영속 전엔 null
    val readingId: ReadingId,
    val memberId: Long,
    val content: CardContent,
    val imageUrl: String,
    val isPublic: Boolean
) {
    // v2.1: ReadingCard는 순수 상태 객체 (makePublic/makePrivate 제거)
    // 카드 공개/비공개 전이는 Reading(AR)에서만 수행

    // 행동 (copy 패턴)
    fun updateContent(newContent: CardContent, newImageUrl: String): ReadingCard {
        return copy(
            content = newContent,
            imageUrl = newImageUrl
        )
    }

    // 정책 검증 (v1: isPublic || owner만)
    fun assertReadableBy(requesterId: Long) {
        if (isPublic || this.memberId == requesterId) return
        throw DomainException("NOT_READABLE", "비공개 카드")
        // v1에서는 추가 정책(차단/블라인드)은 Application에서 처리
    }

    companion object {
        // v2.1: 영속 전엔 id가 null인 상태로 생성
        fun create(readingId: ReadingId, memberId: Long, content: CardContent, imageUrl: String): ReadingCard {
            return ReadingCard(
                id = null,
                readingId = readingId,
                memberId = memberId,
                content = content,
                imageUrl = imageUrl,
                isPublic = false
            )
        }

        fun restore(
            id: Long,
            readingId: ReadingId,
            memberId: Long,
            content: String,
            imageUrl: String,
            isPublic: Boolean
        ): ReadingCard {
            return ReadingCard(
                id = CardId.of(id),
                readingId = readingId,
                memberId = memberId,
                content = CardContent.of(content),
                imageUrl = imageUrl,
                isPublic = isPublic
            )
        }
    }
}
```

**관계**:
```
Reading(AR)
  └─→ ReadingCard(Entity): 1:N
      ├─→ Member: memberId (ID 참조)
      └─→ visibility: PUBLIC/PRIVATE
```

**C-2: Card가 독립 AR이 되는 조건** (향후 승격 기준)

Card가 아래 상황에서 독립 생명주기가 강해지면 Card AR로 분리:

1. **이동/재사용**: Reading 간 카드 이동/복사가 활발
2. **공유 기능**: 다른 사용자와 카드 공유/큐레이션
3. **독립 정책**: 카드 단위로 신고/버전/통계가 복잡해짐
4. **커뮤니티 중심**: Public 카드가 핵심 컨텐츠가 되고 Reading은 부가 정보가 됨

**현재**: Reading 내부 Entity로 시작 (안전)

---

### D. Post (AR) = Book 기반 컬럼/게시

**성격**: "읽지 않아도 bookId로 글을 작성"이므로 Reading과 독립

**Post는 Book을 직접 참조**

**설계 결정 (v2)**: **Post에서 `comments` 컬렉션을 제거**, Comment는 완전히 독립 AR

```java
// 실제 코드 확인
@Entity
@Table(name = "post")
class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne
    private Member author;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @JoinColumn(name = "book_id")
    @ManyToOne
    private Book book;  // 직접 참조 (Reading 경유 X)

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images;

    // 제거 예정 (v2): 아래 코드 삭제
    // @OneToMany(mappedBy = "post")
    // private List<Comment> comments;

    @Setter
    private boolean isPublic = true;

    // 제거 예정 (v2): 아래 메서드 삭제
    // public Comment addComment(Comment comment) { ... }

    // 핵심 행동
    public void publishBy(Member member) {
        authorizeOrThrow(member.getId());
        this.isPublic = true;
    }

    public void unpublishBy(Member member) {
        authorizeOrThrow(member.getId());
        this.isPublic = false;
    }
}
```

**Post 도메인 POJO (Kotlin v2.1)**:
```kotlin
// Post 도메인 POJO (v2.1: comments 컬렉션 제거, data class)
data class Post(
    val id: PostId?,  // v2.1: 영속 전엔 null
    val authorId: Long,
    val bookId: Long,  // Reading 없이 직접 참조
    val title: PostTitle,
    val content: PostContent,
    val isPublic: Boolean,
    val images: List<PostImage>
) {
    // 행동: 상태 전이 (copy 패턴)
    fun publishBy(authorId: Long): Post {
        validateAuthor(authorId)
        return copy(isPublic = true)
    }

    fun unpublishBy(authorId: Long): Post {
        validateAuthor(authorId)
        return copy(isPublic = false)
    }

    // PostImage는 별도 추가/제거 (여기서는 ID만 관리)
    fun addImage(image: PostImage): Post {
        val newImages = this.images.toMutableList()
        newImages.add(image)
        return copy(images = newImages)
    }

    private fun validateAuthor(authorId: Long) {
        if (this.authorId != authorId) {
            throw DomainException("UNAUTHORIZED", "작성자만 변경 가능")
        }
    }

    companion object {
        // v2.1: 영속 전엔 id가 null인 상태로 생성
        fun create(authorId: Long, bookId: Long, title: String, content: String): Post {
            return Post(
                id = null,
                authorId = authorId,
                bookId = bookId,
                title = PostTitle.of(title),
                content = PostContent.of(content),
                isPublic = false,
                images = emptyList()
            )
        }

        fun restore(
            id: Long,
            authorId: Long,
            bookId: Long,
            title: String,
            content: String,
            isPublic: Boolean,
            images: List<PostImage>
        ): Post {
            return Post(
                id = PostId.of(id),
                authorId = authorId,
                bookId = bookId,
                title = PostTitle.of(title),
                content = PostContent.of(content),
                isPublic = isPublic,
                images = images
            )
        }
    }
}
```

**루트 규칙**:
1. publish/unpublish 상태 전이
2. 삭제/숨김
3. 작성자 권한
4. 신고/블라인드 (future)

**관계**:
```
Post(AR)
  ├─→ Member: authorId (Many-to-One)
  ├─→ Book: bookId (Many-to-One, 직접 참조)
  └─→ PostImage: 1:N (하위 Entity)

X Comment: 독립 AR로 분리 (v2)
```

**Note**: Post가 "내 reading 경험 기반"을 표시해야 할 때만 `readingId`를 선택적으로 추가 (없어도 동작)

---

### E. Comment (AR) = Post의 댓글/대댓글(1단)

**성격**: 대댓글 1단이면 독립 AR로 두는 게 가장 단순하고 성능/동시성에 유리

```kotlin
// 독립 AR (v2.1)
data class Comment(
    val id: CommentId?,  // v2.1: 영속 전엔 null
    val postId: Long,
    val postAuthorId: Long,  // 알림용 중복 보유
    val authorId: Long,
    val content: CommentContent,
    val parentCommentId: CommentId?,
    val depth: Int,
    val isDeleted: Boolean,
    val createdAt: LocalDateTime
) {
    // 행동 (copy 패턴)
    fun editBy(requesterId: Long, newContent: CommentContent): Comment {
        validateAuthor(requesterId)
        validateNotDeleted()
        return copy(content = newContent)
    }

    fun deleteBy(requesterId: Long): Comment {
        validateAuthor(requesterId)
        validateNotDeleted()
        return copy(
            isDeleted = true,
            content = CommentContent.of("삭제된 댓글입니다")
        )
    }

    private fun validateAuthor(requesterId: Long) {
        if (authorId != requesterId) {
            throw DomainException("UNAUTHORIZED", "작성자만 수정 가능")
        }
    }

    private fun validateNotDeleted() {
        if (isDeleted) {
            throw DomainException("ALREADY_DELETED", "이미 삭제됨")
        }
    }

    companion object {
        // v2.1: Clock 주입으로 시간 일관성 확보
        fun create(postId: Long, postAuthorId: Long, authorId: Long, content: CommentContent, clock: Clock): Comment {
            return Comment(
                id = null,
                postId = postId,
                postAuthorId = postAuthorId,
                authorId = authorId,
                content = content,
                parentCommentId = null,
                depth = 0,
                isDeleted = false,
                createdAt = LocalDateTime.now(clock).withSecond(0).withNano(0)
            )
        }

        fun createReply(parent: Comment, authorId: Long, content: CommentContent, clock: Clock): Comment {
            if (parent.depth >= 1) {
                throw DomainException("MAX_DEPTH", "대댓글은 1단까지만")
            }
            if (parent.isDeleted) {
                throw DomainException("PARENT_DELETED", "삭제된 댓글에 답글 불가")
            }

            return Comment(
                id = null,
                postId = parent.postId,
                postAuthorId = parent.postAuthorId,
                authorId = authorId,
                content = content,
                parentCommentId = parent.id,
                depth = parent.depth + 1,
                isDeleted = false,
                createdAt = LocalDateTime.now(clock).withSecond(0).withNano(0)
            )
        }

        fun restore(
            id: Long,
            postId: Long,
            postAuthorId: Long,
            authorId: Long,
            content: String,
            parentCommentId: Long?,
            depth: Int,
            isDeleted: Boolean,
            createdAt: LocalDateTime
        ): Comment {
            return Comment(
                id = CommentId.of(id),
                postId = postId,
                postAuthorId = postAuthorId,
                authorId = authorId,
                content = CommentContent.of(content),
                parentCommentId = if (parentCommentId != null) CommentId.of(parentCommentId) else null,
                depth = depth,
                isDeleted = isDeleted,
                createdAt = createdAt
            )
        }
    }
}
```

**관계**:
```
Comment(AR)
  ├─→ Post: postId (ID 참조)
  ├─→ ParentComment: parentCommentId (nullable, ID 참조)
  └─→ depth: 0 or 1
```

**왜 독립 AR인가?**
- 대댓글 1단이면 계층 구조가 단순
- Post에서 컬렉션으로 관리할 필요 없음
- 성능/동시성 이점 (별도 조회)

---

## 3. 관계 요약 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                    Book (Reference)                        │
│    JPA Entity이나 도메인 행위 없음 / 참조 전용              │
└───────────┬─────────────────────────────────┬───────────────┘
            │ bookId                         │ bookId
            │                                 │
    ┌───────▼────────┐              ┌────────▼────────┐
    │   Reading(AR)   │              │    Post(AR)      │
    │  (User×Book)    │              │   (Book 기반)     │
    │                 │              │                  │
    │ ┌─────────────┐ │              │ ┌─────────────┐ │
    │ │ReadingCard  │ │              │ │ PostImage   │ │
    │ │ (1:N Entity)│ │              │ │ (1:N Entity)│ │
    │ └─────────────┘ │              │ └─────────────┘ │
    └─────────────────┘              └──────────────────┘
                                            │
                                            │ postId
                                            │
                                    ┌───────▼────────┐
                                    │  Comment(AR)   │
                                    │  (대댓글 1단)   │
                                    └────────────────┘
```

---

## 4. Value Object 및 ID 정책 (v2.1)

### 4.1 ID 생성 정책: 영속 후 결정 (v2.1 수정)

**문제**: `System.currentTimeMillis()`는 동시성 충돌 가능

**해결**: JPA `@GeneratedValue` 위임, 도메인은 `restore` 전용

**v2.1 치명적 수정**: `placeholder()` 제거, 영속 전엔 `id`를 `Nullable`로 모델링

```kotlin
// CommentId.kt (Kotlin v2.1)
@JvmInline
value class CommentId(val value: Long) {
    init {
        require(value > 0) { "Invalid CommentId: $value" }
    }

    companion object {
        // v2.1: placeholder() 제거 - 0L은 require(value > 0) 위배
        // 영속 후: 실제 ID로 복원만 제공
        fun of(value: Long): CommentId {
            return CommentId(value)
        }
    }
}

// ReadingId.kt
@JvmInline
value class ReadingId(val value: Long) {
    init {
        require(value > 0) { "Invalid ReadingId: $value" }
    }

    companion object {
        fun of(value: Long): ReadingId = ReadingId(value)
    }
}
```

**v2.1 도메인 모델 ID 필드**: Nullable로 선언
```kotlin
data class Comment(
    val id: CommentId?,  // 영속 전엔 null, 영속 후엔 non-null
    val postId: Long,
    ...
)
```

**Application Service 사용 (v2.1)**:
```kotlin
@Service
class CommentCommandService(
    private val commentRepository: CommentRepository,
    private val clock: Clock
) {
    fun postComment(command: PostCommentCommand): CommentId {
        // 1. ID 없이 도메인 생성 (id = null)
        val comment = Comment.create(
            postId = command.postId,
            postAuthorId = command.postAuthorId,
            authorId = command.authorId,
            content = CommentContent.of(command.content),
            clock = clock  // v2.1: 시간 정책 준수
        )

        // 2. 저장 (JPA가 ID 생성)
        val savedEntity = commentRepository.save(comment)

        // 3. 영속된 ID로 복원
        return CommentId.of(savedEntity.id!!)  // 영속 후엔 항상 non-null
    }
}
```

---

### 4.2 Value Object (Kotlin)

```kotlin
// CommentContent.kt
@JvmInline
value class CommentContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Content is blank" }
        require(value.length <= 2000) { "Content too long: ${value.length}" }
    }

    companion object {
        fun of(value: String): CommentContent {
            return CommentContent(value.trim())
        }
    }
}

// PostTitle.kt
@JvmInline
value class PostTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "Title is blank" }
        require(value.length <= 50) { "Title too long: ${value.length}" }
    }

    companion object {
        fun of(value: String): PostTitle {
            return PostTitle(value.trim())
        }
    }
}

// PostContent.kt
@JvmInline
value class PostContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Content is blank" }
        require(value.length <= 10000) { "Content too long" }
    }

    companion object {
        fun of(value: String): PostContent {
            return PostContent(value.trim())
        }
    }
}

// CardContent.kt
@JvmInline
value class CardContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Content is blank" }
        require(value.length <= 2000) { "Content too long" }
    }

    companion object {
        fun of(value: String): CardContent {
            return CardContent(value.trim())
        }
    }
}
```

---

## 5. POJO 전략 (최소 비용 / 높은 ROI)

### 원칙 1: Command(쓰기)만 POJO로 "강제"

**적용 대상**: Reading, ReadingCard, Post, Comment의 상태 변경

**Query(커뮤니티 리스트/검색)**는 엔티티 Projection/DTO로 직접 조회

```kotlin
// Command: 도메인 POJO 사용 (copy 패턴)
@Service
@Transactional
class ReadingCommandService {
    fun startReading(command: StartReadingCommand): ReadingId {
        // 1. 조회 (도메인으로)
        val reading = readingRepository.findById(command.readingId)

        // 2. 상태 변경 (copy로 새 인스턴스)
        val updatedReading = reading.start(clock)

        // 3. 저장
        readingRepository.save(updatedReading)

        return reading.id
    }
}

// Query: Projection 사용 (빠름)
@Service
@Transactional(readOnly = true)
class ReadingQueryService {
    fun getMyReadings(memberId: Long): List<ReadingSummaryResponse> {
        // 엔티티에서 바로 Projection (도메인 경유 X)
        return readingRepository.findProjectionsByMemberId(memberId)
    }
}
```

---

### 원칙 2: 1:N은 Reading 내부만

**Reading ─ 1:N ─ ReadingCard**는 POJO 컬렉션으로 관리 가능

**Book ─ Reading, Post ─ Comment**는 컬렉션으로 들지 말고 ID 참조 + 전용 조회

```kotlin
// ✅ Good: Reading 내부 1:N
class Reading(
    val cards: List<ReadingCard>  // 불변 컬렉션
) {
    fun addCard(card: ReadingCard): Reading {
        val newCards = this.cards.toMutableList()
        newCards.add(card)
        return copy(cards = newCards)
    }

    fun getCards(): List<ReadingCard> = this.cards
}

// ❌ Bad: Post가 Comment 컬렉션을 가짐 (v2에서 제거됨)
class Post(
    // comments 컬렉션 제거
)

// ✅ Good: 별도 조회
interface CommentRepository {
    fun findByPostId(postId: Long): List<Comment>
}
```

---

### 원칙 3: 매핑은 Separate + Mapper (Strategy B) 기본

엔티티 setter로 규칙을 우회하지 않도록:

- **Entity**: Persistence 전용
- **POJO**: 규칙/행동 보유 (불변, copy 패턴)
- **Mapper**: Entity ↔ POJO 변환

```kotlin
// Entity: Persistence 전용
@Entity
@Table(name = "reading")
class ReadingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "is_public")
    var isPublic: Boolean = true

    @OneToMany(mappedBy = "reading")
    var cards: MutableList<CardEntity> = mutableListOf()

    // Package-private setter (Mapper만 사용)
    internal fun setIsPublicForMapper(isPublic: Boolean) {
        this.isPublic = isPublic
    }

    internal fun setCardsForMapper(cards: MutableList<CardEntity>) {
        this.cards = cards
    }
}

// POJO: 행동 보유 (불변, copy)
class Reading(
    val id: ReadingId,
    val isPublic: Boolean,
    val cards: List<ReadingCard>
) {
    // copy로 상태 변경
    fun makePublic(): Reading {
        return copy(
            isPublic = true,
            cards = this.cards.map { it.makePublic() }
        )
    }
}

// Mapper: 변환 담당
class ReadingMapper {
    fun toDomain(entity: ReadingEntity): Reading {
        return Reading.restore(
            id = entity.id!!,
            memberId = entity.member.id!!,
            bookId = entity.book.id!!,
            status = ReadingStatus.valueOf(entity.status.name),
            isPublic = entity.isPublic,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            score = entity.score,
            cards = entity.cards.map { toReadingCard(it) }
        )
    }

    fun toEntity(domain: Reading): ReadingEntity {
        val entity = ReadingEntity()
        entity.id = domain.id.value
        entity.setIsPublicForMapper(domain.isPublic)
        entity.setCardsForMapper(
            domain.cards.map { toCardEntity(it) }.toMutableList()
        )
        return entity
    }
}
```

---

### 원칙 4: 커뮤니티(Public)는 Read Model로 해결

**visibility = PUBLIC 필터**

**author, book, counts는 Projection으로 한 번에 조회**

성능 문제 생기면 그때 집계/캐시/이벤트 추가 (초기엔 과투자 금지)

```kotlin
// Query: Projection으로 커뮤니티 조회
@Query("""
    SELECT new org.veri.dto.CommunityReadingResponse(
        r.id, r.memberId, r.bookId, b.title, b.author, b.image,
        r.status, r.score, r.createdAt,
        (SELECT COUNT(c) FROM ReadingCardEntity c WHERE c.reading.id = r.id AND c.isPublic = true)
    )
    FROM ReadingEntity r
    JOIN BookEntity b ON r.book.id = b.id
    WHERE r.isPublic = true
    ORDER BY r.createdAt DESC
""")
fun findPublicReadings(pageable: Pageable): Page<CommunityReadingResponse>
```

---

## 6. 추천 시작 순서 (실제 구조 기준)

### Phase 1: Comment POJO (1주)

**이유**: 가장 단순한 독립 AR

```kotlin
class Comment(
    val id: CommentId,
    val postId: Long,
    val postAuthorId: Long,
    val authorId: Long,
    val content: CommentContent,
    val parentCommentId: CommentId?,
    val depth: Int,
    val isDeleted: Boolean
) {
    fun editBy(requesterId: Long, newContent: CommentContent): Comment
    fun deleteBy(requesterId: Long): Comment

    companion object {
        fun create(...)  // ID는 placeholder
        fun restore(...)  // 영속 후 복원
        fun createReply(parent: Comment, ...)
    }
}
```

---

### Phase 2: Reading POJO + ReadingCard 내부 규칙 (1주)

**핵심**: Reading이 중심 루트

```kotlin
class Reading(
    val id: ReadingId,
    val memberId: Long,
    val bookId: Long,
    val status: ReadingStatus,
    val isPublic: Boolean,
    val cards: List<ReadingCard>
) {
    fun start(clock: Clock): Reading
    fun finish(clock: Clock): Reading
    fun makePublic(): Reading
    fun makePrivate(): Reading

    fun addCard(card: ReadingCard): Reading
    fun removeCard(cardId: CardId): Reading
    fun reorderCards(cardIds: List<CardId>): Reading
}

class ReadingCard(
    val id: CardId,
    val content: CardContent,
    val imageUrl: String,
    val isPublic: Boolean
) {
    fun updateContent(newContent: CardContent, newImageUrl: String): ReadingCard
    fun assertReadableBy(memberId: Long)
}
```

**규칙**:
- 카드 생성/정렬/제한
- 공개/비공게 전이 (v1: Reading 공개가 카드 공개의 전제)
- Reading 상태에 따른 카드 visibility 제어

---

### Phase 3: Post POJO (1주)

```kotlin
class Post(
    val id: PostId,
    val authorId: Long,
    val bookId: Long,
    val title: PostTitle,
    val content: PostContent,
    val isPublic: Boolean,
    val images: List<PostImage>
) {
    fun publishBy(authorId: Long): Post
    fun unpublishBy(authorId: Long): Post
    fun addImage(image: PostImage): Post

    companion object {
        fun createPlaceholder(...)  // ID는 placeholder
        fun restore(...)  // 영속 후 복원
    }
}
```

---

### Phase 4: 커뮤니티 Query Projection 정리 (3일)

**Book, Reading, Post, Card의 Public 조회를 Projection으로 최적화**

---

## 7. 추가로 정의해야 할 관계

### 7.1 Member 도메인 (v2.1)

**정의**: Member는 **현재는 규칙 최소** (단순 CRUD)

향후 권한/차단/탈퇴 정책이 추가되면 AR 성격이 생길 수 있음

```kotlin
// v2.1: data class + nullable id
data class Member(
    val id: MemberId?,  // 영속 전엔 null
    val email: Email,
    val nickname: Nickname,
    val profileImageUrl: String?
) {
    fun updateProfile(newNickname: Nickname, newImageUrl: String?): Member {
        return copy(
            nickname = newNickname,
            profileImageUrl = newImageUrl
        )
    }

    fun changeNickname(newNickname: Nickname): Member {
        return copy(nickname = newNickname)
    }

    companion object {
        fun create(email: String, nickname: String): Member {
            return Member(
                id = null,  // v2.1: 영속 전엔 null
                email = Email.of(email),
                nickname = Nickname.of(nickname),
                profileImageUrl = null
            )
        }

        fun restore(
            id: Long,
            email: String,
            nickname: String,
            profileImageUrl: String?
        ): Member {
            return Member(
                id = MemberId.of(id),
                email = Email.of(email),
                nickname = Nickname.of(nickname),
                profileImageUrl = profileImageUrl
            )
        }
    }
}
```

**관계**:
```
Member
  ├─→ Reading: 1:N (ID 참조)
  ├─→ Post: 1:N (작성자)
  ├─→ Comment: 1:N (작성자)
  └─→ ReadingCard: 1:N (작성자, ID 참조)
```

---

### 7.2 PostImage 하위 Entity (v2.1)

```kotlin
// v2.1: data class
data class PostImage(
    val id: Long?,  // 영속 전엔 null
    val postId: Long,
    val imageUrl: String,
    val displayOrder: Long
) {
    companion object {
        fun create(postId: Long, imageUrl: String, displayOrder: Long): PostImage {
            return PostImage(
                id = null,  // v2.1: 영속 전엔 null
                postId = postId,
                imageUrl = imageUrl,
                displayOrder = displayOrder
            )
        }

        fun restore(
            id: Long,
            postId: Long,
            imageUrl: String,
            displayOrder: Long
        ): PostImage {
            return PostImage(
                id = id,
                postId = postId,
                imageUrl = imageUrl,
                displayOrder = displayOrder
            )
        }
    }
}
```

**관계**:
```
Post(AR)
  └─→ PostImage(Entity): 1:N
```

---

### 7.3 좋아요 (PostLike, ReadingLike) 중간 테이블

**Post ↔ Member 좋아요**:
```kotlin
@Entity
@Table(name = "post_likes")
class PostLikeEntity(
    @EmbeddedId
    val id: PostLikeId,

    @ManyToOne
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    val post: PostEntity?,

    @ManyToOne
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    val member: MemberEntity?
)

// Post는 좋아요 수만 보유
class Post(
    val id: PostId,
    val likeCount: Long
) {
    fun incrementLikeCount(): Post {
        return copy(likeCount = likeCount + 1)
    }

    fun decrementLikeCount(): Post {
        return copy(likeCount = if (likeCount > 0) likeCount - 1 else 0)
    }
}
```

---

### 7.4 북마크/스크랩 (v2.1)

**사용자가 책을 저장**하는 기능:

```kotlin
// v2.1: data class + nullable id
data class Bookmark(
    val id: BookmarkId?,  // 영속 전엔 null
    val memberId: Long,
    val bookId: Long
) {
    // 단순 ID 매핑, 추가 규칙 없음
    companion object {
        fun create(memberId: Long, bookId: Long): Bookmark {
            return Bookmark(
                id = null,  // v2.1: 영속 전엔 null
                memberId = memberId,
                bookId = bookId
            )
        }

        fun restore(id: Long, memberId: Long, bookId: Long): Bookmark {
            return Bookmark(
                id = BookmarkId.of(id),
                memberId = memberId,
                bookId = bookId
            )
        }
    }
}
```

---

## 8. 최종 AR 목록 (Veri-BE)

| AR | 하위 Entity | 성격 | 비고 |
|----|------------|------|------|
| **Reading** | ReadingCard (1:N) | User×Book 매핑 | 카드 파생 |
| **Post** | PostImage (1:N) | Book 기반 글쓰기 | Reading과 독립 |
| **Comment** | - | Post 댓글/대댓글 | 독립 (대댓글 1단) |
| **Member** | - | 단순 CRUD | v1: 규칙 최소 |

**Non-AR**:
- **Book**: JPA Entity이나 도메인 행위 없음 / Reference 모델
- **PostLike**, **ReadingLike**: 중간 테이블 (ID 매핑만)

---

## 9. 불변 모델과 Copy 패턴 (Kotlin)

### 9.1 왜 Copy 패턴인가?

Kotlin의 `val`은 재할당 불가능하므로, 상태 변경 시 새 인스턴스를 생성

```kotlin
// Before (불변)
class Comment(
    val content: CommentContent
) {
    fun editBy(newContent: CommentContent) {
        this.content = newContent  // ❌ 컴파일 에러: val는 재할당 불가
    }
}

// After (copy 패턴)
class Comment(
    val content: CommentContent
) {
    fun editBy(newContent: CommentContent): Comment {
        return copy(content = newContent)  // ✅ 새 인스턴스 반환
    }
}
```

### 9.2 Copy 패턴의 장점

1. **불변성**: 스레드 안전
2. **예측 가능성**: 상태가 변경되면 참조가 끊어짐
3. **테스트 용이**: 이전 상태와 비교

---

## 10. 정책 명시 (v2.1)

### 10.1 공개 정책 (v2.1 명확화)

**"Reading이 비공개면 하위 카드도 비공개 강제"**

**v2.1 핵심 수정**: **카드 공개/비공개 전이는 Reading(AR)에서만 수행, ReadingCard는 순수 상태 객체**

```kotlin
// v2.1: Reading(AR)이 카드 공개를 통제
data class Reading(
    val isPublic: Boolean,
    val cards: List<ReadingCard>
) {
    fun makePublic(): Reading {
        return copy(
            isPublic = true,
            cards = this.cards.map { it.copy(isPublic = true) }  // Reading이 호출
        )
    }

    fun makePrivate(): Reading {
        return copy(
            isPublic = false,
            cards = this.cards.map { it.copy(isPublic = false) }  // Reading이 호출
        )
    }
}

// v2.1: ReadingCard는 순수 상태 객체 (makePublic/makePrivate 제거)
data class ReadingCard(
    val isPublic: Boolean
) {
    // 단순 상태 보유, 공개 전이 로직 제거
    // 호출은 Reading에서만 수행됨
}
```

**정책 요약**:
- **Reading**: AR로서 공개/비공개 전이의 책임자
- **ReadingCard**: 순수 상태 객체, Reading에 의해 상태가 변경됨
- **일관성 보장**: Reading이 전체 카드의 상태를 원자적으로 관리
```

**향후 확장 가능성** (v2+):
- "Reading은 비공개인데 특정 카드만 공개" 같은 요구가 있을 경우
- 그때 카드 단위 공개 정책을 도입

---

### 10.2 읽기 권한 정책 (v2.1)

**"isPublic || owner만"**

```kotlin
data class ReadingCard(
    val isPublic: Boolean,
    val memberId: Long
) {
    fun assertReadableBy(requesterId: Long) {
        if (isPublic || this.memberId == requesterId) return
        throw DomainException("NOT_READABLE", "비공개 카드")
    }
}
```

**향후 확장**:
- 차단/블라인드 유저에 대한 추가 정책은 **Application Service에서 처리**
- 도메인은 기본 정책만, 특수 케이스는 Application 계층에서 구현

---

### 10.3 시간 정책 (v2.1 추가)

**"도메인 생성 시각은 Application에서 Clock 주입으로 결정"**

**문제**: `LocalDateTime.now()`를 직접 호출하면 테스트에서 시간을 제어할 수 없음

**해결**: 모든 도메인 생성 메서드에 `Clock`을 주입

```kotlin
// v2.1: Clock 주입으로 테스트 가능성 확보
data class Comment(
    val createdAt: LocalDateTime
) {
    companion object {
        fun create(
            postId: Long,
            postAuthorId: Long,
            authorId: Long,
            content: CommentContent,
            clock: Clock  // v2.1: 시간 정책 준수
        ): Comment {
            return Comment(
                id = null,
                postId = postId,
                postAuthorId = postAuthorId,
                authorId = authorId,
                content = content,
                parentCommentId = null,
                depth = 0,
                isDeleted = false,
                createdAt = LocalDateTime.now(clock).withSecond(0).withNano(0)
            )
        }
    }
}

// Application Service에서 Clock 제공
@Service
class CommentCommandService(
    private val commentRepository: CommentRepository,
    private val clock: Clock  // Spring이 자동으로 제공
) {
    fun postComment(command: PostCommentCommand): CommentId {
        val comment = Comment.create(
            postId = command.postId,
            postAuthorId = command.postAuthorId,
            authorId = command.authorId,
            content = CommentContent.of(command.content),
            clock = clock  // 주입
        )
        // ...
    }
}
```

**정책 요약**:
- **도메인**: `Clock`을 외부에서 받아서 `LocalDateTime.now(clock)` 사용
- **Application**: Spring의 `Clock` 빈을 주입
- **테스트**: `Clock.fixed()`로 시간 고정 테스트 가능

---

### 10.4 JPA 저장 가이드 (v2.1 추가)

**"aggregate 단위 저장 시 mapper 설계 원칙"**

**문제**: Reading에 카드가 1:N으로 붙고, 매번 copy로 새 aggregate를 만들면 JPA 엔티티 동기화가 번거로움

**해결**: Mapper에서 Entity ↔ Domain 변환을 원자적으로 수행

```kotlin
// Mapper: 변환 담당 (v2.1)
class ReadingMapper {
    // Domain → Entity (저장용)
    fun toEntity(domain: Reading): ReadingEntity {
        val entity = ReadingEntity()
        entity.id = domain.id?.value  // nullable 처리
        entity.memberId = domain.memberId
        entity.bookId = domain.bookId
        entity.status = domain.status
        entity.isPublic = domain.isPublic
        entity.startedAt = domain.startedAt
        entity.endedAt = domain.endedAt
        entity.score = domain.score

        // v2.1: aggregate 단위로 하위 엔티티도 매핑
        entity.cards = domain.cards.map { card ->
            val cardEntity = CardEntity()
            cardEntity.id = card.id?.value
            cardEntity.readingId = domain.id?.value
            cardEntity.memberId = card.memberId
            cardEntity.content = card.content.value
            cardEntity.imageUrl = card.imageUrl
            cardEntity.isPublic = card.isPublic
            cardEntity
        }.toMutableList()

        return entity
    }

    // Entity → Domain (조회용)
    fun toDomain(entity: ReadingEntity): Reading {
        return Reading.restore(
            id = entity.id!!,
            memberId = entity.memberId,
            bookId = entity.bookId,
            status = ReadingStatus.valueOf(entity.status.name),
            isPublic = entity.isPublic,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            score = entity.score,
            cards = entity.cards.map { toReadingCardDomain(it) }
        )
    }

    private fun toReadingCardDomain(entity: CardEntity): ReadingCard {
        return ReadingCard.restore(
            id = entity.id!!,
            readingId = ReadingId.of(entity.readingId!!),
            memberId = entity.memberId,
            content = entity.content,
            imageUrl = entity.imageUrl,
            isPublic = entity.isPublic
        )
    }
}

// Repository: JPA 처리
@Repository
class ReadingRepository(
    private val em: EntityManager,
    private val mapper: ReadingMapper
) {
    fun save(domain: Reading): Reading {
        val entity = mapper.toEntity(domain)

        // v2.1: merge로 aggregate 전체를 한 번에 저장
        val merged = em.merge(entity)
        em.flush()

        return mapper.toDomain(merged)
    }

    fun findById(id: Long): Reading? {
        val entity = em.find(ReadingEntity::class.java, id) ?: return null
        return mapper.toDomain(entity)
    }
}
```

**구현 원칙**:
1. **aggregate 단위 저장**: `merge()`로 전체 aggregate를 한 번에 저장
2. **Mapper 책임**: Entity ↔ Domain 변환을 Mapper가 담당
3. **영속성 전이**: `@OneToMany(cascade = CascadeType.ALL)`로 하위 엔티티 자동 저장
4. **성능 최적화**: 변경된 카드만 반영하는 로직은 Mapper 레벨에서 구현 (필요시)

--

## 11. 요약 (v2.1)

**핵심 구조**:
1. **Reading(AR)**: User-Book 매핑 + Card 파생의 중심
2. **Post(AR)**: Book 직접 참조 (Reading과 독립)
3. **Comment(AR)**: 독립 (대댓글 1단, Post에서 분리)
4. **Book**: Reference 모델 (JPA Entity이나 도메인 행위 없음)

**시작 순서**:
1. Comment (단순한 독립 AR)
2. Reading + ReadingCard (핵심 도메인)
3. Post (Book 참조)
4. 커뮤니티 Query 최적화

**v2.1 기술적 결정**:
- **ID 정책**: 영속 전엔 `id`를 `Nullable`로 모델링, `placeholder()` 제거
- **불변성**: 모든 도메인 POJO는 `data class`, 상태 변경은 `copy` 패턴
- **시간 정책**: `Clock` 주입으로 테스트 가능성 확보
- **공개 정책**: Reading(AR)이 카드 공개를 통제, ReadingCard는 순수 상태 객체
- **POJO 언어**: Kotlin으로 작성 (Value Class 최적)
- **JPA Entity**: Java 유지 (v1)

**v2.1 핵심 수정사항**:
1. ✅ ID 생성 충돌 해결: `require(value > 0)` 제약 유지, `placeholder()` 제거
2. ✅ data class 선언: `copy()` 메서드 사용 가능
3. ✅ 공개 정책 명확화: Reading이 ReadingCard 상태 변경을 통제
4. ✅ 시간 정책 추가: `Clock` 주입으로 일관성 확보
5. ✅ JPA 저장 가이드: aggregate 단위 저장 원칙 명시
