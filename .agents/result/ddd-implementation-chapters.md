# Veri-BE DDD Implementation Playbook

**Status**: Ready for Execution
**Version**: 1.0
**Date**: 2025-12-31
**Prototype Domain**: Comment
**Estimated Duration**: 5-8 weeks

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Chapter 1: Foundation Setup](#chapter-1-foundation-setup)
- [Chapter 2: Prototype - Comment Domain](#chapter-2-prototype---comment-domain)
- [Chapter 3: Pattern Validation](#chapter-3-pattern-validation)
- [Chapter 4: Event Infrastructure](#chapter-4-event-infrastructure)
- [Chapter 5: Domain Migration](#chapter-5-domain-migration)
- [Chapter 6: Cross-Cutting Infrastructure](#chapter-6-cross-cutting-infrastructure)
- [Appendices](#appendices)

---

## Overview

### Mission Statement

Transform Veri-BE from layered architecture to **Hexagonal DDD Architecture** while maintaining continuous deployment and zero schema changes.

### Success Criteria

- [ ] All 7 domains migrated to DDD structure
- [ ] ArchUnit tests passing (domain purity, dependency rules)
- [ ] Domain test coverage > 80%
- [ ] Zero circular dependencies
- [ ] No performance regression (> 5% overhead)

### Non-Negotiable Constraints

1. **Domain Purity**: `core-domain` has ZERO Spring/JPA dependencies
2. **Zero Downtime**: Per-domain migration with feature flags if needed
3. **Schema Stability**: No database schema changes during migration
4. **Backward Compatibility**: API contracts remain unchanged

---

## Prerequisites

### Tool Installation

```bash
# Gradle plugin for architecture testing
# build.gradle.kts (project root)
plugins {
    id("com.tngtech.archunit") version "1.2.1"
}

dependencies {
    testImplementation("com.tngtech.archunit:archunit:1.2.1")
}
```

### Team Alignment

- [ ] All developers reviewed this playbook
- [ ] Pair programming assigned for Chapter 2 (learning phase)
- [ ] Code review checklist finalized
- [ ] Definition of Done agreed

### Environment Setup

```bash
# Branch strategy
git checkout -b feature/ddd-comment-domain

# Verification
./gradlew clean build
./gradlew test --tests "*ArchTest"
```

---

## Chapter 1: Foundation Setup

**Objective**: Establish architectural groundwork
**Duration**: 1-2 days
**Deliverables**: `core-domain` module, ArchUnit tests, build configuration

---

### 1.1 Create Core Domain Module

**File**: `core/core-domain/build.gradle.kts`

```kotlin
plugins {
    java
    kotlin("jvm")
}

group = "org.veri.be"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(project(":core:core-enum"))

    // Pure domain: NO Spring, NO JPA
    implementation("jakarta.validation:jakarta.validation-api:3.0.0")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

**Verification**:
```bash
./gradlew :core:core-domain:build
./gradlew :core:core-domain:dependencies --configuration runtimeClasspath
# Should NOT contain spring-*, jpa-, hibernate-
```

---

### 1.2 Define Architecture Constraints

**File**: `tests/src/test/java/org/veri/be/arch/DDDArchTest.java`

```java
package org.veri.be.arch;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.ArchUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class DDDArchTest {

    @Test
    @DisplayName("Domain layer should not depend on Spring or JPA")
    void domainPurity() {
        JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("org.veri.be.domain");

        noClasses()
            .that().resideInAPackage("..core.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "org.hibernate.."
            )
            .because("Domain must remain pure and portable")
            .check(importedClasses);
    }

    @Test
    @DisplayName("Application layer should depend only on domain ports")
    void applicationDependency() {
        JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("org.veri.be.application");

        classes()
            .that().resideInAPackage("..core.application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..core.domain..",
                "..core.enum..",
                "java..",
                "jakarta.validation..",
                "org.springframework.stereotype..",
                "org.springframework.transaction.."
            )
            .because("Application should use domain ports, not infrastructure")
            .check(importedClasses);
    }

    @Test
    @DisplayName("Controllers should only delegate to application")
    void controllerSimplicity() {
        JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("org.veri.be.api");

        classes()
            .that().resideInAPackage("..api..controller..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..core.application..",
                "org.springframework.web..",
                "org.springframework.validation..",
                "jakarta.validation.."
            )
            .because("Controllers should be thin delegates")
            .check(importedClasses);
    }

    @Test
    @DisplayName("Prevent circular dependencies")
    void noCircularDependencies() {
        JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("org.veri.be");

        slices()
            .matching("org.veri.be.(*)..")
            .should().beFreeOfCycles()
            .because("Circular dependencies indicate architecture problems")
            .check(importedClasses);
    }
}
```

**Verification**:
```bash
./gradlew test --tests "*DDDArchTest"
```

---

### 1.3 Create Domain Package Structure

**Directory Layout**:
```
core/core-domain/src/main/java/org/veri/be/domain/
├── shared/
│   ├── exception/
│   │   └── DomainException.java
│   └── event/
│       └── DomainEvent.java (marker interface)
└── comment/ (to be implemented in Chapter 2)
```

**Base Classes**:

```java
// shared/exception/DomainException.java
package org.veri.be.domain.shared.exception;

public abstract class DomainException extends RuntimeException {
    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

```java
// shared/event/DomainEvent.java
package org.veri.be.domain.shared.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredOn();
}
```

---

### 1.4 Update Settings

**File**: `settings.gradle.kts`

```kotlin
rootProject.name = "Veri-BE"

include(
    "clients:client-aws",
    "clients:client-ocr",
    "clients:client-search",
    "core:core-api",
    "core:core-app",
    "core:core-enum",
    "core:core-domain",        // NEW
    "core:core-application",   // NEW
    "storage:db-core",
    "support:common",
    "support:events",          // NEW
    "support:logging",
    "support:monitoring",
    "tests"
)
```

---

### Chapter 1 Checklist

- [ ] `core/core-domain/build.gradle.kts` created (NO Spring)
- [ ] `tests/src/test/java/.../DDDArchTest.java` created
- [ ] Base domain classes created (DomainException, DomainEvent)
- [ ] `settings.gradle.kts` updated
- [ ] Build successful: `./gradlew clean build`
- [ ] ArchUnit tests passing (expected to fail initially)
- [ ] Team review: Domain purity constraints understood

---

## Chapter 2: Prototype - Comment Domain

**Objective**: Complete vertical slice for comment domain
**Duration**: 5-7 days
**Deliverables**: Working DDD structure for comment, all tests passing

**Why Comment First?**
- Simplest domain (CRUD + reply hierarchy)
- Clear aggregate boundary (Comment aggregate)
- No external integrations (unlike Card/Image)
- Learning opportunity before complex domains

---

### 2.1 Domain Model Implementation

**Duration**: 1 day

#### 2.1.1 Value Objects

```java
// comment/model/CommentId.java
package org.veri.be.domain.comment.model;

import java.util.Objects;

public final class CommentId {
    private final Long value;

    private CommentId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid CommentId");
        }
        this.value = value;
    }

    public static CommentId of(Long value) {
        return new CommentId(value);
    }

    public static CommentId generate() {
        return new CommentId(System.currentTimeMillis());  // Simplified
    }

    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentId commentId = (CommentId) o;
        return Objects.equals(value, commentId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "CommentId{" + value + "}";
    }
}
```

```java
// comment/model/CommentContent.java
package org.veri.be.domain.comment.model;

import java.util.Objects;

public final class CommentContent {
    private static final int MAX_LENGTH = 2000;

    private final String value;

    private CommentContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Content cannot be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Content exceeds max length of " + MAX_LENGTH
            );
        }
        this.value = value.trim();
    }

    public static CommentContent of(String value) {
        return new CommentContent(value);
    }

    public String value() {
        return value;
    }

    public int length() {
        return value.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentContent that = (CommentContent) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "CommentContent{" + value.substring(0, Math.min(20, value.length())) + "...}";
    }
}
```

#### 2.1.2 Domain Events

```java
// comment/event/CommentPosted.java
package org.veri.be.domain.comment.event;

import org.veri.be.domain.comment.model.CommentId;
import org.veri.be.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record CommentPosted(
    CommentId commentId,
    Long postId,
    Long postAuthorId,
    Long authorId,
    String content,
    LocalDateTime occurredOn
) implements DomainEvent {
    public CommentPosted {
        if (occurredOn == null) {
            throw new IllegalArgumentException("occurredOn cannot be null");
        }
    }

    public static CommentPosted create(
        CommentId commentId,
        Long postId,
        Long postAuthorId,
        Long authorId,
        String content
    ) {
        return new CommentPosted(
            commentId,
            postId,
            postAuthorId,
            authorId,
            content,
            LocalDateTime.now()
        );
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
}
```

```java
// comment/event/CommentReplied.java
package org.veri.be.domain.comment.event;

import org.veri.be.domain.comment.model.CommentId;
import org.veri.be.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record CommentReplied(
    CommentId replyId,
    CommentId parentCommentId,
    Long parentAuthorId,
    Long replyAuthorId,
    String content,
    LocalDateTime occurredOn
) implements DomainEvent {
    public CommentReplied {
        if (occurredOn == null) {
            throw new IllegalArgumentException("occurredOn cannot be null");
        }
    }

    public static CommentReplied create(
        CommentId replyId,
        CommentId parentCommentId,
        Long parentAuthorId,
        Long replyAuthorId,
        String content
    ) {
        return new CommentReplied(
            replyId,
            parentCommentId,
            parentAuthorId,
            replyAuthorId,
            content,
            LocalDateTime.now()
        );
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
}
```

#### 2.1.3 Aggregate Root

```java
// comment/model/Comment.java
package org.veri.be.domain.comment.model;

import org.veri.be.domain.comment.event.CommentPosted;
import org.veri.be.domain.comment.event.CommentReplied;
import org.veri.be.domain.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Comment {

    private final CommentId id;
    private final Long postId;
    private final Long authorId;
    private CommentContent content;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final boolean isDeleted;
    private final List<Comment> replies;
    private final List<DomainEvent> domainEvents;

    // Factory method for new comment
    public static Comment create(Long postId, Long authorId, CommentContent content) {
        Comment comment = new Comment(
            CommentId.generate(),
            postId,
            authorId,
            content,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            false,
            new ArrayList<>(),
            new ArrayList<>()
        );

        comment.registerEvent(CommentPosted.create(
            comment.id,
            comment.postId,
            null,  // Will be set by application service
            comment.authorId,
            comment.content.value()
        ));

        return comment;
    }

    // Factory method for reply
    public static Comment createReply(Comment parent, Long authorId, CommentContent content) {
        if (parent.isDeleted) {
            throw new DomainException("CANNOT_REPLY", "Cannot reply to deleted comment");
        }

        Comment reply = new Comment(
            CommentId.generate(),
            parent.postId,
            authorId,
            content,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            false,
            new ArrayList<>(),
            new ArrayList<>()
        );

        reply.registerEvent(CommentReplied.create(
            reply.id,
            parent.id,
            parent.authorId,
            reply.authorId,
            reply.content.value()
        ));

        return reply;
    }

    // Restore from persistence
    public static Comment restore(
        CommentId id,
        Long postId,
        Long authorId,
        CommentContent content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        boolean isDeleted,
        List<Comment> replies
    ) {
        return new Comment(
            id,
            postId,
            authorId,
            content,
            createdAt,
            updatedAt,
            deletedAt,
            isDeleted,
            replies != null ? new ArrayList<>(replies) : new ArrayList<>(),
            new ArrayList<>()
        );
    }

    // Private constructor
    private Comment(
        CommentId id,
        Long postId,
        Long authorId,
        CommentContent content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        boolean isDeleted,
        List<Comment> replies,
        List<DomainEvent> domainEvents
    ) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.isDeleted = isDeleted;
        this.replies = Collections.unmodifiableList(replies);
        this.domainEvents = domainEvents;
    }

    // Domain behavior
    public void editContent(Long requesterId, CommentContent newContent) {
        if (isDeleted) {
            throw new DomainException("COMMENT_DELETED", "Cannot edit deleted comment");
        }
        if (!authorId.equals(requesterId)) {
            throw new DomainException("UNAUTHORIZED", "Not the comment author");
        }

        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete(Long requesterId) {
        if (isDeleted) {
            throw new DomainException("ALREADY_DELETED", "Comment already deleted");
        }
        if (!authorId.equals(requesterId)) {
            throw new DomainException("UNAUTHORIZED", "Not the comment author");
        }

        // Soft delete
        this.content = CommentContent.of("삭제된 댓글입니다");
        this.updatedAt = LocalDateTime.now();
    }

    public void addReply(Comment reply) {
        if (isDeleted) {
            throw new DomainException("COMMENT_DELETED", "Cannot reply to deleted comment");
        }
        this.replies.add(reply);
    }

    // Event management
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    // Getters
    public CommentId getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public CommentContent getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public boolean isDeleted() { return isDeleted; }
    public List<Comment> getReplies() { return replies; }
}
```

---

### 2.2 Repository Port Definition

**Duration**: 0.5 day

```java
// comment/port/CommentRepository.java
package org.veri.be.domain.comment.port;

import org.veri.be.domain.comment.model.Comment;
import org.veri.be.domain.comment.model.CommentId;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(CommentId id);

    List<Comment> findByPostId(Long postId);

    List<Comment> findByAuthorId(Long authorId);

    Comment save(Comment comment);

    void delete(CommentId id);

    boolean existsByPostIdAndAuthorId(Long postId, Long authorId);
}
```

---

### 2.3 Domain Model Tests

**Duration**: 0.5 day

```java
// core-domain/src/test/java/.../comment/model/CommentTest.java
package org.veri.be.domain.comment.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.shared.exception.DomainException;

import static org.assertj.core.api.Assertions.*;

class CommentTest {

    @Test
    @DisplayName("댓글을 생성한다")
    void createComment() {
        // When
        Comment comment = Comment.create(
            1L,  // postId
            100L,  // authorId
            CommentContent.of("첫 댓글입니다")
        );

        // Then
        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getPostId()).isEqualTo(1L);
        assertThat(comment.getAuthorId()).isEqualTo(100L);
        assertThat(comment.getContent().value()).isEqualTo("첫 댓글입니다");
        assertThat(comment.getDomainEvents()).hasSize(1);
        assertThat(comment.getDomainEvents().get(0))
            .isInstanceOf(org.veri.be.domain.comment.event.CommentPosted.class);
    }

    @Test
    @DisplayName("댓글 내용이 2000자를 초과하면 예외 발생")
    void contentMaxLength() {
        // When & Then
        assertThatThrownBy(() ->
            CommentContent.of("a".repeat(2001))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("max length");
    }

    @Test
    @DisplayName("빈 내용으로 댓글 생성 불가")
    void blankContent() {
        // When & Then
        assertThatThrownBy(() ->
            CommentContent.of("   ")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("댓글 내용을 수정한다")
    void editComment() {
        // Given
        Comment comment = Comment.create(1L, 100L, CommentContent.of("원본 내용"));

        // When
        comment.editContent(100L, CommentContent.of("수정된 내용"));

        // Then
        assertThat(comment.getContent().value()).isEqualTo("수정된 내용");
        assertThat(comment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("타인의 댓글은 수정 불가")
    void editCommentUnauthorized() {
        // Given
        Comment comment = Comment.create(1L, 100L, CommentContent.of("원본"));

        // When & Then
        assertThatThrownBy(() ->
            comment.editContent(999L, CommentContent.of("수정"))
        ).isInstanceOf(DomainException.class)
         .extracting("errorCode")
         .isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("답글을 생성한다")
    void createReply() {
        // Given
        Comment parent = Comment.create(1L, 100L, CommentContent.of("부모 댓글"));

        // When
        Comment reply = Comment.createReply(parent, 200L, CommentContent.of("답글"));

        // Then
        assertThat(reply.getPostId()).isEqualTo(parent.getPostId());
        assertThat(reply.getAuthorId()).isEqualTo(200L);
        assertThat(reply.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("삭제된 댓글에는 답글 불가")
    void replyToDeletedComment() {
        // Given
        Comment parent = Comment.create(1L, 100L, CommentContent.of("부모"));
        parent.delete(100L);

        // When & Then
        assertThatThrownBy(() ->
            Comment.createReply(parent, 200L, CommentContent.of("답글"))
        ).isInstanceOf(DomainException.class)
         .extracting("errorCode")
         .isEqualTo("CANNOT_REPLY");
    }
}
```

**Verification**:
```bash
./gradlew :core:core-domain:test --tests "*CommentTest"
```

---

### 2.4 Persistence Adapter

**Duration**: 1.5 days

#### 2.4.1 JPA Entity

```java
// storage/db-core/adapter/comment/entity/CommentEntity.java
package org.veri.be.db.core.adapter.comment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_post_id", columnList = "post_id"),
    @Index(name = "idx_author_id", columnList = "author_id"),
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> replies = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters for JPA
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public CommentEntity getParent() { return parent; }
    public void setParent(CommentEntity parent) { this.parent = parent; }

    public List<CommentEntity> getReplies() { return replies; }
    public void setReplies(List<CommentEntity> replies) { this.replies = replies; }
}
```

#### 2.4.2 Mapper

```java
// storage/db-core/adapter/comment/mapper/CommentMapper.java
package org.veri.be.db.core.adapter.comment.mapper;

import org.veri.be.db.core.adapter.comment.entity.CommentEntity;
import org.veri.be.domain.comment.model.Comment;
import org.veri.be.domain.comment.model.CommentContent;
import org.veri.be.domain.comment.model.CommentId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public Comment toDomain(CommentEntity entity) {
        List<Comment> replyDomains = entity.getReplies().stream()
            .map(this::toDomain)
            .collect(Collectors.toList();

        return Comment.restore(
            CommentId.of(entity.getId()),
            entity.getPostId(),
            entity.getAuthorId(),
            CommentContent.of(entity.getContent()),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt(),
            entity.isDeleted(),
            replyDomains
        );
    }

    public CommentEntity toEntity(Comment domain) {
        CommentEntity entity = new CommentEntity();
        entity.setId(domain.getId().value());
        entity.setPostId(domain.getPostId());
        entity.setAuthorId(domain.getAuthorId());
        entity.setContent(domain.getContent().value());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.isDeleted() ? domain.getUpdatedAt() : null);
        entity.setDeleted(domain.isDeleted());

        // Map replies
        List<CommentEntity> replyEntities = domain.getReplies().stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
        entity.setReplies(replyEntities);

        return entity;
    }

    public CommentEntity toEntityForUpdate(Comment domain, CommentEntity existing) {
        existing.setContent(domain.getContent().value());
        existing.setUpdatedAt(domain.getUpdatedAt());
        existing.setDeletedAt(domain.isDeleted() ? domain.getUpdatedAt() : null);
        existing.setDeleted(domain.isDeleted());
        return existing;
    }
}
```

#### 2.4.3 Repository Implementation

```java
// storage/db-core/adapter/comment/persistence/CommentRepositoryImpl.java
package org.veri.be.db.core.adapter.comment.persistence;

import org.veri.be.db.core.adapter.comment.entity.CommentEntity;
import org.veri.be.db.core.adapter.comment.mapper.CommentMapper;
import org.veri.be.domain.comment.model.Comment;
import org.veri.be.domain.comment.model.CommentId;
import org.veri.be.domain.comment.port.CommentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentJpaRepository jpaRepository;
    private final CommentMapper mapper;

    public CommentRepositoryImpl(
        CommentJpaRepository jpaRepository,
        CommentMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Comment> findById(CommentId id) {
        return jpaRepository.findByIdWithReplies(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        return jpaRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Comment> findByAuthorId(Long authorId) {
        return jpaRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Comment save(Comment comment) {
        CommentEntity entity = mapper.toEntity(comment);
        CommentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(CommentId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByPostIdAndAuthorId(Long postId, Long authorId) {
        return jpaRepository.existsByPostIdAndAuthorId(postId, authorId);
    }
}
```

```java
// storage/db-core/adapter/comment/persistence/CommentJpaRepository.java
package org.veri.be.db.core.adapter.comment.persistence;

import org.veri.be.db.core.adapter.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT c FROM CommentEntity c LEFT JOIN FETCH c.replies WHERE c.id = :id")
    Optional<CommentEntity> findByIdWithReplies(@Param("id") Long id);

    List<CommentEntity> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(Long postId);

    List<CommentEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    boolean existsByPostIdAndAuthorId(Long postId, Long authorId);
}
```

---

### 2.5 Application Service Layer

**Duration**: 1.5 days

#### 2.5.1 Command DTOs

```java
// core-application/comment/dto/PostCommentCommand.java
package org.veri.be.application.comment.dto;

public record PostCommentCommand(
    Long postId,
    String content
) {
    public PostCommentCommand {
        if (postId == null) {
            throw new IllegalArgumentException("postId cannot be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be blank");
        }
    }
}
```

```java
// core-application/comment/dto/PostReplyCommand.java
package org.veri.be.application.comment.dto;

public record PostReplyCommand(
    Long parentCommentId,
    String content
) {
    public PostReplyCommand {
        if (parentCommentId == null) {
            throw new IllegalArgumentException("parentCommentId cannot be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be blank");
        }
    }
}
```

```java
// core-application/comment/dto/EditCommentCommand.java
package org.veri.be.application.comment.dto;

public record EditCommentCommand(
    String content
) {
    public EditCommentCommand {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be blank");
        }
    }
}
```

#### 2.5.2 Result DTOs

```java
// core-application/comment/dto/CommentResult.java
package org.veri.be.application.comment.dto;

public record CommentResult(
    Long id,
    Long postId,
    Long authorId,
    String authorNickname,  // Will be populated by QueryService
    String content,
    String createdAt,
    String updatedAt,
    boolean isDeleted,
    int replyCount
) {
    public static CommentResult from(
        org.veri.be.domain.comment.model.Comment comment,
        String authorNickname
    ) {
        return new CommentResult(
            comment.getId().value(),
            comment.getPostId(),
            comment.getAuthorId(),
            authorNickname,
            comment.getContent().value(),
            comment.getCreatedAt().toString(),
            comment.getUpdatedAt().toString(),
            comment.isDeleted(),
            comment.getReplies().size()
        );
    }
}
```

#### 2.5.3 Command Service

```java
// core-application/comment/service/CommentCommandService.java
package org.veri.be.application.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.application.comment.dto.*;
import org.veri.be.domain.comment.model.Comment;
import org.veri.be.domain.comment.model.CommentContent;
import org.veri.be.domain.comment.model.CommentId;
import org.veri.be.domain.comment.port.CommentRepository;

@Service
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final SpringCommentEventPublisher eventPublisher;

    public CommentCommandService(
        CommentRepository commentRepository,
        SpringCommentEventPublisher eventPublisher
    ) {
        this.commentRepository = commentRepository;
        this.eventPublisher = eventPublisher;
    }

    public CommentId postComment(Long authorId, PostCommentCommand command) {
        // Validate post exists (will be done via PostRepository port)
        Comment comment = Comment.create(
            command.postId(),
            authorId,
            CommentContent.of(command.content())
        );

        Comment saved = commentRepository.save(comment);

        // Publish domain events
        comment.pullDomainEvents().forEach(eventPublisher::publish);

        return saved.getId();
    }

    public CommentId postReply(Long authorId, PostReplyCommand command) {
        Comment parent = commentRepository.findById(CommentId.of(command.parentCommentId()))
            .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));

        Comment reply = Comment.createReply(
            parent,
            authorId,
            CommentContent.of(command.content())
        );

        parent.addReply(reply);
        commentRepository.save(parent);

        // Publish events
        parent.pullDomainEvents().forEach(eventPublisher::publish);
        reply.pullDomainEvents().forEach(eventPublisher::publish);

        return reply.getId();
    }

    public void editComment(Long requesterId, CommentId commentId, EditCommentCommand command) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        comment.editContent(requesterId, CommentContent.of(command.content()));
        commentRepository.save(comment);
    }

    public void deleteComment(Long requesterId, CommentId commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        comment.delete(requesterId);
        commentRepository.save(comment);
    }
}
```

---

### 2.6 API Controller Refactoring

**Duration**: 0.5 day

```java
// core-api/comment/controller/CommentController.java
package org.veri.be.api.comment.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.veri.api.common.dto.ApiResponse;
import org.veri.api.comment.dto.request.*;
import org.veri.api.comment.dto.response.CommentResponse;
import org.veri.api.comment.dto.response.CommentResponseConverter;
import org.veri.application.comment.dto.*;
import org.veri.application.comment.service.CommentCommandService;
import org.veri.application.comment.service.CommentQueryService;
import org.veri.domain.member.entity.Member;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;

    @PostMapping
    public ApiResponse<CommentResponse> postComment(
        @AuthenticationPrincipal Member member,
        @Valid @RequestBody CommentPostRequest request
    ) {
        PostCommentCommand command = new PostCommentCommand(
            request.postId(),
            request.content()
        );

        CommentId commentId = commentCommandService.postComment(member.getId(), command);

        CommentResult result = commentQueryService.getCommentById(commentId);
        return ApiResponse.success(CommentResponseConverter.toResponse(result));
    }

    @PostMapping("/{commentId}/replies")
    public ApiResponse<CommentResponse> postReply(
        @PathVariable Long commentId,
        @AuthenticationPrincipal Member member,
        @Valid @RequestBody CommentReplyRequest request
    ) {
        PostReplyCommand command = new PostReplyCommand(commentId, request.content());

        CommentId replyId = commentCommandService.postReply(member.getId(), command);

        CommentResult result = commentQueryService.getCommentById(replyId);
        return ApiResponse.success(CommentResponseConverter.toResponse(result));
    }

    @PutMapping("/{commentId}")
    public ApiResponse<Void> editComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal Member member,
        @Valid @RequestBody CommentEditRequest request
    ) {
        EditCommentCommand command = new EditCommentCommand(request.content());

        commentCommandService.editComment(
            member.getId(),
            CommentId.of(commentId),
            command
        );

        return ApiResponse.success();
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal Member member
    ) {
        commentCommandService.deleteComment(
            member.getId(),
            CommentId.of(commentId)
        );

        return ApiResponse.success();
    }
}
```

---

### 2.7 Integration Testing

**Duration**: 1 day

```java
// tests/src/test/java/.../integration/CommentIntegrationTest.java
package org.veri.be.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.veri.application.comment.dto.*;
import org.veri.application.comment.service.CommentCommandService;
import org.veri.application.comment.service.CommentQueryService;
import org.veri.domain.comment.model.CommentId;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CommentIntegrationTest {

    @Autowired
    private CommentCommandService commentCommandService;

    @Autowired
    private CommentQueryService commentQueryService;

    @Test
    @DisplayName("댓글 작성 통합 테스트")
    void postCommentIntegration() {
        // Given
        Long authorId = 1L;
        Long postId = 100L;
        PostCommentCommand command = new PostCommentCommand(postId, "통합 테스트 댓글");

        // When
        CommentId commentId = commentCommandService.postComment(authorId, command);

        // Then
        CommentResult result = commentQueryService.getCommentById(commentId);
        assertThat(result.id()).isEqualTo(commentId.value());
        assertThat(result.content()).isEqualTo("통합 테스트 댓글");
        assertThat(result.authorId()).isEqualTo(authorId);
    }

    @Test
    @DisplayName("답글 작성 통합 테스트")
    void postReplyIntegration() {
        // Given
        Long authorId = 1L;
        Long postId = 100L;

        // Create parent comment
        PostCommentCommand parentCommand = new PostCommentCommand(postId, "부모 댓글");
        CommentId parentId = commentCommandService.postComment(authorId, parentCommand);

        // When
        PostReplyCommand replyCommand = new PostReplyCommand(parentId.value(), "답글");
        CommentId replyId = commentCommandService.postReply(authorId, replyCommand);

        // Then
        CommentResult parentResult = commentQueryService.getCommentById(parentId);
        assertThat(parentResult.replyCount()).isEqualTo(1);

        CommentResult replyResult = commentQueryService.getCommentById(replyId);
        assertThat(replyResult.content()).isEqualTo("답글");
    }

    @Test
    @DisplayName("댓글 수정 통합 테스트")
    void editCommentIntegration() {
        // Given
        Long authorId = 1L;
        PostCommentCommand postCommand = new PostCommentCommand(100L, "원본");
        CommentId commentId = commentCommandService.postComment(authorId, postCommand);

        // When
        EditCommentCommand editCommand = new EditCommentCommand("수정됨");
        commentCommandService.editComment(authorId, commentId, editCommand);

        // Then
        CommentResult result = commentQueryService.getCommentById(commentId);
        assertThat(result.content()).isEqualTo("수정됨");
    }

    @Test
    @DisplayName("댓글 삭제 통합 테스트")
    void deleteCommentIntegration() {
        // Given
        Long authorId = 1L;
        PostCommentCommand postCommand = new PostCommentCommand(100L, "삭제될 댓글");
        CommentId commentId = commentCommandService.postComment(authorId, postCommand);

        // When
        commentCommandService.deleteComment(authorId, commentId);

        // Then
        CommentResult result = commentQueryService.getCommentById(commentId);
        assertThat(result.isDeleted()).isTrue();
        assertThat(result.content()).isEqualTo("삭제된 댓글입니다");
    }
}
```

---

### Chapter 2 Checklist

- [ ] Domain model implemented (Comment, CommentId, CommentContent)
- [ ] Domain events implemented (CommentPosted, CommentReplied)
- [ ] Repository port defined in domain
- [ ] JPA entities created in storage/db-core
- [ ] Mapper implemented
- [ ] Repository implementation completed
- [ ] Application services (Command/Query) created
- [ ] Controllers refactored
- [ ] Domain unit tests passing (> 80% coverage)
- [ ] Integration tests passing
- [ ] ArchUnit tests passing
- [ ] Code review completed
- [ ] Documentation updated

---

## Chapter 3: Pattern Validation

**Objective**: Learn from prototype, refine patterns
**Duration**: 2-3 days
**Deliverables**: Pattern decision document, updated playbook

---

### 3.1 Retrospective

**Questions to Answer**:

1. **Mapper Complexity**
   - How many lines of mapper code per domain entity?
   - Did we forget to map any fields?
   - Is MapStruct needed?

2. **VO Usage**
   - Did VOs add value or boilerplate?
   - Were validation errors caught early?
   - Did JPA conversion cause issues?

3. **Event Handling**
   - Were events published correctly?
   - Did event handlers execute asynchronously?
   - Were there transaction issues?

4. **Test Coverage**
   - Were domain tests easy to write?
   - Did mocks work well?
   - Were integration tests slow?

---

### 3.2 Pattern Decision Document

**File**: `.agents/context/pattern-decisions.md`

```markdown
# Pattern Decisions - Veri-BE DDD

## Date: [After Chapter 2 completion]

## Decisions Made

### 1. Mapper Strategy
**Decision**: Strategy B (Separate Models + Mapper)
**Rationale**:
- [ ] Separation of concerns validated
- [ ] No JPA lifecycle conflicts
- [ ] Testability confirmed
**Overhead**: [X lines of mapper code per entity]
**Verdict**: Continue with Strategy B

### 2. Value Objects
**Decision**: [ ] Use VOs for IDs and simple values / [ ] Limited VO usage
**Rationale**:
- [ ] Validation caught early: YES/NO
- [ ] JPA conversion overhead: [X ms per entity]
**Verdict**: [ ] Continue / [ ] Adjust strategy

### 3. Aggregate Loading
**Decision**: [ ] Eager loading / [ ] Lazy loading with pagination
**Rationale**:
- N+1 query issues: YES/NO
- Performance impact: [X%]
**Verdict**: [ ] Continue / [ ] Optimize

## Action Items

- [ ] Update playbook based on learnings
- [ ] Create mapper utility template
- [ ] Define VO usage guidelines
```

---

### 3.3 Playbook Updates

**Update chapters 4-6 based on lessons learned**.

---

### Chapter 3 Checklist

- [ ] Retrospective conducted
- [ ] Pattern decision document created
- [ ] Playbook updated
- [ ] Team alignment on refined patterns

---

## Chapter 4: Event Infrastructure

**Objective**: Add event-driven architecture for comment domain
**Duration**: 2-3 days
**Deliverables**: Event module, Spring bridge, handlers

---

### 4.1 Support Events Module

**File**: `support/events/build.gradle.kts`

```kotlin
dependencies {
    implementation(project(":core:core-domain"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    testImplementation(project(":core:core-application"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

---

### 4.2 Spring Event Publisher

```java
// support/events/comment/SpringCommentEventPublisher.java
package org.veri.be.support.events.comment;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.veri.domain.comment.event.*;

@Component
public class SpringCommentEventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    public SpringCommentEventPublisher(ApplicationEventPublisher springEventPublisher) {
        this.springEventPublisher = springEventPublisher;
    }

    public void publish(CommentPosted event) {
        springEventPublisher.publishEvent(new CommentPostedSpringEvent(event));
    }

    public void publish(CommentReplied event) {
        springEventPublisher.publishEvent(new CommentRepliedSpringEvent(event));
    }
}
```

---

### 4.3 Async Configuration

```java
// support/events/config/AsyncConfig.java
package org.veri.be.support.events.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

### 4.4 Event Handlers

```java
// support/events/comment/CommentEventHandler.java
package org.veri.be.support.events.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CommentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CommentEventHandler.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventTaskExecutor")
    public void handleCommentPosted(CommentPostedSpringEvent event) {
        log.info("CommentPosted: commentId={}, postId={}, authorId={}",
            event.domainEvent().commentId(),
            event.domainEvent().postId(),
            event.domainEvent().authorId()
        );

        // TODO: Implement notification logic
        // TODO: Implement activity feed logic
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventTaskExecutor")
    public void handleCommentReplied(CommentRepliedSpringEvent event) {
        log.info("CommentReplied: replyId={}, parentAuthorId={}, replyAuthorId={}",
            event.domainEvent().replyId(),
            event.domainEvent().parentAuthorId(),
            event.domainEvent().replyAuthorId()
        );

        // TODO: Implement notification logic
    }
}
```

---

### 4.5 Event Tests

```java
// support/events/src/test/java/.../CommentEventHandlerTest.java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CommentEventHandlerTest {

    @Autowired
    private CommentCommandService commentCommandService;

    @Autowired
    private CommentEventHandler commentEventHandler;

    @Test
    @DisplayName("댓글 작성 시 이벤트 발행 및 핸들링")
    void commentPostedEvent() {
        // Wait for async events
        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                // Verify event handler was called
                // (would need to add verification logic)
            });
    }
}
```

---

### Chapter 4 Checklist

- [ ] `support/events` module created
- [ ] Spring event publisher implemented
- [ ] Async configuration added
- [ ] Event handlers created
- [ ] Event tests passing
- [ ] Performance measured (async overhead)

---

## Chapter 5: Domain Migration

**Objective**: Migrate remaining 6 domains
**Duration**: 14-21 days
**Deliverables**: All domains in DDD structure

---

### 5.1 Migration Order

| Priority | Domain | Duration | Complexity | Dependencies |
|----------|--------|----------|------------|--------------|
| 1 | **member** | 5-7 days | Low | None |
| 2 | **card** | 5-7 days | Medium | member, book (reading) |
| 3 | **post** | 7-10 days | Medium | member, book |
| 4 | **book** | 7-10 days | High | card, member |
| 5 | **auth** | 3-5 days | Low | member |
| 6 | **image** | 5-7 days | High | AWS client |

---

### 5.2 Per-Domain Checklist

For each domain, complete:

#### Phase 1: Domain Model (1-2 days)
- [ ] Aggregate root defined
- [ ] Value objects created (ID, content VOs)
- [ ] Domain events defined (pure POJOs)
- [ ] Factory methods implemented
- [ ] Domain behaviors implemented
- [ ] Port interfaces defined

#### Phase 2: Persistence (1-2 days)
- [ ] JPA entities created
- [ ] Mapper implemented
- [ ] Repository implementation completed
- [ ] Association handling (OneToOne, OneToMany, ManyToMany)
- [ ] Query optimization (JOIN FETCH, pagination)

#### Phase 3: Application (1-2 days)
- [ ] Command DTOs created
- [ ] Query DTOs created
- [ ] Command service implemented
- [ ] Query service implemented
- [ ] Facade for complex use cases (if needed)

#### Phase 4: API (0.5 day)
- [ ] Controllers refactored
- [ ] Request DTOs updated
- [ ] Response DTOs updated
- [ ] API documentation updated

#### Phase 5: Testing (1 day)
- [ ] Domain unit tests (> 80% coverage)
- [ ] Application service tests (mocked ports)
- [ ] Integration tests
- [ ] API tests (MockMvc)

#### Phase 6: Events (0.5 day, if applicable)
- [ ] Spring event publisher created
- [ ] Event handlers implemented
- [ ] Event tests passing

---

### 5.3 Migration Template

**For each domain**, follow Chapter 2 structure with domain-specific adaptations.

---

### Chapter 5 Checklist

- [ ] All 7 domains migrated
- [ ] ArchUnit tests passing for all domains
- [ ] Integration tests passing
- [ ] Performance benchmarks acceptable
- [ ] Code reviews completed

---

## Chapter 6: Cross-Cutting Infrastructure

**Objective**: Add shared infrastructure
**Duration**: 3-5 days
**Deliverables**: ArchUnit enforcement, monitoring, documentation

---

### 6.1 ArchUnit Enforcement

**File**: `tests/src/test/java/.../arch/ArchitectureVerificationTest.java`

Add comprehensive architecture tests as templates for future development.

---

### 6.2 Performance Benchmarking

**File**: `tests/src/test/java/.../performance/DomainPerformanceTest.java`

```java
class DomainPerformanceTest {

    @Test
    @DisplayName("매퍼 오버헤드 측정")
    void measureMapperOverhead() {
        // Benchmark mapper overhead
        // Target: < 5% overhead vs direct JPA
    }

    @Test
    @DisplayName("이벤트 발행 오버헤드 측정")
    void measureEventPublishingOverhead() {
        // Benchmark event publishing
        // Target: < 10ms per event
    }
}
```

---

### 6.3 Documentation

**Files to Update**:
- [ ] `.agents/context/ddd-patterns.md` - Pattern guidelines
- [ ] `.agents/context/domain-glossary.md` - Domain terminology
- [ ] `README.md` - Project overview

---

### Chapter 6 Checklist

- [ ] ArchUnit tests comprehensive
- [ ] Performance benchmarks acceptable
- [ ] Documentation complete
- [ ] Team training conducted

---

## Appendices

### Appendix A: Troubleshooting

| Issue | Solution | Reference |
|-------|----------|-----------|
| LazyLoadingException | Use JOIN FETCH in queries | Chapter 2.4.3 |
| Circular dependency | Check ArchUnit tests | Chapter 1.2 |
| Event not firing | Check @Async configuration | Chapter 4.3 |
| Mapper missing field | Add unit test for mapper | Chapter 2.4.2 |

---

### Appendix B: Code Review Checklist

- [ ] Domain has NO Spring dependencies
- [ ] Repository is interface in domain
- [ ] Domain events are pure POJOs
- [ ] Mapper has bidirectional tests
- [ ] Application service depends only on ports
- [ ] Controller has no business logic
- [ ] Tests cover domain behavior

---

### Appendix C: Definition of Done

A domain migration is complete when:
- [ ] All domain behaviors implemented in aggregates
- [ ] Port interfaces defined in domain
- [ ] Adapters implemented in infrastructure
- [ ] Application services use ports
- [ ] Controllers delegate to application
- [ ] Unit tests > 80% coverage (domain)
- [ ] Integration tests passing
- [ ] ArchUnit tests passing
- [ ] Code review approved
- [ ] Documentation updated

---

### Appendix D: Rollback Plan

If migration fails for a domain:
1. Revert domain-specific commits
2. Keep old service in `core-api` (marked deprecated)
3. Document failure reasons
4. Adjust patterns before retry

---

## Glossary

- **Aggregate Root**: Entity that serves as entry point to aggregate
- **Port**: Interface in domain layer (Hexagonal Architecture)
- **Adapter**: Implementation in infrastructure layer
- **VO**: Value Object (immutable, identified by value)
- **CQS**: Command Query Separation
- **DIP**: Dependency Inversion Principle

---

**End of Playbook**
