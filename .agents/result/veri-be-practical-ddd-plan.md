# Veri-BE Real-World DDD: ROI-Focused Approach

**Status**: Practical Implementation Guide
**Version**: 2.0 (Simplified from Hexagonal)
**Date**: 2025-12-31
**Focus**: **상태 변경의 진입점을 도메인 POJO로 이동**

---

## Executive Summary

### 핵심 원칙

**"POJO를 만드는 것"이 목적이 아닙니다. "상태 변경의 진입점을 POJO로 옮기는 것"이 목표입니다.**

### 현실적인 접근

헥사고날 아키텍처(Port/Adapter)는 **옵션**입니다. 핵심은:
1. **비즈니스 규칙의 거처** 만들기
2. **JPA 라이프사이클 분리**
3. **CQS와의 결합**

---

## 1. 도메인 POJO 도입의 ROI 분석

### 1.1 ROI가 큰 경우 (적용 추천)

#### ✅ 비즈니스 규칙이 복잡할 때

**현재**: 서비스 메서드에 분산
```java
// Before: 규칙이 서비스에 흩어짐
@Service
class CommentService {
    fun editComment(commentId: Long, memberId: Long, newContent: String) {
        val comment = commentRepository.findById(commentId)

        // 규칙 1: 권한 체크
        if (comment.authorId != memberId) {
            throw UnauthorizedException()
        }

        // 규칙 2: 삭제된 댓글 체크
        if (comment.isDeleted) {
            throw AlreadyDeletedException()
        }

        // 규칙 3: 내용 길이 체크
        if (newContent.isBlank() || newContent.length > 2000) {
            throw InvalidContentException()
        }

        // 상태 변경
        comment.content = newContent  // Setter!
        comment.updatedAt = now()
    }
}
```

**After**: 도메인이 규칙의 거처
```java
// After: 도메인이 모든 규칙 보유
class Comment {
    fun editBy(requesterId: Long, newContent: CommentContent) {
        validateAuthor(requesterId)     // 규칙 1
        validateNotDeleted()            // 규칙 2
        this.content = newContent       // 규칙 3은 VO가 검증
        this.updatedAt = LocalDateTime.now()
    }

    private fun validateAuthor(requesterId: Long) {
        if (authorId != requesterId) {
            throw DomainException("UNAUTHORIZED", "Not the author")
        }
    }

    private fun validateNotDeleted() {
        if (isDeleted) {
            throw DomainException("ALREADY_DELETED", "Comment is deleted")
        }
    }
}

// Service는 도메인을 호출만
@Service
class CommentService {
    fun editComment(commentId: Long, memberId: Long, newContent: String) {
        val comment = commentRepository.findById(commentId)
        comment.editBy(memberId, CommentContent.of(newContent))  // 모든 규칙 실행
        commentRepository.save(comment)
    }
}
```

**효과**:
- 규칙 중복 제거 (다른 서비스에서도 재사용)
- 테스트가 단순해짐 (도메인 로직만 테스트)
- 변경 위치가 명확해짐 (규칙 수정 → 도메인만 변경)

#### ✅ JPA 라이프사이클 문제가 발생할 때

**문제 상황**:
```java
// Entity에서 equals/hashCode 문제
@Entity
class Post {
    @Id
    var id: Long? = null

    @OneToMany(mappedBy = "post")
    var comments: MutableList<Comment> = mutableListOf()

    // 문제: id가 null인 상태에서 HashSet에 넣으면?
    // 문제: lazy loading 시 프록시 객체 equals 동작은?
}
```

**해결**: 도메인 POJO는 JPA에서 자유로움
```java
// Domain: JPA 무관
class Post(
    val id: PostId,
    val authorId: Long,
    val title: String,
    val comments: List<Comment> = emptyList()  // 불변
) {
    override fun equals(other: Any?) =
        other is Post && id == other.id

    override fun hashCode() = id.hashCode()
}

// JPA Entity는 저장용
@Entity
class PostEntity {
    @Id
    var id: Long? = null

    // JPA만 고려하면 됨
}

// Mapper가 변환 담당
fun PostEntity.toDomain(): Post {
    return Post(
        id = PostId.of(this.id!!),
        authorId = this.authorId,
        title = this.title,
        comments = this.comments.map { it.toDomain() }
    )
}
```

#### ✅ CQS (Command/Query)와 결합할 때

**Command**: 도메인 POJO 사용 (규칙 실행)
```java
// Command: 도메인 모델로 규칙 실행
val post = postRepository.findById(postId)
post.publish()  // 도메인의 비즈니스 규칙
postRepository.save(post)
```

**Query**: 엔티티 기반 Projection 사용 (성능 최적화)
```java
// Query: 엔티티 Projection으로 바로 조회
@Query("SELECT new com.veri.app.dto.PostSummary(p.id, p.title, p.author.nickname) FROM PostEntity p WHERE p.id = :id")
fun findSummaryById(id: Long): PostSummary
```

---

### 1.2 ROI가 낮은 경우 (도입 비권장)

#### ❌ POJO가 엔티티 필드 복사본에 불과할 때

**나쁜 예**:
```java
// 단순 필드 복사 (DTO화)
data class CommentDto(
    val id: Long,
    val content: String,
    val authorId: Long,
    val createdAt: LocalDateTime
)

// 이건 도메인 모델이 아니라 DTO입니다
// 행동이 없고, 불변이 아니라서 의미 없음
```

**해결**: 행동(behavior)을 포함해야 함
```java
// 도메인 모델은 행동을 가짐
class Comment(
    val id: CommentId,
    val content: CommentContent,
    val authorId: Long
) {
    fun editBy(requesterId: Long, newContent: CommentContent) { ... }  // 행동
    fun deleteBy(requesterId: Long) { ... }                           // 행동
}
```

#### ❌ 서비스가 여전히 엔티티를 직접 수정할 때

**나쁜 예**:
```java
@Service
class CommentService {
    fun editComment(id: Long, newContent: String) {
        // 엔티티를 직접 수정
        val entity = commentRepository.findById(id)
        entity.content = newContent  // Setter!
        entity.updatedAt = now()
    }
}

// 도메인 POJO는 만들었지만 사용하지 않음
```

**해결**: 상태 변경은 도메인 메서드만 통해야 함
```java
@Service
class CommentService {
    fun editComment(id: Long, newContent: String) {
        // 도메인 통해서만 변경
        val comment = commentRepository.findById(id)
        comment.editBy(requesterId, CommentContent.of(newContent))
        commentRepository.save(comment)
    }
}
```

#### ❌ 1:N 컬렉션 매핑 비용만 증가할 때

**나쁜 예**:
```java
// 전체 컬렉션을 도메인으로 변환 (비용만 큼)
class Post(
    val comments: List<Comment>  // 1000개 댓글 전체 변환?
) {
    // 댓글 하나 추가하는데 전체를 다시 변환?
}

// Mapper
fun toDomain(entity: PostEntity): Post {
    return Post(
        comments = entity.comments.map { it.toDomain() }  // 비용!
    )
}
```

**해결**: Aggregate 경계에서만 조작
```java
// Post는 댓글 컬렉션을 직접 수정 안 함
class Post(
    val id: PostId,
    private val commentIds: List<CommentId> = emptyList()  // ID만 보유
) {
    // 댓글 추가는 CommentRepository 통해
}

// 또는: 필요한 경우에만 조회
class CommentService {
    fun addComment(postId: Long, content: String) {
        val post = postRepository.findById(postId)  // Post만 조회
        val comment = Comment.create(postId, content)
        commentRepository.save(comment)              // Comment 별도 저장
    }
}
```

---

## 2. 현실적인 최소 규칙 3개

이것만 지키면 ROI가 보장됩니다.

### 규칙 1: 쓰기(Command) 경로에서만 POJO 사용

**원칙**:
- **Command**: 도메인 POJO 사용 (규칙 실행 필요)
- **Query**: 엔티티 Projection 사용 (성능 중시)

**적용 예시**:
```java
// ✅ Command: 도메인 POJO
@Service
@Transactional
class CommentCommandService {
    fun editComment(command: EditCommentCommand) {
        val comment = commentRepository.findById(command.commentId)
        comment.editBy(command.memberId, CommentContent.of(command.content))
        commentRepository.save(comment)
    }
}

// ✅ Query: 엔티티 Projection (성능)
@Service
@Transactional(readOnly = true)
class CommentQueryService {
    fun getComment(id: Long): CommentResponse {
        // 엔티티에서 바로 변환 (도메인 경유 X)
        return commentRepository.findProjectionById(id)
    }
}

// Repository
interface CommentRepository : JpaRepository<CommentEntity, Long> {
    // Query용 Projection (빠름)
    @Query("SELECT new com.veri.dto.CommentResponse(c.id, c.content, a.nickname) FROM CommentEntity c JOIN AuthorEntity a ON c.authorId = a.id WHERE c.id = :id")
    fun findProjectionById(id: Long): CommentResponse
}
```

**장점**:
- Command는 규칙 보장 (도메인)
- Query는 성능 최적화 (Projection)

---

### 규칙 2: 엔티티 Setter 직접 변경 금지

**원칙**: 엔티티의 `public` setter를 최소화하고, 도메인 메서드를 통해서만 상태 변경.

**적용 예시**:

**Option A: Setter 제거**
```java
@Entity
class CommentEntity {
    @Id
    var id: Long? = null

    var content: String = ""
        private set  // 외부에서 설정 불가

    var updatedAt: LocalDateTime = LocalDateTime.now()
        private set

    // 패키지 전용 (Mapper만 사용)
    internal fun setContent(content: String) {
        this.content = content
    }

    internal fun setUpdatedAt(updatedAt: LocalDateTime) {
        this.updatedAt = updatedAt
    }
}

// Mapper는 같은 패키지에 배치
package org.veri.db.mapper

class CommentMapper {
    fun toEntity(domain: Comment): CommentEntity {
        val entity = CommentEntity()
        entity.setContent(domain.content.value)  // 패키지 접근
        entity.setUpdatedAt(domain.updatedAt)
        return entity
    }
}
```

**Option B: 패키지 protected setter (Kotlin)**
```kotlin
@Entity
class CommentEntity(
    var content: String = "",
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // Setter를 internal로 제한
    fun setContentForMapper(newContent: String) {
        this.content = newContent
    }
}
```

**강제 방법**: ArchUnit 테스트
```java
@ArchTest
val `setter should not be called from service` = noClasses()
    .that().resideInAPackage("..service..")
    .should().callMethod(Entity::class.java, "setContent", String::class.java)
    .because("상태 변경은 도메인 메서드를 통해서만")
```

---

### 규칙 3: 1:N은 Aggregate 경계에서만 조작

**원칙**: 컬렉션 전체를 도메인에 올리지 않고, 필요한 만큼만 조작.

**적용 전략**:

#### Strategy A: ID만 보유 (조회 시점에 로드)
```java
// Post는 댓글 ID만 보유
class Post(
    val id: PostId,
    private val _commentIds: MutableList<CommentId> = mutableListOf()
) {
    fun addComment(commentId: CommentId) {
        _commentIds.add(commentId)
    }

    fun getCommentIds(): List<CommentId> = _commentIds.toList()
}

// 댓글이 필요한 시점에 별도 조회
class PostService {
    fun getPostWithComments(postId: Long): PostWithCommentsDto {
        val post = postRepository.findById(postId)
        val comments = commentRepository.findByPostId(postId)  // 별도 조회

        return PostWithCommentsDto(post, comments)
    }
}
```

#### Strategy B: 루트 Aggregate만 관리
```java
// Post는 댓글 컬렉션을 갖지 않음
class Post(
    val id: PostId,
    val authorId: Long,
    val title: String
) {
    // 댓글 추가는 Comment 쪽에서 처리
}

// 댓글이 Post를 참조
class Comment(
    val id: CommentId,
    val postId: Long,  // Post ID 참조
    val content: CommentContent
) {
    companion object {
        fun create(postId: Long, content: CommentContent): Comment {
            return Comment(
                id = CommentId.generate(),
                postId = postId,
                content = content
            )
        }
    }
}
```

#### Strategy C: 필요한 경우에만 조회 (Read Model)
```java
// Command: Aggregate만 다룸
@Service
class CommentCommandService {
    fun addComment(postId: Long, content: String) {
        val post = postRepository.findById(postId)  // 댓글 없이 Post만
        val comment = Comment.create(postId, content)
        commentRepository.save(comment)
    }
}

// Query: Read Model 사용
@Service
class CommentQueryService {
    fun getPostWithComments(postId: Long): PostWithCommentsResponse {
        // 한 번의 쿼리로 조인 조회 (성능)
        return commentRepository.findPostWithComments(postId)
    }
}
```

---

## 3. 중간 테이블 (Associative Table) 처리

### 3.1 문제 상황

**예시**: Post와 Tag의 N:M 관계
```sql
CREATE TABLE posts_tags (
    post_id BIGINT,
    tag_id BIGINT,
    PRIMARY KEY (post_id, tag_id)
)
```

### 3.2 접근 전략

#### Strategy A: 중간 테이블을 별도 Entity로 관리

**JPA Entity**:
```java
@Entity
@Table(name = "posts_tags")
class PostTagEntity(
    @EmbeddedId
    val id: PostTagId,

    @ManyToOne
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    val post: PostEntity?,

    @ManyToOne
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    val tag: TagEntity?
)

@Embeddable
class PostTagId(
    var postId: Long = 0L,
    var tagId: Long = 0L
) : Serializable
```

**Repository**:
```java
interface PostTagRepository : JpaRepository<PostTagEntity, PostTagId> {
    fun findByPostId(postId: Long): List<PostTagEntity>
    fun findByTagId(tagId: Long): List<PostTagEntity>

    @Query("SELECT pt FROM PostTagEntity pt JOIN FETCH pt.post WHERE pt.tag.id = :tagId")
    fun findByTagIdWithPost(@Param("tagId") tagId: Long): List<PostTagEntity>
}
```

**도메인**:
```java
// Post는 Tag ID만 보유
class Post(
    val id: PostId,
    private val _tagIds: MutableSet<TagId> = mutableSetOf()
) {
    fun addTag(tagId: TagId) {
        _tagIds.add(tagId)
    }

    fun removeTag(tagId: TagId) {
        _tagIds.remove(tagId)
    }

    fun getTagIds(): Set<TagId> = _tagIds.toSet()
}

// Application Service
@Service
class PostService {
    fun addTag(postId: Long, tagId: Long) {
        val post = postRepository.findById(postId)

        // 도메인 상태 변경
        post.addTag(TagId.of(tagId))

        // 중간 테이블 엔티티 생성/저장
        val postTag = PostTagEntity(
            id = PostTagId(postId, tagId),
            post = null,  // FetchType.LAZY라 필요 없음
            tag = null
        )
        postTagRepository.save(postTag)

        // Post도 저장
        postRepository.save(post)
    }
}
```

#### Strategy B: N:M을 1:N + 1:N으로 풀어서 도메인이 관리

**권장**: 도메인에서 직접 관리하고 싶을 때

**JPA Entity**:
```java
@Entity
class PostEntity(
    @Id
    val id: Long? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "post_id")
    val tags: MutableSet<PostTagEntity> = mutableSetOf()
)

@Entity
class PostTagEntity(
    @Id
    @GeneratedValue
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "tag_id")
    val tag: TagEntity
)
```

**도메인**:
```java
// Post는 Tag 목록을 직접 관리
class Post(
    val id: PostId,
    private val _tags: MutableSet<Tag> = mutableSetOf()
) {
    fun addTag(tag: Tag) {
        if (_tags.size >= MAX_TAG_COUNT) {
            throw DomainException("MAX_TAGS", "최대 10개까지")
        }
        _tags.add(tag)
    }

    fun removeTag(tagId: TagId) {
        _tags.removeIf { it.id == tagId }
    }

    fun getTags(): Set<Tag> = _tags.toSet()
}

// Mapper
fun PostEntity.toDomain(): Post {
    return Post(
        id = PostId.of(this.id!!),
        _tags = this.tags
            .map { it.tag.toDomain() }
            .toMutableSet()
    )
}

fun Post.toEntity(): PostEntity {
    val entity = PostEntity(id = this.id.value)
    entity.tags = this._tags
        .map { tag -> PostTagEntity(tag = tag.toEntity()) }
        .toMutableSet()
    return entity
}
```

**장점**:
- 도메인에서 직접 컬렉션 관리
- N:M 관계를 도메인에서 표현 가능

**단점**:
- 매핑 비용 증가
- 컬렉션이 크면 성능 이슈

#### Strategy C: Query는 Projection, Command는 중간 엔티티

**현실적인 절충**:

```java
// Command: 중간 엔티티로 관리
@Service
class PostService {
    fun addTag(postId: Long, tagId: Long) {
        // 중간 테이블 직접 조작
        val postTag = PostTagEntity(postId = postId, tagId = tagId)
        postTagRepository.save(postTag)
    }
}

// Query: Projection으로 조회
@Query("SELECT new com.veri.dto.PostTagResponse(p.id, p.title, t.id, t.name) FROM PostEntity p JOIN PostTagEntity pt ON p.id = pt.postId JOIN TagEntity t ON pt.tagId = t.id WHERE p.id = :postId")
fun findPostTags(@Param("postId") postId: Long): List<PostTagResponse>
```

---

### 3.3 Veri-BE 구체적 예시

**Post ↔ Member (좋아요)**

```java
// 중간 테이블 엔티티
@Entity
@Table(name = "post_likes", indexes = [
    Index(name = "idx_post_id", columnList = "post_id"),
    Index(name = "idx_member_id", columnList = "member_id")
])
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

@Embeddable
class PostLikeId(
    var postId: Long = 0L,
    var memberId: Long = 0L
) : Serializable

// Repository
interface PostLikeRepository : JpaRepository<PostLikeEntity, PostLikeId> {
    fun existsByPostIdAndMemberId(postId: Long, memberId: Long): Boolean
    fun countByPostId(postId: Long): Long
}

// Domain: Post는 좋아요 수만 보유
class Post(
    val id: PostId,
    val title: String,
    private var _likeCount: Long = 0L
) {
    fun incrementLikeCount() {
        _likeCount++
    }

    fun decrementLikeCount() {
        if (_likeCount > 0) _likeCount--
    }

    fun getLikeCount(): Long = _likeCount
}

// Service
@Service
class PostService {
    fun likePost(postId: Long, memberId: Long) {
        // 이미 좋아요 했는지 체크
        if (postLikeRepository.existsByPostIdAndMemberId(postId, memberId)) {
            return
        }

        // 중간 테이블 저장
        val postLike = PostLikeEntity(
            id = PostLikeId(postId, memberId),
            post = null,
            member = null
        )
        postLikeRepository.save(postLike)

        // Post 도메인 상태 변경
        val post = postRepository.findById(PostId.of(postId))
        post.incrementLikeCount()
        postRepository.save(post)
    }
}
```

---

## 4. Veri-BE 적용 계획

### 4.1 도메인별 우선순위

| Domain | ROI | 복잡도 | 추천 순위 | 이유 |
|--------|-----|--------|----------|------|
| **Comment** | 높음 | 낮음 | 1순위 | 규칙이 많음 (삭제, 권한, 답글) |
| **Post** | 높음 | 중간 | 2순위 | 좋아요, 게시 상태 전이 |
| **Card** | 중간 | 중간 | 3순위 | 독서(Reading)과 연계 |
| **Member** | 낮음 | 낮음 | 4순위 | 단순 CRUD |
| **Book** | 낮음 | 낮음 | 5순위 | 조회 중심 |
| **Auth** | 낮음 | 낮음 | 6순위 | 인증만 처리 |

### 4.2 단계적 적용 (Minimum Viable DDD)

#### Phase 1: Comment 도메인 (1주)

**목표**: 3가지 규칙 적용

1. **Comment 도메인 POJO 생성**
   - `Comment` class (행동 포함)
   - `CommentId`, `CommentContent` (VO)
   - 규칙 메서드 (`editBy`, `deleteBy`, `replyTo`)

2. **Command 경로에만 적용**
   - `CommentCommandService`는 도메인 사용
   - `CommentQueryService`는 엔티티 Projection 유지

3. **엔티티 Setter 제한**
   - Entity setter를 `internal`로 제한
   - Mapper만 접근 가능

4. **1:N 답글 처리**
   - Post는 댓글 ID만 보유
   - 답글은 별도 조회

#### Phase 2: Post 도메인 (1주)

1. **Post 도메인 POJO**
   - 상태 전이 (DRAFT → PUBLISHED → DELETED)
   - 좋아요 수 관리

2. **중간 테이블 처리**
   - `PostLikeEntity`는 그대로 사용
   - Post 도메인은 좋아요 수만 보유

#### Phase 3: Card, Book, 나머지 (2주)

각 도메인에 Phase 1 패턴 적용

---

## 5. 구현 예시: Comment 도메인

### 5.1 도메인 POJO

```java
package org.veri.domain.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Comment(
    val id: CommentId,
    val postId: Long,
    val authorId: Long,
    content: CommentContent,
    val createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    isDeleted: Boolean,
    // 답글은 직접 들고 있지 않음 (1:N 비용 회피)
) {
    private val _domainEvents: MutableList<DomainEvent> = ArrayList()

    // Factory method
    companion object {
        fun create(postId: Long, authorId: Long, content: CommentContent): Comment {
            val comment = Comment(
                id = CommentId.generate(),
                postId = postId,
                authorId = authorId,
                content = content,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                isDeleted = false
            )
            comment.registerEvent(CommentPostedEvent(comment.id, comment.postId, comment.authorId))
            return comment
        }
    }

    // 행동 (상태 변경의 유일한 진입점)
    fun editBy(requesterId: Long, newContent: CommentContent) {
        validateAuthor(requesterId)
        validateNotDeleted()

        this.content = newContent
        this.updatedAt = LocalDateTime.now()
    }

    fun deleteBy(requesterId: Long) {
        validateAuthor(requesterId)
        validateNotDeleted()

        // Soft delete
        this.content = CommentContent.of("삭제된 댓글입니다")
        this.isDeleted = true
        this.updatedAt = LocalDateTime.now()
    }

    // 규칙 검증
    private fun validateAuthor(requesterId: Long) {
        if (authorId != requesterId) {
            throw DomainException("UNAUTHORIZED", "댓글 작성자만 수정 가능")
        }
    }

    private fun validateNotDeleted() {
        if (isDeleted) {
            throw DomainException("ALREADY_DELETED", "삭제된 댓글은 수정 불가")
        }
    }

    // 이벤트 관리
    private fun registerEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    fun pullDomainEvents(): List<DomainEvent> {
        val events = _domainEvents.toList()
        _domainEvents.clear()
        return events
    }
}
```

### 5.2 VO

```java
// CommentId.kt
@JvmInline
value class CommentId(val value: Long) {
    init {
        require(value > 0) { "Invalid CommentId: $value" }
    }

    companion object {
        fun generate(): CommentId = CommentId(System.currentTimeMillis())
    }
}

// CommentContent.kt
@JvmInline
value class CommentContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Content is blank" }
        require(value.length <= 2000) { "Content too long: ${value.length}" }
    }

    companion object {
        fun of(value: String): CommentContent = CommentContent(value.trim())
    }
}
```

### 5.3 Command Service

```java
@Service
@Transactional
class CommentCommandService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository
) {
    fun editComment(command: EditCommentCommand) {
        // 1. 조회 (도메인으로)
        val comment = commentRepository.findById(CommentId.of(command.commentId))
            ?: throw NotFoundException("Comment not found")

        // 2. 상태 변경 (도메인 메서드만)
        comment.editBy(command.memberId, CommentContent.of(command.content))

        // 3. 저장
        commentRepository.save(comment)
    }

    fun deleteComment(command: DeleteCommentCommand) {
        val comment = commentRepository.findById(CommentId.of(command.commentId))
            ?: throw NotFoundException("Comment not found")

        comment.deleteBy(command.memberId)

        commentRepository.save(comment)
    }
}
```

### 5.4 Query Service (Projection)

```java
@Service
@Transactional(readOnly = true)
class CommentQueryService(
    private val commentRepository: CommentRepository
) {
    fun getCommentById(id: Long): CommentResponse {
        // 엔티티에서 바로 Projection (도메인 경유 X)
        return commentRepository.findProjectionById(id)
            ?: throw NotFoundException("Comment not found")
    }

    fun getCommentsByPost(postId: Long, page: Pageable): Page<CommentSummaryResponse> {
        // Projection으로 페이징 조회
        return commentRepository.findProjectionsByPostId(postId, page)
    }
}
```

### 5.5 Repository

```java
interface CommentRepository : JpaRepository<CommentEntity, Long> {
    // Query용 Projection (빠름)
    @Query("""
        SELECT new org.veri.dto.CommentResponse(
            c.id, c.content, c.authorId, a.nickname, c.createdAt, c.isDeleted
        )
        FROM CommentEntity c
        JOIN AuthorEntity a ON c.authorId = a.id
        WHERE c.id = :id
    """)
    fun findProjectionById(@Param("id") id: Long): CommentResponse?

    @Query("""
        SELECT new org.veri.dto.CommentSummaryResponse(
            c.id, c.content, c.authorId, a.nickname, c.createdAt
        )
        FROM CommentEntity c
        JOIN AuthorEntity a ON c.authorId = a.id
        WHERE c.postId = :postId AND c.parentId IS NULL
        ORDER BY c.createdAt ASC
    """)
    fun findProjectionsByPostId(
        @Param("postId") postId: Long,
        page: Pageable
    ): Page<CommentSummaryResponse>

    // Command용 (도메인 변환)
    @Query("SELECT c FROM CommentEntity c LEFT JOIN FETCH c.author WHERE c.id = :id")
    fun findDomainById(@Param("id") id: Long): CommentEntity?
}
```

---

## 6. 기대 효과 (체감 가능한 변화)

### 6.1 Before

```java
// 규칙이 서비스에 흩어짐
@Service
class CommentService {
    fun editComment(id: Long, memberId: Long, content: String) {
        val comment = repository.findById(id)

        // 규칙 1: 여기서 체크
        if (comment.authorId != memberId) throw Unauthorized()

        // 규칙 2: 여기서 체크
        if (comment.isDeleted) throw AlreadyDeleted()

        // 규칙 3: 여기서 체크
        if (content.isBlank()) throw Invalid()

        // 상태 변경
        comment.content = content  // Setter!
        comment.updatedAt = now()
    }
}

// 다른 서비스에서 중복
@Service
class CommentModerationService {
    fun moderateComment(id: Long, moderatorId: Long) {
        val comment = repository.findById(id)

        // 규칙 1: 또 체크
        if (comment.authorId != moderatorId) throw Unauthorized()

        // 규칙 2: 또 체크
        if (comment.isDeleted) throw AlreadyDeleted()

        comment.delete()
    }
}
```

### 6.2 After

```java
// 도메인이 모든 규칙 보유
class Comment {
    fun editBy(requesterId: Long, newContent: CommentContent) {
        validateAuthor(requesterId)   // 규칙 1
        validateNotDeleted()          // 규칙 2
        // 규칙 3은 VO가 검증

        this.content = newContent
        this.updatedAt = LocalDateTime.now()
    }

    fun deleteBy(requesterId: Long) {
        validateAuthor(requesterId)   // 규칙 1 (재사용)
        validateNotDeleted()          // 규칙 2 (재사용)

        this.isDeleted = true
    }

    private fun validateAuthor(requesterId: Long) {
        if (authorId != requesterId) throw DomainException("UNAUTHORIZED")
    }

    private fun validateNotDeleted() {
        if (isDeleted) throw DomainException("ALREADY_DELETED")
    }
}

// 서비스는 단순 호출
@Service
class CommentService {
    fun editComment(id: Long, memberId: Long, content: String) {
        val comment = repository.findById(id)
        comment.editBy(memberId, CommentContent.of(content))  // 규칙 실행
        repository.save(comment)
    }
}

// 다른 서비스에서도 재사용
@Service
class CommentModerationService {
    fun moderateComment(id: Long, moderatorId: Long) {
        val comment = repository.findById(id)
        comment.deleteBy(moderatorId)  // 같은 규칙 재사용
        repository.save(comment)
    }
}
```

---

## 7. 성공 기준

### 7.1 기술적 지표

| 지표 | Before | After | 목표 |
|------|--------|-------|------|
| **규칙 중복** | 서비스마다 중복 | 도메인에 집중 | -70% |
| **테스트 속도** | DB 필요 | 도메인만 테스트 가능 | 10x 빠름 |
| **엔티티 Setter 호출** | 100회 | 0회 (도메인 메서드만) | 100% 제거 |
| **CQS 준수** | ❌ | ✅ | Query는 Projection |

### 7.2 체감 가능한 변화

1. **"이 로직 어디 있지?"** → 도메인으로 수렴
2. **"테스트가 너무 느려"** → 도메인만 테스트 (DB X)
3. **"JPA 프록시 때문에..."** → 도메인은 자유로움
4. **"규칙 중복 제거"** → 도메인에 한 번만 정의

---

## 8. 요약

### 핵심 원칙

1. **상태 변경의 진입점을 도메인 POJO로 이동**
   - POJO가 엔티티 복사본이 아니라 행동을 가져야 함

2. **쓰기는 도메인, 읽기는 Projection**
   - Command: 도메인 POJO (규칙 실행)
   - Query: 엔티티 Projection (성능)

3. **엔티티 Setter 직접 호출 금지**
   - 상태 변경은 도메인 메서드만 통해서

4. **1:N은 Aggregate 경계에서만**
   - 전체 컬렉션을 도메인에 올리지 않음
   - 중간 테이블은 별도 Entity로 관리

### 최소 시작점

**Comment 도메인**부터 시작:
1. `Comment` 클래스 생성 (행동 포함)
2. `CommentCommandService`에서만 사용
3. `CommentQueryService`는 Projection 유지
4. 1주일에 완성하고 효과 검증

### 다음 단계

Comment 도메인에서 효과를 확인한 후:
- Post 도메인 적용
- Card 도메인 적용
- 나머지 도메인 적용
