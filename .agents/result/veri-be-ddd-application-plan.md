# Veri-BE DDD Implementation Plan (Revised)

**Status**: Draft (Revised based on feedback)
**Date**: 2025-12-31
**Source**: ./ref structure analysis, DDD principles, and practical considerations

---

## 1. Current Veri-BE Structure Analysis

### 1.1 Module Structure

```
Veri-BE/
├── core/
│   ├── core-enum          # Shared domain types
│   ├── core-api           # Controller + Service + DTO (mixed concerns)
│   └── core-app           # Application bootstrap
├── storage/
│   └── db-core            # Persistence layer (Entity, Repository)
├── clients/
│   ├── client-aws         # AWS S3 integration
│   ├── client-ocr         # OCR service integration
│   └── client-search      # Search service integration
├── support/
│   ├── common             # Shared utilities
│   ├── logging            # Logging infrastructure
│   └── monitoring         # Monitoring infrastructure
└── tests/                 # Test module
```

### 1.2 Domain Boundaries

Current domains in `core/core-api/src/main/java/org/veri/be/domain/`:
- **auth**: Authentication and authorization
- **book**: Book and bookshelf management
- **card**: Card-based content creation
- **comment**: Comment management
- **image**: Image upload and OCR processing
- **member**: Member management
- **post**: Social posting

### 1.3 Service Pattern

Already using **CQS (Command Query Separation)**:
- `CardCommandService` / `CardQueryService`
- `CommentCommandService` / `CommentQueryService`
- `MemberCommandService` / `MemberQueryService`
- `PostCommandService` / `PostQueryService`

---

## 2. Ref Structure Principles Analysis

### 2.1 Core Design Principles

**Reference**: `./ref/docs/ddd-analysis-and-plan.md`

#### 2.1.1 Layer Separation

```
core-api (Presentation) → db-core (Persistence) → support (Infrastructure)
```

**Key Characteristic**: Gradle modules provide **physical boundaries** between layers.

#### 2.1.2 Dependency Direction

```
                ┌─────────────┐
                │  support/*  │
                └──────┬──────┘
                       │
               ┌───────┴───────┐
               │               │
         ┌─────▼─────┐   ┌────▼────┐
         │  db-core  │   │core-enum│
         └─────┬─────┘   └─────────┘
               │
         ┌─────▼─────┐
         │ core-api  │
         └───────────┘
```

### 2.2 Advantages

**Physical Boundaries**:
- Gradle module dependencies enforce compile-time checks
- Prevents circular dependencies
- Explicit dependency declaration in `build.gradle.kts`

**Persistence Independence**:
- `db-core` encapsulates JPA/Hibernate dependencies
- Domain layer can be decoupled from persistence technology

**Infrastructure Modularization**:
- `logging`, `monitoring` as independent modules
- Easy to replace/extend

### 2.3 Disadvantages

**Anemic Domain Model Risk**:
- Business logic concentrated in Service layer
- Domain objects as data containers

**Missing DDD Patterns**:
- No explicit Aggregate Root
- No Domain Event mechanism
- No Bounded Context boundaries

**Dependency Inversion Violation**:
- Domain depends on persistence (Clean Architecture violation)

---

## 3. Veri-BE Specific Analysis

### 3.1 Current Issues

| Issue | Description | Impact |
|-------|-------------|--------|
| **Mixed Layer** | `core-api` contains Controller, Service, DTO | High coupling, hard to test |
| **No Domain Module** | Pure domain logic mixed with infrastructure | Anemic Domain Model |
| **Direct Entity Usage** | Services use JPA entities directly | Persistence leakage |
| **No Domain Events** | Side effects handled directly | Potential future coupling |

### 3.2 Existing Strengths

| Strength | Description |
|----------|-------------|
| **CQS Pattern** | Command/Query service separation already implemented |
| **Domain Organization** | Domains already separated by package |
| **Client Modularization** | External integrations isolated in `clients/` |
| **Low Coupling** | Services already have minimal cross-dependencies |

---

## 4. DDD Implementation Plan for Veri-BE (Revised)

### 4.1 Target Module Structure

```
Veri-BE/
├── core/
│   ├── core-enum              # Shared domain types (existing)
│   ├── core-domain/           # NEW: Pure domain logic
│   │   ├── auth/              # Authentication domain
│   │   ├── book/              # Book & bookshelf domain
│   │   ├── card/              # Card content domain
│   │   ├── comment/           # Comment domain
│   │   ├── image/             # Image domain
│   │   ├── member/            # Member domain
│   │   ├── post/              # Social post domain
│   │   └── shared/            # Shared domain concepts (events, errors)
│   ├── core-application/      # NEW: Application services
│   │   ├── auth/              # Auth use cases
│   │   ├── book/              # Book use cases
│   │   ├── card/              # Card use cases
│   │   ├── comment/           # Comment use cases
│   │   ├── image/             # Image use cases
│   │   ├── member/            # Member use cases
│   │   ├── post/              # Post use cases
│   │   └── shared/            # Shared application logic
│   ├── core-api/              # Controllers (existing, refactored)
│   └── core-app/              # Application bootstrap (existing)
├── storage/
│   ├── db-core/               # JPA persistence (existing)
│   │   └── adapter/           # NEW: Repository implementations
│   └── db-redis/              # NEW: Redis cache (optional)
├── clients/                   # External integrations (existing)
│   ├── client-aws/
│   ├── client-ocr/
│   ├── client-search/
│   └── adapter/               # NEW: Client adapters implementing domain ports
├── support/
│   ├── common/                # Shared utilities (existing)
│   ├── logging/               # Logging (existing)
│   ├── monitoring/            # Monitoring (existing)
│   └── events/                # NEW: Domain event handlers & Spring bridge
└── tests/                     # Test module (existing)
```

### 4.2 Revised Dependency Rules (Hexagonal Architecture)

```
┌──────────────────────────────────────────────────────────────┐
│                         core-api                             │
│                    (Controllers only)                        │
└─────────────────────────────┬────────────────────────────────┘
                              │
┌─────────────────────────────▼────────────────────────────────┐
│                    core-application                          │
│              (Application Services, DTOs)                    │
│  - Uses core-domain (models, ports)                          │
│  - Uses storage/clients via interfaces only                  │
└─────────────────────────────┬────────────────────────────────┘
                              │
┌─────────────────────────────▼────────────────────────────────┐
│                      core-domain                              │
│  - Rich domain models (NO Spring, NO infrastructure)         │
│  - Port interfaces (Repository, Client interfaces)           │
│  - Pure domain events (POJOs, NO ApplicationEvent)           │
└─────────────────────────────┬────────────────────────────────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
┌───────────▼───────┐  ┌──────▼──────────┐  ┌──▼──────────────┐
│  storage/db-core  │  │   clients/*     │  │  support/events │
│  (implements     │  │  (implements    │  │  (Spring bridge)│
│   Repository)    │  │   Client ports) │  └─────────────────┘
└──────────────────┘  └─────────────────┘
```

**Key Principles**:
- **Domain Purity**: `core-domain` has ZERO dependencies on Spring, JPA, or infrastructure
- **Dependency Inversion**: Domain defines interfaces (ports), infrastructure implements them (adapters)
- **Event Separation**: Domain events are plain POJOs; Spring event conversion happens in `support/events`

### 4.3 Per-Domain Structure (Example: Card)

```
core-domain/card/
├── model/
│   ├── Card.java              # Aggregate Root (rich behavior)
│   ├── CardDeck.java          # Entity
│   ├── CardCategory.java      # Value Object (enum in core-enum)
│   ├── CardId.java            # Identifier VO
│   ├── CardContent.java       # Value Object
│   └── CardVisibility.java    # Value Object (enum in core-enum)
├── port/
│   ├── CardRepository.java    # Repository port (interface)
│   └── CardImageStorage.java  # Storage port (interface)
├── service/
│   └── CardDomainService.java # Domain service (complex logic)
├── event/
│   ├── CardCreated.java       # Domain event (POJO, NO Spring)
│   ├── CardUpdated.java       # Domain event (POJO)
│   └── CardDeleted.java       # Domain event (POJO)
└── exception/
    └── CardDomainException.java

core-application/card/
├── service/
│   ├── CardCommandService.java # Command (state change)
│   └── CardQueryService.java   # Query (read)
├── dto/
│   ├── CreateCardCommand.java
│   ├── UpdateCardCommand.java
│   └── CardQueryResult.java
└── facade/
    └── CardOrchestrationService.java # Complex use cases

storage/db-core/adapter/card/
├── entity/
│   ├── CardEntity.java         # JPA Entity
│   └── CardDeckEntity.java     # JPA Entity
├── persistence/
│   └── CardRepositoryImpl.java # JPA Repository implements CardRepository port
├── mapper/
│   └── CardMapper.java         # Entity ↔ Domain model mapper
└── repository/
    └── CardJpaRepository.java  # Spring Data JPA

clients/client-aws/adapter/
└── S3CardImageStorageAdapter.java  # Implements CardImageStorage port

support/events/card/
├── SpringCardEventPublisher.java   # Converts domain events → Spring events
└── CardEventHandler.java           # @TransactionalEventListener handlers

core-api/card/
└── controller/
    ├── CardCommandController.java
    └── CardQueryController.java
```

---

## 5. Critical Design Decisions (Revised)

### 5.1 Domain Events: Pure POJOs

**Issue**: If domain events extend `ApplicationEvent`, core-domain depends on Spring.

**Solution**: Domain events are plain POJOs/records; Spring bridge handles conversion.

**Example**:
```java
// core-domain/card/event/CardCreated.java (Pure POJO, NO Spring)
package org.veri.be.domain.card.event;

public record CardCreated(
    CardId cardId,
    MemberId authorId,
    ReadingId readingId,
    CardContent content,
    LocalDateTime createdAt
) {
    // Pure domain event - no Spring dependency
}
```

**Spring Bridge**:
```java
// support/events/card/SpringCardEventPublisher.java
@Component
@RequiredArgsConstructor
public class SpringCardEventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    // Application service calls this to publish domain events
    public void publishCardCreated(CardCreated domainEvent) {
        // Convert to Spring event and publish
        springEventPublisher.publishEvent(
            new CardCreatedSpringEvent(domainEvent)
        );
    }
}

// Spring event wrapper (lives in support/events)
public class CardCreatedSpringEvent extends ApplicationEvent {
    private final CardCreated domainEvent;

    public CardCreatedSpringEvent(CardCreated domainEvent) {
        super(domainEvent);
        this.domainEvent = domainEvent;
    }

    public CardCreated domainEvent() {
        return domainEvent;
    }
}
```

**Application Service Usage**:
```java
// core-application/card/service/CardCommandService.java
@Service
@RequiredArgsConstructor
public class CardCommandService {

    private final CardRepository cardRepository;
    private final SpringCardEventPublisher eventPublisher;

    @Transactional
    public CardId createCard(CreateCardCommand command) {
        Card card = Card.create(command);
        cardRepository.save(card);

        // Publish pure domain event (no Spring dependency in domain)
        eventPublisher.publishCardCreated(
            new CardCreated(card.getId(), card.getAuthorId(), ...)
        );

        return card.getId();
    }
}
```

**Event Handler**:
```java
// support/events/card/CardEventHandler.java
@Component
@RequiredArgsConstructor
public class CardEventHandler {

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardCreated(CardCreatedSpringEvent springEvent) {
        CardCreated event = springEvent.domainEvent();

        // Handle event (notification, indexing, etc.)
        notificationService.notifyFollowers(event.authorId());
        searchService.indexCard(event.cardId());
    }
}
```

### 5.2 Entity-Domain Mapping: Strategy B (Mapper)

**Issue**: Strategy C (Entity implements Domain interface) causes practical problems:
- Lazy loading/proxy issues
- Complex equals/hashCode
- Immutable VO conversion difficulties
- Collection change detection conflicts
- Domain model becomes "read-only interface" (anemic risk)

**Solution**: Use **Strategy B (Separate Models + Mapper)** with **aggregate-level save/load**.

**Example**:
```java
// core-domain/card/model/Card.java (Pure domain, NO JPA annotations)
package org.veri.be.domain.card.model;

public class Card extends AggregateRoot {

    private final CardId id;
    private MemberId authorId;
    private CardContent content;
    private ReadingId readingId;
    private CardVisibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor
    private Card(CardId id, MemberId authorId, CardContent content, ReadingId readingId) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.readingId = readingId;
        this.visibility = CardVisibility.PRIVATE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Factory method
    public static Card create(MemberId authorId, CardContent content, ReadingId readingId) {
        Card card = new Card(CardId.generate(), authorId, content, readingId);
        card.registerEvent(new CardCreated(card.id, authorId, readingId, content, card.createdAt));
        return card;
    }

    // Domain behavior
    public void updateContent(CardContent newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
        this.registerEvent(new CardUpdated(this.id, newContent, this.updatedAt));
    }

    public void changeVisibility(CardVisibility newVisibility) {
        this.visibility = newVisibility;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters (no setters for invariants)
    public CardId getId() { return id; }
    public MemberId getAuthorId() { return authorId; }
    public CardContent getContent() { return content; }
    // ...
}

// storage/db-core/adapter/card/entity/CardEntity.java (JPA Entity)
@Entity
@Table(name = "cards")
public class CardEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "reading_id", nullable = false)
    private Long readingId;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA only - getters/setters for persistence
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ...
}

// storage/db-core/adapter/card/mapper/CardMapper.java (Mapper)
@Component
public class CardMapper {

    public Card toDomain(CardEntity entity) {
        return Card.restore(
            CardId.of(entity.getId()),
            MemberId.of(entity.getMemberId()),
            CardContent.of(entity.getContent()),
            ReadingId.of(entity.getReadingId()),
            entity.isPublic() ? CardVisibility.PUBLIC : CardVisibility.PRIVATE,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public CardEntity toEntity(Card domain) {
        CardEntity entity = new CardEntity();
        entity.setId(domain.getId().value());
        entity.setMemberId(domain.getAuthorId().value());
        entity.setContent(domain.getContent().value());
        entity.setReadingId(domain.getReadingId().value());
        entity.setPublic(domain.getVisibility() == CardVisibility.PUBLIC);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}

// storage/db-core/adapter/card/persistence/CardRepositoryImpl.java
@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

    private final CardJpaRepository jpaRepository;
    private final CardMapper mapper;

    @Override
    public Optional<Card> findById(CardId id) {
        return jpaRepository.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public Card save(Card card) {
        CardEntity entity = mapper.toEntity(card);
        CardEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(CardId id) {
        jpaRepository.deleteById(id.value());
    }
}
```

**Key Principles**:
- **Aggregate-level save/load**: Repository saves entire aggregate at once
- **Separate models**: Domain model and JPA entity are independent
- **Mapper encapsulation**: Mapping logic isolated in mapper component
- **Query optimization**: Use CQS - complex queries use separate read models (projections)

### 5.3 Value Objects: Gradual Introduction

**Issue**: VO introduction (e.g., `CardId`, `CardContent`) requires JPA mapping (Converter/Embeddable). "No schema change" constraint conflicts with rich domain.

**Solution**: **Gradual migration path**:
1. **Phase 1**: Use VOs in domain only; persistence uses primitive types
2. **Phase 2**: Introduce JPA AttributeConverters for VOs
3. **Phase 3**: Optimize queries with native types where performance matters

**Example**:
```java
// core-domain/card/model/CardId.java (Value Object)
public record CardId(Long value) {
    public CardId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid CardId");
        }
    }

    public static CardId of(Long value) {
        return new CardId(value);
    }

    public static CardId generate() {
        return new CardId(IdGenerator.generate());  // Domain ID generation
    }
}

// Phase 1: Repository converts to/from primitives
// storage/db-core/adapter/card/persistence/CardRepositoryImpl.java
@Override
public Card save(Card card) {
    // Domain uses CardId, persistence uses Long
    CardEntity entity = new CardEntity();
    entity.setId(card.getId().value());  // Convert to primitive
    // ...
}

// Phase 2: Add JPA AttributeConverter (optional, when comfortable)
@Converter(autoApply = true)
public class CardIdConverter implements AttributeConverter<CardId, Long> {

    @Override
    public Long convertToDatabaseColumn(CardId attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public CardId convertToEntityAttribute(Long dbData) {
        return dbData != null ? CardId.of(dbData) : null;
    }
}

// After Phase 2, Entity can use VO directly:
@Entity
public class CardEntity {
    @Convert(converter = CardIdConverter.class)
    private CardId id;  // Now uses VO directly
}
```

**Migration Strategy**:
- **Domain first**: Introduce VOs in domain models immediately
- **Persistence gradual**: Keep JPA using primitives initially
- **Query optimization**: For complex queries, use primitive-based read models (CQS)

### 5.4 Dependency Rules: Port/Adapter Pattern

**Issue**: If `core-application` depends directly on `storage` and `clients`, it becomes a "God layer" again.

**Solution**: **Application depends on interfaces (ports) only**; Infrastructure implements ports (adapters).

**Port Interfaces in Domain**:
```java
// core-domain/card/port/CardRepository.java (Port)
package org.veri.be.domain.card.port;

import java.util.Optional;

public interface CardRepository {
    Optional<Card> findById(CardId id);
    List<Card> findByAuthorId(MemberId authorId);
    Card save(Card card);
    void delete(CardId id);
}

// core-domain/card/port/CardImageStorage.java (Port)
package org.veri.be.domain.card.port;

public interface CardImageStorage {
    String generatePresignedUrl(String contentType, long contentLength);
    String upload(String key, byte[] data);
    void delete(String key);
}
```

**Adapter Implementations**:
```java
// storage/db-core/adapter/card/persistence/CardRepositoryImpl.java (Adapter)
@Repository
public class CardRepositoryImpl implements CardRepository {
    // JPA implementation
}

// clients/client-aws/adapter/S3CardImageStorageAdapter.java (Adapter)
@Component
public class S3CardImageStorageAdapter implements CardImageStorage {
    private final S3Client s3Client;

    @Override
    public String generatePresignedUrl(String contentType, long contentLength) {
        // S3-specific implementation
    }
}
```

**Application Service**:
```java
// core-application/card/service/CardCommandService.java
@Service
@RequiredArgsConstructor
public class CardCommandService {

    // Depends on ports (interfaces), not concrete implementations
    private final CardRepository cardRepository;
    private final CardImageStorage imageStorage;

    @Transactional
    public CardId createCardWithImage(CreateCardCommand command) {
        // Use storage port
        String imageUrl = imageStorage.generatePresignedUrl(
            command.contentType(),
            command.contentLength()
        );

        Card card = Card.create(command.authorId(), command.content(), ...);
        cardRepository.save(card);

        return card.getId();
    }
}
```

**Configuration**:
```java
// core-app/config/DomainConfig.java
@Configuration
public class DomainConfig {

    // All adapters automatically discovered via @Repository/@Component
    // Application services auto-wired to implementations
}
```

**Benefits**:
- **Clean dependency**: Application → Domain Ports ← Infrastructure Adapters
- **Testability**: Mock ports easily in tests
- **Swappability**: Replace implementations without touching domain/application

---

## 6. Implementation Phases (Revised)

### Phase 1: Prototype One Domain (Foundation)

**Objective**: Complete vertical slice for **comment** domain (simplest)

**Duration**: 5-7 days

#### 6.1.1 Domain Model Setup

**Tasks**:
- [ ] Create `core/core-domain/build.gradle.kts` (NO Spring dependencies)
- [ ] Create `core-domain/comment/` package structure
- [ ] Implement `Comment` aggregate root with rich behavior
- [ ] Implement `CommentId`, `CommentContent` VOs
- [ ] Define `CommentRepository` port interface
- [ ] Create domain events (pure POJOs): `CommentPosted`, `CommentReplied`

**build.gradle.kts**:
```kotlin
// core/core-domain/build.gradle.kts
plugins {
    java
    kotlin("jvm")
}

dependencies {
    api(project(":core:core-enum"))

    // Pure domain: NO Spring, NO JPA
    implementation("jakarta.validation:jakarta.validation-api:3.0.0")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
}
```

#### 6.1.2 Persistence Adapter

**Tasks**:
- [ ] Create `storage/db-core/adapter/comment/` package
- [ ] Implement `CommentEntity` (JPA)
- [ ] Implement `CommentMapper` (Entity ↔ Domain)
- [ ] Implement `CommentRepositoryImpl` (implements port)

#### 6.1.3 Application Service

**Tasks**:
- [ ] Create `core/core-application/build.gradle.kts`
- [ ] Create `core-application/comment/service/CommentCommandService`
- [ ] Create DTOs: `PostCommentCommand`, `CommentQueryResult`
- [ ] Use domain model (not JPA entities)

#### 6.1.4 API Controller

**Tasks**:
- [ ] Refactor `core-api/comment/controller/CommentController`
- [ ] Delegate to `CommentCommandService`
- [ ] Remove business logic (keep HTTP concerns only)

#### 6.1.5 Testing

**Tasks**:
- [ ] Unit tests for domain model (`CommentTest`)
- [ ] Unit tests for application service (mock repository)
- [ ] Integration test with `@SpringBootTest`
- [ ] Verify CQS compliance

**Deliverable**: Fully working comment domain with DDD structure, all tests passing.

### Phase 2: Validate and Refine Patterns

**Objective**: Learn from prototype, adjust patterns

**Duration**: 2-3 days

**Tasks**:
- [ ] Review mapper complexity
- [ ] Adjust VO usage based on friction points
- [ ] Document pattern decisions in `.agents/context/`
- [ ] Update DDD plan with learnings

### Phase 3: Introduce Domain Events (After 1 Domain Complete)

**Objective**: Add event-driven architecture for **comment** domain only

**Duration**: 2-3 days

**Tasks**:
- [ ] Create `support/events` module
- [ ] Configure `@EnableAsync`
- [ ] Implement `SpringCommentEventPublisher` (domain → Spring bridge)
- [ ] Create event handlers (notification, activity feed)
- [ ] Add `@TransactionalEventListener(phase = AFTER_COMMIT)`
- [ ] Test async event processing

**Deliverable**: Comment domain emits and handles events correctly.

### Phase 4: Migrate Remaining Domains

**Objective**: Apply validated patterns to other domains

**Duration**: 14-21 days

**Migration Order** (simplest to most complex):
1. **member** (5-7 days)
2. **card** (5-7 days)
3. **post** (7-10 days)
4. **book** (7-10 days)
5. **auth** (3-5 days)
6. **image** (5-7 days)

**Per-Domain Checklist**:
- [ ] Domain model (aggregates, VOs, ports)
- [ ] Persistence adapter (entity, mapper, repository impl)
- [ ] Application service (command/query)
- [ ] API controller refactoring
- [ ] Tests (unit + integration)
- [ ] Event handlers (if needed)

### Phase 5: Cross-Cutting Concerns

**Objective**: Add shared infrastructure

**Duration**: 3-5 days

**Tasks**:
- [ ] Implement notification system (if adding feature)
- [ ] Implement search indexing (if adding feature)
- [ ] Implement activity feed (if adding feature)
- [ ] Add ArchUnit tests for dependency rules
- [ ] Performance testing (mapper overhead)

---

## 7. Risks and Mitigations (Revised)

### 7.1 Domain Purity vs Spring Features

**Risk**: Temptation to add Spring dependencies to domain for convenience (DI, transactions, events).

**Impact**: Domain becomes tied to Spring, loses portability, testability suffers.

**Mitigation**:
- **Enforce by build**: `core-domain/build.gradle.kts` explicitly excludes Spring
- **ArchUnit test**: Prevent Spring package imports in domain
- **Code review**: Manual review of all `core-domain` changes

**ArchUnit Test**:
```java
@AnalyzeClasses(packages = "org.veri.be.domain")
class DomainPurityTest {

    @ArchTest
    final ArchRule domain_should_not_depend_on_spring =
        noClasses()
            .that().resideInAPackage("..core.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "org.hibernate.."
            );
}
```

### 7.2 JPA Entity vs Rich Domain Lifecycle

**Risk**: JPA entity lifecycle (proxy, lazy loading, dirty checking) conflicts with rich domain model behavior.

**Impact**:
- Domain behavior bypassed when JPA mutates state
- LazyLoadingException when domain methods access associations
- Inconsistent state between domain and entity

**Mitigation**:
- **Strategy B**: Separate models prevent lifecycle conflicts
- **Aggregate boundaries**: Only aggregate root can hold associations
- **Explicit save**: Repository.save() called explicitly (no auto-commit)
- **Test with real DB**: Integration tests catch JPA-specific issues

**Example**:
```java
// Good: Explicit domain behavior
card.updateContent(newContent);  // Domain validates
cardRepository.save(card);       // Explicit persistence

// Bad: JPA mutates without domain logic
entity.setContent("new");  // Bypasses domain validation
entity.setUpdatedAt(now);  // JPA dirty checking
repository.save(entity);   // Auto-commits invalid state
```

### 7.3 Application Layer Becomes "God Layer" Again

**Risk**: If `core-application` depends on too many infrastructure modules, it becomes a new bottleneck.

**Impact**:
- Services accumulate business logic
- Hard to test (many dependencies)
- Defeats purpose of DDD layering

**Mitigation**:
- **Port/Adapter rule**: Application depends only on interfaces (ports)
- **Service limit**: Max 5-6 dependencies per application service
- **Facade pattern**: Complex orchestration in dedicated facades, not command services
- **Regular refactoring**: Extract new domain services as complexity grows

**Example**:
```java
// Good: Application depends on ports
@Service
class CardCommandService {
    private final CardRepository cardRepo;       // Port
    private final MemberRepository memberRepo;   // Port
    private final CardImageStorage storage;      // Port
    // Max 3 dependencies, all interfaces
}

// Bad: Application depends on infrastructure
@Service
class CardCommandService {
    private final CardRepositoryImpl cardRepo;         // Impl
    private final S3Client s3Client;                   // AWS SDK
    private final EntityManager em;                     // JPA
    private final EventPublisher publisher;             // Spring
    // Too many concrete dependencies
}
```

### 7.4 Mapper Performance Overhead

**Risk**: Strategy B (mapper) adds CPU/memory overhead for copying data.

**Impact**:
- Performance degradation vs single-model approach
- Complex mappers become unmaintainable

**Mitigation**:
- **Aggregate-level save**: Save entire aggregate once (not entity-by-entity)
- **CQS for queries**: Use read models/projections for complex queries (skip domain)
- **Mapper utilities**: Use MapStruct or code generation
- **Performance test**: Measure overhead before/after

**CQS Example**:
```java
// Command: Use rich domain model (pays mapper cost)
Card card = cardRepository.findById(cardId).get();
card.updateContent(newContent);
cardRepository.save(card);

// Query: Use read model directly (no mapper)
@Query("SELECT new org.veri.be.application.dto.CardSummaryDTO(c.id, m.nickname, c.content) FROM CardEntity c JOIN MemberEntity m ON c.memberId = m.id WHERE c.id = :id")
CardSummaryDTO findSummaryById(Long id);
```

### 7.5 Gradual Migration Complexity

**Risk**: Old (core-api) and new (core-application) code coexist, causing confusion.

**Impact**:
- Developers don't know which service to call
- Duplicate logic across old and new
- Migration never completes

**Mitigation**:
- **Explicit rule**: "All new features use new structure"
- **Per-domain cutover**: Fully migrate one domain before starting next
- **Deprecation warnings**: Mark old services as `@Deprecated`
- **Delete old code**: Remove old implementation immediately after cutover

**Migration Rule Example**:
```java
// Old service (deprecated)
@Deprecated(forRemoval = true)
@Service
class OldCommentService {
    // Marked for deletion after migration
}

// New service (use this)
@Service
class CommentCommandService {
    // New implementation
}
```

---

## 8. Verification Methods

### 8.1 ArchUnit Tests

```java
@AnalyzeClasses(packages = "org.veri.be")
class DDDArchTest {

    @ArchTest
    final ArchRule domain_purity =
        noClasses()
            .that().resideInAPackage("..core.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

    @ArchTest
    final ArchRule application_depends_on_ports_only =
        classes()
            .that().resideInAPackage("..core.application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..core.domain..",
                "..core.enum..",
                "java..",
                "org.springframework.stereotype..",
                "org.springframework.transaction.."
            );

    @ArchTest
    final ArchRule controllers_delegates_only =
        classes()
            .that().resideInAPackage("..core.api..controller..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..core.application..",
                "org.springframework.web..",
                "org.springframework.validation..",
                "jakarta.validation.."
            );
}
```

### 8.2 Quality Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Domain Logic Ratio** | > 40% | Domain LOC / Total LOC |
| **Circular Dependencies** | 0 | Gradle `dependencyInsight` |
| **Domain Test Coverage** | > 80% | JaCoCo (core-domain only) |
| **Service Dependencies** | < 5 deps/service | Code analysis |
| **Mapper Complexity** | < 50 lines/mapper | Code analysis |

---

## 9. Expected Benefits

### 9.1 Technical Benefits

| Benefit | Description |
|---------|-------------|
| **Rich Domain Model** | Business logic in domain entities |
| **Testability** | Domain logic testable without infrastructure |
| **Maintainability** | Clear separation of concerns |
| **Extensibility** | New features via event handlers |
| **Portability** | Domain logic independent of Spring/JPA |

### 9.2 Business Benefits

| Benefit | Description |
|---------|-------------|
| **Faster Feature Development** | Isolated domain changes |
| **Easier Onboarding** | Clear module boundaries |
| **Better Collaboration** | Teams work on independent domains |
| **Quality** | Enforced purity through architecture |

---

## 10. Timeline Estimate (Revised)

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| **Phase 1**: Prototype (comment) | 5-7 days | Working DDD domain |
| **Phase 2**: Validate Patterns | 2-3 days | Refined patterns |
| **Phase 3**: Events (comment only) | 2-3 days | Event infrastructure |
| **Phase 4**: Migrate 6 domains | 14-21 days | All domains migrated |
| **Phase 5**: Cross-cutting | 3-5 days | Infrastructure, ArchUnit |

**Total**: 26-39 days (approx. 5-8 weeks)

---

## 11. Next Steps

1. **Review and Approval**
   - Review revised plan with team
   - Adjust scope/timeline as needed
   - Confirm "comment" as prototype domain

2. **Setup Core Domain Module**
   - Create `core/core-domain/build.gradle.kts`
   - Establish ArchUnit tests for purity
   - Document pattern decisions

3. **Prototype Comment Domain**
   - Implement full vertical slice
   - Test thoroughly
   - Validate patterns

4. **Learn and Adjust**
   - Document what works/doesn't work
   - Update this plan
   - Proceed to migration

---

## 12. Summary of Key Revisions

### 12.1 Domain Events
- **Before**: `ApplicationEvent` in domain
- **After**: Pure POJO events in domain; Spring bridge in `support/events`

### 12.2 Entity Mapping
- **Before**: Strategy C (Entity implements Domain)
- **After**: Strategy B (Mapper) with aggregate-level save/load

### 12.3 Value Objects
- **Before**: Immediate JPA mapping
- **After**: Gradual migration (domain first, persistence later)

### 12.4 Dependency Rules
- **Before**: Application → storage/clients
- **After**: Application → Domain Ports ← Infrastructure Adapters

### 12.5 Phase Sequence
- **Before**: Events introduced early
- **After**: Events after one domain fully working

---

## References

- **./ref/docs/ddd-analysis-and-plan.md** - Original DDD analysis (with corrections applied)
- **./ref/docs/event-refactoring-analysis.md** - Event-based refactoring patterns
- **Domain-Driven Design** by Eric Evans
- **Implementing Domain-Driven Design** by Vaughn Vernon
- **Clean Architecture** by Robert C. Martin
- **Patterns, Principles, and Practices of Domain-Driven Design** by Scott Millett
