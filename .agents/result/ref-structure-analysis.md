# Ref Structure Analysis: Principles, Pros & Cons

**Status**: Complete
**Date**: 2025-12-31
**Source**: ./ref (dev-practice-commerce)

---

## 1. Ref Structure Overview

### 1.1 Directory Structure

```
dev-practice-commerce/
├── core/
│   ├── core-enum          # Shared domain types
│   └── core-api           # API layer (Controller, DTO, Service)
├── storage/
│   └── db-core            # Database persistence (Entity, Repository)
└── support/
    ├── logging            # Logging infrastructure
    └── monitoring         # Monitoring infrastructure
```

### 1.2 Module Dependencies

```
core-api → core-enum + db-core + support/logging + support/monitoring
```

**Visualization**:
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

---

## 2. Design Principles

### 2.1 Physical Layer Separation

**Principle**: Gradle modules provide **compile-time boundaries** between layers.

**Implementation**:
```kotlin
// settings.gradle.kts
include(
    "core:core-enum",
    "core:core-api",
    "storage:db-core",
    "support:logging",
    "support:monitoring",
)
```

**Benefits**:
- **Explicit Dependencies**: All dependencies declared in `build.gradle.kts`
- **Compile-Time Checks**: Circular dependencies cause build failures
- **Independent Versioning**: Each module can version independently

### 2.2 Layer Responsibility Definition

| Module | Responsibility | Contents |
|--------|---------------|----------|
| **core-enum** | Shared domain types | Enums, constants, value objects |
| **core-api** | Presentation layer | Controllers, DTOs, API services |
| **db-core** | Persistence layer | JPA entities, repositories, EntityManager |
| **support** | Infrastructure | Cross-cutting concerns (logging, monitoring) |

### 2.3 Dependency Direction

**Rule**: High-level modules should not depend on low-level modules.

**Current Flow**: `core-api` (highest) → `db-core` (middle) → `support` (lowest)

**Issue**: This is **inverted** from Clean Architecture principles.

**Clean Architecture Flow**:
```
      ┌─────────────────────────────────────┐
      │         Controllers (API)           │
      └─────────────────┬───────────────────┘
                        │
      ┌─────────────────▼───────────────────┐
      │      Application Services           │
      └─────────────────┬───────────────────┘
                        │
      ┌─────────────────▼───────────────────┐
      │         Domain Logic                │
      └─────────────────┬───────────────────┘
                        │
      ┌─────────────────▼───────────────────┐
      │      Infrastructure (DB, API)       │
      └─────────────────────────────────────┘
```

**Ref Structure Issue**: Domain logic is mixed into API layer.

### 2.4 Separation of Concerns

**Core-Enum Independence**:
- Pure Kotlin module
- No Spring dependencies
- Shareable across all modules

**Support Module Isolation**:
- Infrastructure concerns separated
- Easy to swap implementations
- Minimal domain knowledge

---

## 3. Advantages

### 3.1 Gradle Module Boundaries

**Strength**: Compile-time dependency enforcement

**Example**:
```kotlin
// core-api/build.gradle.kts
dependencies {
    implementation(project(":core:core-enum"))
    implementation(project(":storage:db-core"))
    implementation(project(":support:logging"))

    // If core-enum tries to depend on core-api:
    // BUILD ERROR: Circular dependency detected
}
```

**Benefits**:
- Prevents accidental coupling
- Forces conscious dependency decisions
- IDE auto-completion respects boundaries

### 3.2 Persistence Independence

**Strength**: `db-core` encapsulates JPA/Hibernate

**Example**:
```kotlin
// db-core/persistence/ProductRepositoryImpl.kt
@Repository
class ProductRepositoryImpl(
    private val entityManager: EntityManager
) : ProductRepository {
    // JPA details hidden from other layers
}
```

**Benefits**:
- Can replace JPA with other ORM (e.g., JDBI, Hibernate)
- Domain layer unaware of persistence technology
- Test domain logic without database

### 3.3 Infrastructure Modularization

**Strength**: `support` modules are plug-and-play

**Example**:
```kotlin
// support/logging/build.gradle.kts
dependencies {
    api("org.slf4j:slf4j-api")
    implementation("ch.qos.logback:logback-classic")
}

// support/monitoring/build.gradle.kts
dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

**Benefits**:
- Logging can be replaced without touching domain logic
- Monitoring can be swapped (e.g., Prometheus → CloudWatch)
- Easy to add new support modules (e.g., caching, messaging)

### 3.4 Kotlin Language Features

**Strength**: Kotlin's type safety and conciseness

**Example**:
```kotlin
// Null-safe code
fun findProduct(id: ProductId): Product? {
    return repository.findById(id.value)  // Returns null if not found
}

// Data class for DTO
data class CreateProductRequest(
    val name: String,
    val price: BigDecimal,
    val category: ProductCategory
)

// Extension functions
fun ProductEntity.toDomain(): Product {
    return Product(
        id = ProductId(this.id),
        name = this.name,
        price = Money(this.price)
    )
}
```

**Benefits**:
- Reduced boilerplate
- Compile-time null checks
- Expressive code through extensions

---

## 4. Disadvantages

### 4.1 Anemic Domain Model Risk

**Issue**: Business logic concentrated in Service layer

**Example**:
```kotlin
// core-api/service/ProductService.kt
@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val inventoryService: InventoryService,
    private val pricingService: PricingService
) {
    fun updatePrice(productId: Long, newPrice: BigDecimal) {
        val product = productRepository.findById(productId)
            ?: throw ProductNotFoundException(productId)

        // Business logic in service (anemic)
        if (newPrice < BigDecimal.ZERO) {
            throw InvalidPriceException("Price cannot be negative")
        }

        pricingService.validatePriceChange(product.price, newPrice)

        product.price = newPrice  // Setter-style mutation
        product.updatedAt = LocalDateTime.now()

        productRepository.save(product)

        inventoryService.notifyPriceChange(productId, newPrice)
    }
}
```

**Problems**:
- Domain object is a data container (no behavior)
- Business rules scattered across services
- Hard to reuse domain logic
- Difficult to test in isolation

**Rich Domain Model Alternative**:
```kotlin
// core-domain/product/model/Product.kt
@Aggregate
class Product(
    val id: ProductId,
    var price: Money,
    val category: ProductCategory
) {
    fun updatePrice(newPrice: Money, pricingPolicy: PricingPolicy) {
        pricingPolicy.validatePriceChange(this.price, newPrice)
        this.price = newPrice
        this.updatedAt = LocalDateTime.now()

        // Domain event
        registerEvent(ProductPriceChangedEvent(id, newPrice))
    }
}
```

### 4.2 Missing DDD Strategic Patterns

**Issue**: No Aggregate Root, Domain Events, or Bounded Contexts

#### 4.2.1 No Aggregate Root Distinction

**Example**:
```kotlin
// All entities treated equally
class Product(val id: Long, val name: String)
class ProductImage(val id: Long, val productId: Long, val url: String)
class ProductReview(val id: Long, val productId: Long, val rating: Int)

// No clear aggregate root
// ProductImage can be modified without Product knowledge
// ProductReview can be deleted independently
```

**DDD Approach**:
```kotlin
@AggregateRoot
class Product(
    val id: ProductId,
    val name: String,
    private val images: MutableList<ProductImage>,
    private val reviews: MutableList<ProductReview>
) {
    // Only Product can modify its images
    fun addImage(image: ProductImage) {
        validateImageLimit()
        images.add(image)
    }

    // Only Product can manage reviews
    fun addReview(review: ProductReview) {
        reviews.add(review)
        updateRating()
    }

    private fun validateImageLimit() {
        if (images.size >= MAX_IMAGES) {
            throw ProductDomainException("Image limit exceeded")
        }
    }
}
```

#### 4.2.2 No Domain Event Mechanism

**Example**:
```kotlin
// Side effects handled directly
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val inventoryService: InventoryService,
    private val notificationService: NotificationService,
    private val analyticsService: AnalyticsService
) {
    fun placeOrder(order: Order) {
        orderRepository.save(order)
        inventoryService.decreaseStock(order.items)  // Direct call
        notificationService.sendConfirmation(order.userId)  // Direct call
        analyticsService.recordPurchase(order)  // Direct call
    }
}
```

**Problems**:
- Tight coupling between Order and other services
- Hard to add new side effects (must modify OrderService)
- Difficult to test (need to mock many dependencies)

**Domain Event Approach**:
```kotlin
// core-domain/order/model/Order.kt
@AggregateRoot
class Order(...) {
    fun place() {
        // Domain logic
        this.status = OrderStatus.PLACED

        // Register event
        registerEvent(OrderPlacedEvent(
            orderId = this.id,
            userId = this.userId,
            items = this.items
        ))
    }
}

// support/events/handler/OrderEventHandler.kt
@Component
class OrderEventHandler(
    private val inventoryService: InventoryService,
    private val notificationService: NotificationService,
    private val analyticsService: AnalyticsService
) {
    @EventListener
    @Async
    fun handleOrderPlaced(event: OrderPlacedEvent) {
        inventoryService.decreaseStock(event.items)
        notificationService.sendConfirmation(event.userId)
        analyticsService.recordPurchase(event)
    }
}

// Application service
@Service
class OrderCommandService(
    private val orderRepository: OrderRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun placeOrder(command: PlaceOrderCommand): OrderId {
        val order = Order.create(command)
        order.place()
        orderRepository.save(order)

        // Publish domain events
        order.domainEvents.forEach { eventPublisher.publishEvent(it) }
        order.clearDomainEvents()

        return order.id
    }
}
```

#### 4.2.3 No Bounded Context Communication

**Issue**: All domains in same layer, no explicit boundaries

**Example**:
```kotlin
// core-api contains everything
// PaymentService directly calls OrderService, InventoryService, ShippingService
// No clear boundaries between payment, order, inventory, shipping contexts
```

**Bounded Context Approach**:
```
Payment Context (BC)      Order Context (BC)      Inventory Context (BC)
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│PaymentModule │         │ OrderModule  │         │InventoryModule│
│              │         │              │         │              │
│PaymentService│────────→│OrderService  │────────→│InventorySvc  │
│PaymentRepo   │         │OrderRepo     │         │InventoryRepo │
└──────────────┘         └──────────────┘         └──────────────┘
       ↑                        ↑                         ↑
       │                        │                         │
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│PaymentEvents │         │ OrderEvents  │         │InventoryEvents│
└──────────────┘         └──────────────┘         └──────────────┘
```

### 4.3 Dependency Inversion Violation

**Issue**: Domain depends on persistence

**Example**:
```kotlin
// db-core needs to know about core-api models
// because core-api services use JPA entities directly

// core-api/service/ProductService.kt
@Service
class ProductService(
    private val productRepository: ProductRepository  // Returns JPA Entity
) {
    fun findProduct(id: Long): ProductEntity {  // Exposed JPA Entity
        return productRepository.findById(id)
            .orElseThrow { ProductNotFoundException(id) }
    }
}

// db-core/persistence/ProductRepositoryImpl.kt
@Repository
class ProductRepositoryImpl : ProductRepository {
    override fun findById(id: Long): ProductEntity {  // Returns JPA Entity
        return entityManager.find(ProductEntity::class.java, id)
    }
}
```

**Problems**:
- Domain logic coupled to JPA annotations
- Cannot test domain without JPA provider
- Cannot replace persistence technology easily

**Dependency Inversion Approach**:
```kotlin
// core-domain/product/model/ProductRepository.kt
interface ProductRepository {
    fun findById(id: ProductId): Product?  // Returns domain model
    fun save(product: Product): Product
}

// core-application/product/service/ProductCommandService.kt
@Service
class ProductCommandService(
    private val productRepository: ProductRepository  // Domain interface
) {
    fun updateProduct(command: UpdateProductCommand) {
        val product = productRepository.findById(command.id)
            ?: throw ProductNotFoundException(command.id)

        product.updatePrice(command.newPrice)  // Domain behavior

        productRepository.save(product)
    }
}

// db-core/persistence/ProductRepositoryImpl.kt
@Repository
class ProductRepositoryImpl : ProductRepository {
    override fun findById(id: ProductId): Product? {
        return entityManager.find(ProductEntity::class.java, id.value)?.toDomain()
    }

    override fun save(product: Product): Product {
        val entity = ProductEntity.fromDomain(product)
        val saved = entityManager.merge(entity)
        return saved.toDomain()
    }
}
```

### 4.4 No Bounded Context Boundaries

**Issue**: Commerce subdomains not separated

**Example**:
```
core-api/src/main/kotlin/io/dodn/commerce/
├── product/          # Product subdomain
│   ├── Product.kt
│   └── ProductService.kt
├── order/            # Order subdomain
│   ├── Order.kt
│   └── OrderService.kt
├── payment/          # Payment subdomain
│   ├── Payment.kt
│   └── PaymentService.kt
└── catalog/          # Catalog subdomain
    ├── Catalog.kt
    └── CatalogService.kt
```

**Problems**:
- All subdomains in same module (`core-api`)
- No explicit boundaries between contexts
- Easy to accidentally couple subdomains

**Bounded Context Separation**:
```
core/
├── core-domain/
│   ├── product/      # Product BC
│   ├── order/        # Order BC
│   ├── payment/      # Payment BC
│   └── catalog/      # Catalog BC
├── core-application/
│   ├── product/
│   ├── order/
│   ├── payment/
│   └── catalog/
└── core-api/
    ├── product/
    ├── order/
    ├── payment/
    └── catalog/
```

### 4.5 Test Strategy Ambiguity

**Issue**: No clear separation between unit and integration tests

**Example**:
```kotlin
// All tests in same module
// core-api/src/test/ contains:
// - Unit tests (mocked dependencies)
// - Integration tests (real database)
// - End-to-end tests (full Spring context)
```

**Problems**:
- Slow test suite (integration tests mixed with unit tests)
- Unclear test scope
- Difficult to run fast feedback loop

**Clear Test Strategy**:
```
core-domain/src/test/          # Fast unit tests (no infrastructure)
├── product/model/ProductTest.kt
└── order/model/OrderTest.kt

core-application/src/test/     # Application service tests (mocked repos)
├── product/service/ProductCommandServiceTest.kt
└── order/service/OrderCommandServiceTest.kt

storage/db-core/src/test/      # Persistence tests (Testcontainers)
├── product/persistence/ProductRepositoryTest.kt
└── order/persistence/OrderRepositoryTest.kt

tests/                         # Full integration tests (@SpringBootTest)
└── ProductIntegrationTest.kt
```

---

## 5. Comparison with Clean Architecture

### 5.1 Ref Structure (Layered Architecture)

```
┌─────────────────────────────────────────────────────┐
│                   core-api                          │
│  (Controller + Service + DTO + Business Logic)      │
└─────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼─────┐
│   db-core    │ │  core-enum  │ │  support  │
│ (JPA Entity) │ │  (Shared)   │ │ (Logging) │
└──────────────┘ └─────────────┘ └───────────┘
```

**Issues**:
- Business logic in outer layer
- Domain depends on infrastructure
- Hard to test without framework

### 5.2 Clean Architecture

```
┌───────────────────────────────────────────────────────┐
│                     Controllers                       │
│                  (core-api, web)                      │
└───────────────────────────┬───────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────┐
│              Application Services                     │
│              (core-application, use cases)            │
└───────────────────────────┬───────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────┐
│                  Domain Logic                         │
│              (core-domain, entities, value objects)   │
└───────────────────────────┬───────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────┐
│                 Infrastructure                        │
│        (storage/db-core, support, clients)            │
└───────────────────────────────────────────────────────┘
```

**Benefits**:
- Domain logic in inner layer (independent)
- Infrastructure implements domain interfaces
- Easy to test (mock outer layers)

---

## 6. When to Use Ref Structure

### 6.1 Suitable For

| Scenario | Reason |
|----------|--------|
| **Small Teams** | Simple module structure, easy to understand |
| **MVP / Prototype** | Fast development, clear layer separation |
| **Low Complexity** | Simple CRUD applications |
| **Kotlin Expertise** | Team comfortable with Kotlin's type system |

### 6.2 Not Suitable For

| Scenario | Reason |
|----------|--------|
| **Complex Business Logic** | Anemic domain model, logic scattered |
| **Large Teams** | Need clear bounded contexts for parallel work |
| **Long-Term Maintenance** | Technical debt accumulates in services |
| **Frequent Requirement Changes** | Tight coupling makes changes risky |

---

## 7. Recommendations

### 7.1 For Simple Projects

**Use Ref Structure As-Is**:
- Clear physical boundaries
- Easy to understand
- Sufficient for CRUD applications

### 7.2 For Complex Projects

**Add DDD Patterns**:

1. **Create `core-domain` Module**:
   - Pure domain logic
   - No infrastructure dependencies
   - Rich domain models

2. **Implement Domain Events**:
   - Decouple subdomains
   - Enable asynchronous processing
   - Improve extensibility

3. **Apply DIP**:
   - Domain defines interfaces
   - Infrastructure implements interfaces
   - Test domain without infrastructure

4. **Define Bounded Contexts**:
   - Separate subdomains into modules
   - Explicit context boundaries
   - Context mapping between BCs

### 7.3 Migration Path

**From Ref to DDD**:

```
Phase 1: Add core-domain module
Phase 2: Move domain logic from services to entities
Phase 3: Implement domain events
Phase 4: Apply dependency inversion
Phase 5: Separate bounded contexts
```

---

## 8. Summary

### 8.1 Ref Structure Strengths

- Physical module boundaries (Gradle)
- Clear layer separation
- Infrastructure modularization
- Kotlin language benefits

### 8.2 Ref Structure Weaknesses

- Anemic domain model
- Missing DDD patterns
- Dependency inversion violation
- No bounded context boundaries

### 8.3 Conclusion

**Ref structure is a good starting point** for Spring Boot projects, but for complex business domains, it should be extended with DDD patterns:

- Add `core-domain` for pure domain logic
- Implement domain events for decoupling
- Apply DIP for clean dependencies
- Define bounded contexts for large projects

The modular foundation is solid. The missing pieces are DDD strategic patterns and Clean Architecture principles.

---

## References

- **./ref/docs/ddd-analysis-and-plan.md** - DDD implementation plan
- **./ref/docs/event-refactoring-analysis.md** - Event-based refactoring analysis
- **Domain-Driven Design** by Eric Evans
- **Clean Architecture** by Robert C. Martin
- **Implementing Domain-Driven Design** by Vaughn Vernon
