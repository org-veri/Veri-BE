# System Overview: Veri-BE

## 1. Project Purpose & Scope
**Veri-BE** is a specialized backend platform for book enthusiasts to track their reading journey and engage in social sharing. It bridges personal utility (bookshelf management) with social connectivity (activity feeds).

### Key Business Features
*   **Personal Bookshelf**: Comprehensive tracking of reading states (**NOT_START**, **READING**, **DONE**), personal ratings (0.5 increments), and reading duration.
*   **Reading Cards**: Granular note-taking for specific books, supporting both text and image-based content with **Mistral AI OCR** integration.
*   **Social Interaction**: A public feed where users can share posts, like activities, and leave comments (supporting nested replies and soft-deletion).
*   **Book Discovery**: Integration with **Naver Book Search API** for accurate metadata retrieval and indexing.

---

## 2. Technical Stack & Infrastructure

### Core Frameworks
*   **Runtime**: **Java 25** (Early Access) utilizing **Virtual Threads** for optimal concurrency.
*   **Framework**: **Spring Boot 4.0.0**.
*   **Security**: **Spring Security** with **OAuth2 Client** (Kakao) and **JWT** (Dual-token: 1h Access / 30d Refresh).

### Data & Persistence
*   **Primary Database**: **MySQL** (Production), **H2** (In-memory for testing).
*   **JPA/Hibernate**:
    *   **OSIV Disabled**: `spring.jpa.open-in-view: false` is enforced. All services must return DTOs.
    *   **DDL Policy**: `update` for production, `create` for tests.
*   **Query DSL**: **`me.miensoap:fluent:1.0-SNAPSHOT`** for type-safe, fluent query construction.

### External Integrations
*   **Storage**: **AWS S3** via `io.github.miensoap:aws-s3` library.
*   **AI/OCR**: **Mistral AI** async processing with a dedicated **`ocrExecutor`** thread pool.
*   **Social**: **Kakao OAuth2** for seamless authentication.

### DevOps
*   **Deployment**: **Blue-Green strategy** (Green: Oracle Cloud / Blue: AWS).
*   **Monitoring**: **Spring Boot Actuator** and **Prometheus** metrics.
*   **API Docs**: **Swagger UI** (`/resources/docs/swagger`) and **OpenAPI 3.0** (`/resources/docs/v3/api-docs`).

---

## 3. Architecture & Layering

The system is designed as a **Modular Monolith** following **CQS (Command Query Separation)**.

### Directory Structure
*   **`api/`**: Presentation layer grouped by context (**Common**, **Personal**, **Social**).
*   **`domain/`**: Bounded contexts (Auth, Book, Card, Comment, Image, Member, Post).
*   **`global/`**: Cross-cutting infrastructure (Auth guards, Interceptors, S3, Standard Response).
*   **`lib/`**: Generic reusable utilities (JWT logic, Exception framework, Time abstractions).

### Service Layer Standards
*   **QueryService**: Strictly read-only operations with `@Transactional(readOnly=true)`. Must return DTOs.
*   **CommandService**: State-changing operations with `@Transactional`. Orchestrates domain entities.
*   **Converters**: Implemented as **Spring Beans** (not static utils) to allow dependency injection (e.g., `CurrentMemberAccessor`).

---

## 4. Design Principles & Patterns

### Domain Model Pattern
*   **Rich Entities**: Business logic is encapsulated within JPA Entities (e.g., `Reading.finish()`, `Card.authorizeMember()`) to avoid **Anemic Domain Models**.
*   **Ownership**: Entities implement authorization checks to ensure users can only modify their own data.

### Testability by Design
*   **Dependency Injection over Static Calls**: Use `Clock` bean for time and `StorageKeyGenerator` for UUIDs to ensure deterministic testing.
*   **Interface Abstraction**: All external boundaries are interfaced (`OcrService`, `BookSearchClient`) to allow **Stubbing** without Mockito.

### Robustness & Safety
*   **Validation**: Mandatory JSR-303 **`@Valid`** on all Controller request bodies.
*   **Error Handling**: Standardized via domain-specific **Error Enums** and a **`GlobalExceptionHandler`** capturing `JwtException`, `IllegalArgumentException`, etc.
*   **Idempotency**: Critical operations (like Logout or duplicate Book adds) are handled idempotently.

---

## 5. Testing Strategy

### Integration Tests
*   **Zero Mocking Policy**: Internal services are never mocked.
*   **Stubs**: External systems (S3, OCR, Naver) are replaced with **Stubs** in `SharedTestConfig` to test real business flows and DB interactions.
*   **Baseline**: All features must pass the **70+ scenario suite** managed in `api-test-scenarios-checklist.md`.

### Slice & Unit Tests

*   **Controller Slice**: Use `@WebMvcTest` to verify HTTP contracts, validation, and security guards.

*   **Persistence Slice**: Use `@DataJpaTest` to verify:

    *   **Mappings**: Entity-table mapping, constraints (NotNull/Unique), and relations (Cascade/OrphanRemoval).

    *   **Queries**: Accuracy of custom Fluent DSL or JPQL queries (Filtering, Sorting, Paging).

    *   **Performance**: Prevention of N+1 issues and verification of index usage for critical paths.

*   **Unit Tests**: Fast POJO tests for complex logic residing in Entities.
