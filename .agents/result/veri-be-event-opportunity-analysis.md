# Veri-BE Event-Driven Refactoring Analysis

**Status**: Complete
**Date**: 2025-12-31
**Source**: Current service code analysis

---

## 1. Executive Summary

### 1.1 Current State Assessment

**Finding**: Veri-BE has **low coupling** between services compared to the reference commerce project.

**Evidence**:
- Services focus on their own responsibilities
- No direct service-to-service calls for side effects
- Clean CQS pattern implemented

**Conclusion**: **No urgent event-driven refactoring needed** for existing functionality.

### 1.2 Event Opportunities

**Primary Use Case**: **New features** that require cross-cutting concerns:

| Feature | Current State | With Events | Priority |
|---------|---------------|-------------|----------|
| **Notifications** | Not implemented | Decoupled notification handlers | **High** |
| **Search Indexing** | Not implemented | Async indexing on content changes | **Medium** |
| **Activity Feeds** | Not implemented | Event-based feed aggregation | **Medium** |
| **Analytics** | Not implemented | Non-blocking analytics collection | **Low** |

---

## 2. Service Analysis

### 2.1 PostCommandService Analysis

**Location**: `core/core-api/src/main/java/org/veri/be/domain/post/service/PostCommandService.java`

#### Current Code

```java
@Service
@RequiredArgsConstructor
public class PostCommandService {
    private final PostRepository postRepository;
    private final BookService bookService;
    private final StorageService storageService;
    private final LikePostRepository likePostRepository;

    @Transactional
    public Long createPost(PostCreateRequest request, Member member) {
        Book book = this.bookService.getBookById(request.bookId());

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .author(member)
                .book(book)
                .build();

        for (int i = 0; i < request.images().size(); i++) {
            post.addImage(request.images().get(i), i + 1L);
        }

        this.postRepository.save(post);
        return post.getId();
    }

    @Transactional
    public void publishPost(Long postId, Member member) {
        Post post = this.postQueryService.getPostById(postId);
        post.publishBy(member);
        postRepository.save(post);
    }

    @Transactional
    public LikeInfoResponse likePost(Long postId, Member member) {
        if (likePostRepository.existsByPostIdAndMemberId(postId, member.getId())) {
            return new LikeInfoResponse(likePostRepository.countByPostId(postId), true);
        }

        LikePost likePost = LikePost.builder()
                .post(postQueryService.getPostById(postId))
                .member(member)
                .build();

        likePostRepository.save(likePost);
        return new LikeInfoResponse(likePostRepository.countByPostId(postId), true);
    }
}
```

#### Analysis

| Method | Current Side Effects | Potential Side Effects (Future) |
|--------|---------------------|--------------------------------|
| `createPost()` | None | Author notification, search indexing, activity feed |
| `publishPost()` | None | Follower notification, search indexing |
| `likePost()` | None | Author notification, analytics tracking |

**Verdict**: **Clean implementation**. No direct coupling issues.

**Event Opportunities**:

1. **PostCreatedEvent**:
   - Notify author's followers
   - Index to search service
   - Add to activity feeds

2. **PostPublishedEvent**:
   - Send push notifications to followers
   - Update search index

3. **PostLikedEvent**:
   - Notify post author
   - Update analytics

#### Event-Driven Implementation

```java
// After event-driven refactoring

@Service
@RequiredArgsConstructor
public class PostCommandService {
    private final PostRepository postRepository;
    private final BookService bookService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createPost(PostCreateRequest request, Member member) {
        Book book = this.bookService.getBookById(request.bookId());

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .author(member)
                .book(book)
                .build();

        for (int i = 0; i < request.images().size(); i++) {
            post.addImage(request.images().get(i), i + 1L);
        }

        this.postRepository.save(post);

        // Publish event instead of direct calls
        eventPublisher.publishEvent(new PostCreatedEvent(
                post.getId(),
                member.getId(),
                book.getId(),
                post.getTitle(),
                post.getCreatedAt()
        ));

        return post.getId();
    }
}

// Event handlers in support/events/
@Component
@RequiredArgsConstructor
public class PostEventHandler {

    private final NotificationService notificationService;
    private final SearchService searchService;
    private final ActivityFeedService activityFeedService;

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        // Notify followers
        notificationService.notifyFollowers(event.authorId(), "New post created");

        // Index to search
        searchService.indexPost(event.postId(), event.title());

        // Add to activity feed
        activityFeedService.addActivity(event.authorId(), ActivityType.POST_CREATED, event.postId());
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        // Notify post author
        notificationService.notifyLike(event.postAuthorId(), event.likerId());

        // Update analytics
        analyticsService.recordLike(event.postId(), event.likerId());
    }
}
```

---

### 2.2 CommentCommandService Analysis

**Location**: `core/core-api/src/main/java/org/veri/be/domain/comment/service/CommentCommandService.java`

#### Current Code

```java
@Service
@RequiredArgsConstructor
public class CommentCommandService {
    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;
    private final PostQueryService postQueryService;

    @Transactional
    public Long postComment(CommentPostRequest request, Member member) {
        Post post = postQueryService.getPostById(request.postId());

        Comment comment = Comment.builder()
                .post(post)
                .author(member)
                .content(request.content())
                .build();

        post.addComment(comment);
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public Long postReply(Long parentCommentId, String content, Member member) {
        Comment parentComment = commentQueryService.getCommentById(parentCommentId);

        Comment reply = parentComment.replyBy(member, content);
        return commentRepository.save(reply).getId();
    }
}
```

#### Analysis

| Method | Current Side Effects | Potential Side Effects (Future) |
|--------|---------------------|--------------------------------|
| `postComment()` | Updates post's comment list | Post author notification |
| `postReply()` | Creates reply comment | Parent comment author notification |

**Verdict**: **Clean implementation**. Domain logic properly encapsulated.

**Event Opportunities**:

1. **CommentPostedEvent**:
   - Notify post author
   - Add to activity feed

2. **CommentRepliedEvent**:
   - Notify parent comment author
   - Add to activity feed

#### Event-Driven Implementation

```java
@Service
@RequiredArgsConstructor
public class CommentCommandService {
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long postComment(CommentPostRequest request, Member member) {
        Post post = postQueryService.getPostById(request.postId());

        Comment comment = Comment.builder()
                .post(post)
                .author(member)
                .content(request.content())
                .build();

        post.addComment(comment);
        Comment saved = commentRepository.save(comment);

        // Publish event
        eventPublisher.publishEvent(new CommentPostedEvent(
                saved.getId(),
                post.getId(),
                post.getAuthor().getId(),
                member.getId(),
                request.content()
        ));

        return saved.getId();
    }
}

@Component
@RequiredArgsConstructor
public class CommentEventHandler {

    private final NotificationService notificationService;
    private final ActivityFeedService activityFeedService;

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentPosted(CommentPostedEvent event) {
        // Notify post author (if not self-comment)
        if (!event.postAuthorId().equals(event.commenterId())) {
            notificationService.notifyComment(event.postAuthorId(), event.commenterId(), event.postId());
        }

        // Add to activity feed
        activityFeedService.addActivity(event.commenterId(), ActivityType.COMMENT_POSTED, event.commentId());
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentReplied(CommentRepliedEvent event) {
        // Notify parent comment author (if not self-reply)
        if (!event.parentAuthorId().equals(event.replierId())) {
            notificationService.notifyReply(event.parentAuthorId(), event.replierId(), event.commentId());
        }
    }
}
```

---

### 2.3 CardCommandService Analysis

**Location**: `core/core-api/src/main/java/org/veri/be/domain/card/service/CardCommandService.java`

#### Current Code

```java
@Service
@RequiredArgsConstructor
public class CardCommandService {
    private final CardRepository cardRepository;
    private final ReadingRepository readingRepository;
    private final StorageService storageService;

    @Transactional
    public Long createCard(Member member, String content, String imageUrl, Long memberBookId, Boolean isPublic) {
        Reading reading = readingRepository.findById(memberBookId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.INVALID_REQUEST));

        Card card = Card.builder()
                .member(member)
                .content(content)
                .image(imageUrl)
                .reading(reading)
                .isPublic(reading.isPublic() && isPublic)
                .build();

        cardRepository.save(card);
        return card.getId();
    }

    @Transactional
    public CardUpdateResponse updateCard(Member member, Long cardId, String content, String imageUrl) {
        Card card = this.getCard(cardId);
        Card updatedCard = card.updateContent(content, imageUrl, member);
        Card savedCard = cardRepository.save(updatedCard);

        return CardConverter.toCardUpdateResponse(savedCard);
    }
}
```

#### Analysis

| Method | Current Side Effects | Potential Side Effects (Future) |
|--------|---------------------|--------------------------------|
| `createCard()` | None | Follower notification, search indexing |
| `updateCard()` | None | Search index update |

**Verdict**: **Clean implementation**. No coupling issues.

**Event Opportunities**:

1. **CardCreatedEvent**:
   - Notify followers
   - Index to search service

2. **CardUpdatedEvent**:
   - Update search index

---

### 2.4 BookshelfService Analysis

**Location**: `core/core-api/src/main/java/org/veri/be/domain/book/service/BookshelfService.java`

#### Current Code

```java
@Service
@Transactional
@RequiredArgsConstructor
public class BookshelfService {
    private final ReadingRepository readingRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Reading addToBookshelf(Member member, Long bookId, boolean isPublic) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ApplicationException.of(BookErrorCode.BAD_REQUEST));

        // Check for duplicates
        Optional<Reading> findReading = readingRepository.findByMemberAndBook(member.getId(), bookId);
        if (findReading.isPresent()) {
            return findReading.get();
        }

        Reading reading = Reading.builder()
                .member(member)
                .book(book)
                .score(null)
                .startedAt(null)
                .endedAt(null)
                .status(NOT_START)
                .cards(new ArrayList<>())
                .isPublic(isPublic)
                .build();

        return readingRepository.save(reading);
    }

    @Transactional
    public void readStart(Member member, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.start(clock);
        readingRepository.save(reading);
    }

    @Transactional
    public void readOver(Member member, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.finish(clock);
        readingRepository.save(reading);
    }
}
```

#### Analysis

| Method | Current Side Effects | Potential Side Effects (Future) |
|--------|---------------------|--------------------------------|
| `addToBookshelf()` | Duplicate check | Badge award, recommendation update |
| `readStart()` | None | Reading streak tracking |
| `readOver()` | None | Achievement unlock, analytics |

**Event Opportunities**:

1. **ReadingStartedEvent**:
   - Update reading streak
   - Notify followers

2. **ReadingCompletedEvent**:
   - Award achievement badges
   - Update recommendations
   - Analytics tracking

---

## 3. Comparison: Veri-BE vs Ref Commerce

### 3.1 Coupling Analysis

| Aspect | Ref Commerce | Veri-BE |
|--------|--------------|---------|
| **Service Dependencies** | High (3-5 services/service) | Low (1-2 services/service) |
| **Side Effects** | Direct calls to coupon, point, inventory | None (clean) |
| **Urgency** | High (tight coupling) | Low (already decoupled) |

### 3.2 Ref Commerce Issues (Not Present in Veri-BE)

**PaymentService.success()** from ref:
```java
// Ref: Tight coupling
@Service
class PaymentService(
    private val pointHandler: PointHandler,           // 🔴 Coupled
    private val ownedCouponRepository: OwnedCouponRepository,  // 🔴 Coupled
) {
    fun success(...) {
        pointHandler.deduct(...)     // Direct call
        ownedCouponRepository.use()  // Direct call
    }
}
```

**Veri-BE**: No equivalent coupling found.

### 3.3 Event Priority Comparison

| Priority | Ref Commerce | Veri-BE |
|----------|--------------|---------|
| **1st (Critical)** | PaymentService.success() | None (already clean) |
| **2nd (High)** | CancelService.cancel() | None (already clean) |
| **3rd (Medium)** | ReviewService.addReview() | Notification features |
| **4th (Low)** | SettlementService | Search indexing |

**Conclusion**: Veri-BE does **not** need urgent event-driven refactoring. Events are valuable for **new features**, not fixing existing issues.

---

## 4. Recommended Event-Driven Features

### 4.1 Notification System (High Priority)

**Current State**: Not implemented

**Event-Driven Design**:

```
┌─────────────────────────────────────────────────────────────┐
│                     Domain Services                         │
│  (PostCommandService, CommentCommandService, etc.)          │
└────────────────────────────┬────────────────────────────────┘
                             │
                             │ Publish Events
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                  ApplicationEventPublisher                  │
└────────────────────────────┬────────────────────────────────┘
                             │
                             │ Async Event Bus
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              NotificationEventHandler                       │
│  - PostCreatedEvent → Notify followers                     │
│  - CommentPostedEvent → Notify post author                 │
│  - PostLikedEvent → Notify post author                     │
│  - CommentRepliedEvent → Notify parent author              │
└─────────────────────────────────────────────────────────────┘
```

**Events to Define**:
```java
// core-domain/post/event/PostCreatedEvent.java
public class PostCreatedEvent extends ApplicationEvent {
    private final Long postId;
    private final Long authorId;
    private final Long bookId;
    private final String title;
    private final LocalDateTime createdAt;
}

// core-domain/comment/event/CommentPostedEvent.java
public class CommentPostedEvent extends ApplicationEvent {
    private final Long commentId;
    private final Long postId;
    private final Long postAuthorId;
    private final Long commenterId;
    private final String content;
}

// core-domain/post/event/PostLikedEvent.java
public class PostLikedEvent extends ApplicationEvent {
    private final Long postId;
    private final Long postAuthorId;
    private final Long likerId;
}
```

**Implementation**:
```java
// support/events/notification/NotificationEventHandler.java
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        notificationService.notifyFollowers(
            event.authorId(),
            NotificationType.NEW_POST,
            "New post: " + event.title()
        );
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentPosted(CommentPostedEvent event) {
        if (!event.postAuthorId().equals(event.commenterId())) {
            notificationService.notifyUser(
                event.postAuthorId(),
                NotificationType.COMMENT,
                "New comment on your post"
            );
        }
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        if (!event.postAuthorId().equals(event.likerId())) {
            notificationService.notifyUser(
                event.postAuthorId(),
                NotificationType.LIKE,
                "Someone liked your post"
            );
        }
    }
}
```

### 4.2 Search Indexing (Medium Priority)

**Current State**: Not implemented

**Event-Driven Design**:

```java
// support/events/search/SearchIndexEventHandler.java
@Component
@RequiredArgsConstructor
public class SearchIndexEventHandler {

    private final SearchService searchService;

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        searchService.indexPost(event.postId());
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostUpdated(PostUpdatedEvent event) {
        searchService.updateIndex(event.postId());
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDeleted(PostDeletedEvent event) {
        searchService.removeFromIndex(event.postId());
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardCreated(CardCreatedEvent event) {
        searchService.indexCard(event.cardId());
    }
}
```

### 4.3 Activity Feed (Medium Priority)

**Current State**: Not implemented

**Event-Driven Design**:

```java
// support/events/activity/ActivityFeedEventHandler.java
@Component
@RequiredArgsConstructor
public class ActivityFeedEventHandler {

    private final ActivityFeedService activityFeedService;

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        activityFeedService.addActivity(
            event.authorId(),
            ActivityType.POST_CREATED,
            event.postId(),
            event.createdAt()
        );
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentPosted(CommentPostedEvent event) {
        activityFeedService.addActivity(
            event.commenterId(),
            ActivityType.COMMENT_POSTED,
            event.commentId(),
            LocalDateTime.now()
        );
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReadingCompleted(ReadingCompletedEvent event) {
        activityFeedService.addActivity(
            event.memberId(),
            ActivityType.BOOK_READ,
            event.readingId(),
            event.completedAt()
        );
    }
}
```

### 4.4 Analytics & Gamification (Low Priority)

**Current State**: Not implemented

**Event-Driven Design**:

```java
// support/events/analytics/AnalyticsEventHandler.java
@Component
@RequiredArgsConstructor
public class AnalyticsEventHandler {

    private final AnalyticsService analyticsService;
    private final BadgeService badgeService;

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        analyticsService.recordLike(event.postId(), event.likerId());
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReadingCompleted(ReadingCompletedEvent event) {
        analyticsService.recordBookCompleted(event.memberId(), event.bookId());

        // Check for badge awards
        long completedCount = analyticsService.getCompletedBooksCount(event.memberId());
        if (completedCount == 10) {
            badgeService.awardBadge(event.memberId(), BadgeType.BOOKWORM);
        }
    }
}
```

---

## 5. Implementation Roadmap

### Phase 1: Event Infrastructure (1-2 days)

**Tasks**:
- [ ] Create `support/events` module
- [ ] Configure `@EnableAsync`
- [ ] Create `AsyncConfig` for thread pool
- [ ] Define base event classes

**build.gradle.kts**:
```kotlin
// support/events/build.gradle.kts
dependencies {
    implementation(project(":core:core-domain"))
    implementation("org.springframework.boot:spring-boot-starter")

    // For @Async
    implementation("org.springframework.boot:spring-boot-starter-aop")
}
```

**Configuration**:
```java
// support/events/config/AsyncConfig.java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "eventTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-async-");
        executor.initialize();
        return executor;
    }
}
```

### Phase 2: Notification Events (2-3 days)

**Tasks**:
- [ ] Define notification events (PostCreated, CommentPosted, PostLiked)
- [ ] Implement `NotificationEventHandler`
- [ ] Create `NotificationService` interface
- [ ] Add notification storage (DB, Redis, or push notification service)
- [ ] Write integration tests

### Phase 3: Search Indexing (1-2 days)

**Tasks**:
- [ ] Define search events (PostCreated, PostUpdated, CardCreated)
- [ ] Implement `SearchIndexEventHandler`
- [ ] Integrate with search service (Elasticsearch, OpenSearch, or client-search)
- [ ] Add re-indexing endpoint
- [ ] Write integration tests

### Phase 4: Activity Feed (2-3 days)

**Tasks**:
- [ ] Define activity events
- [ ] Implement `ActivityFeedEventHandler`
- [ ] Create activity storage (Redis or DB)
- [ ] Add activity feed query API
- [ ] Write integration tests

### Phase 5: Analytics & Gamification (2-3 days)

**Tasks**:
- [ ] Define analytics events
- [ ] Implement `AnalyticsEventHandler`
- [ ] Create badge/achievement system
- [ ] Add analytics dashboard
- [ ] Write integration tests

**Total Duration**: 8-13 days

---

## 6. Architecture Impact

### 6.1 Before Events

```
PostCommandService.createPost()
    → PostRepository.save()
    → (end)
```

### 6.2 After Events

```
PostCommandService.createPost()
    → PostRepository.save()
    → ApplicationEventPublisher.publishEvent(PostCreatedEvent)
        → NotificationEventHandler (async)
        → SearchIndexEventHandler (async)
        → ActivityFeedEventHandler (async)
        → AnalyticsEventHandler (async)
```

### 6.3 Benefits

| Aspect | Before | After |
|--------|--------|-------|
| **Responsiveness** | Blocking side effects | Async processing |
| **Extensibility** | Modify service for new features | Add new event handlers |
| **Testability** | Mock all side effects | Test event publishing only |
| **Failure Isolation** | One failure breaks all | Independent handlers |
| **Scalability** | Monolithic logic | Distributed handlers |

---

## 7. Risks and Mitigations

### 7.1 Event Ordering

**Risk**: Events processed out of order

**Mitigation**:
```java
// For sequential processing, use synchronous handler
@EventListener  // Remove @Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleCriticalEvent(CriticalEvent event) {
    // Process in order
}
```

### 7.2 Event Loss

**Risk**: Event handler fails before processing

**Mitigation**:
- Use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` for transactional safety
- Implement Dead Letter Queue for failed events
- Add retry logic with Spring Retry

```java
@RetryableTopic(attempts = "3")
@EventListener
@Async
public void handleWithRetry(PostCreatedEvent event) {
    // Auto-retry on failure
}
```

### 7.3 Debugging Complexity

**Risk**: Hard to trace async event flows

**Mitigation**:
- Add correlation IDs (MDC)
- Use distributed tracing (Spring Cloud Sleuth)
- Log all events with detailed context

```java
@EventListener
@Async
public void handleWithLogging(PostCreatedEvent event) {
    MDC.put("eventId", UUID.randomUUID().toString());
    MDC.put("postId", event.postId().toString());
    log.info("Processing PostCreatedEvent");

    try {
        // Process event
    } finally {
        MDC.clear();
    }
}
```

---

## 8. Conclusion

### 8.1 Key Findings

1. **No Urgent Refactoring Needed**: Veri-BE has clean service separation with low coupling
2. **Events for New Features**: Event-driven architecture valuable for notifications, search, analytics
3. **Incremental Adoption**: Can add events incrementally as new features are developed

### 8.2 Recommendation

**Adopt event-driven architecture for new features**, not for refactoring existing code.

**Priority Order**:
1. **Notifications** (High business value)
2. **Search Indexing** (User experience improvement)
3. **Activity Feeds** (Engagement feature)
4. **Analytics** (Business intelligence)

### 8.3 Success Metrics

| Metric | Target |
|--------|--------|
| **Notification Delivery Rate** | > 99% |
| **Search Index Latency** | < 1 second |
| **Event Processing Time** | < 100ms (p95) |
| **Event Handler Failure Rate** | < 0.1% |

---

## 9. Next Steps

1. **Confirm Requirements**
   - Prioritize notification vs search vs activity feed
   - Define notification channels (push, email, in-app)

2. **Prototype One Feature**
   - Implement notification for PostCreated
   - Validate event flow
   - Measure performance

3. **Scale to Other Features**
   - Apply pattern to other domains
   - Add more event handlers
   - Monitor and optimize

---

## References

- **./ref/docs/event-refactoring-analysis.md** - Reference event refactoring patterns
- **Spring ApplicationEventPublisher** - Native event mechanism
- **@TransactionalEventListener** - Transaction-bound event processing
