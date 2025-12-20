# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Veri-BE** is a Spring Boot 4.0 backend application for a book reading tracking and social sharing platform. The project is built with Java 25, uses MySQL for persistence, and integrates with external services (Kakao OAuth2, Naver Book API, AWS S3, Mistral OCR).

## Build & Development Commands

### Building
```bash
# Build without tests
./gradlew clean build -x test

# Build with tests
./gradlew clean build

# Run tests
./gradlew test
```

### Running Locally
```bash
# Run with local profile (requires local DB and environment variables)
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Docker
```bash
# Build Docker image locally
make build

# Build and push multi-platform images (linux/amd64, linux/arm64)
make buildx-and-push APP_TAG=v1.0.0

# Deploy to servers (green -> blue sequence)
make deploy
```

### Testing

#### Test Types
- **Integration tests**: Extend `IntegrationTestSupport` which provides `@SpringBootTest` setup with a pre-configured `mockMember` and `MemberContext`
- **Controller slice tests**: Use `@WebMvcTest` to verify parameter validation, HTTP status codes, and error types
- **Persistence tests**: Use `@DataJpaTest` for entity mappings, constraints, cascades, and repository queries
- **Unit tests**: POJO tests for domain logic (fast, no Spring context)

#### Test Organization
- Package structure: `test/java/org/veri/be/{integration|slice|unit}/{domain}/`
- Use `@Nested` + `@DisplayName` for readability
- Follow Given-When-Then pattern
- See `docs/test-convention.md` for detailed conventions
- See `docs/api-test-scenarios.md` for comprehensive endpoint scenarios

#### Test Checkpoint Workflow
After implementing features, use the test checkpoint system:

```bash
# Analyze untested changes and generate comprehensive tests
/sc:test-checkpoint
```

This will:
1. Run `.claude/scripts/get_target_diff.sh` to identify changes since last `test.` commit
2. Generate tests following strict conventions in `docs/test-convention.md`
3. Create a test integrity checkpoint with `test.` prefixed commit

**Convention**: All feature work should be followed by a test checkpoint commit to maintain test coverage

## Architecture

### High-Level Structure

The codebase follows **CQS (Command Query Separation)** pattern with domain-driven organization:

```
org.veri.be
├── api/                    # Presentation layer (Controllers grouped by API type)
│   ├── common/            # Common APIs (Auth, Image)
│   ├── personal/          # Personal APIs (Member, Bookshelf, Card)
│   └── social/            # Social APIs (Feed, Post, Comment)
├── domain/                # Business logic organized by bounded contexts
│   ├── auth/             # Authentication & token management
│   ├── book/             # Book catalog & reading tracking (bookshelf)
│   ├── card/             # Reading cards (quotes/notes)
│   ├── comment/          # Post comments
│   ├── image/            # Image upload & OCR processing
│   ├── member/           # User management
│   └── post/             # Social posts
├── global/               # Cross-cutting concerns
│   ├── auth/            # Auth infrastructure (guards, interceptors, context)
│   ├── config/          # Spring configurations
│   ├── storage/         # AWS S3 integration
│   └── response/        # Standardized response wrappers
└── lib/                  # Reusable utilities
    ├── auth/            # JWT utilities
    ├── exception/       # Exception handling framework
    ├── response/        # Response builders
    └── time/            # Time abstractions (Clock)
```

### Service Layer Pattern

Each domain follows a consistent layering:

- **QueryService**: Read-only operations with `@Transactional(readOnly=true)`, returns DTOs to avoid lazy-loading issues (OSIV disabled)
- **CommandService**: State-changing operations with full `@Transactional`
- **Repository**: Data access using Spring Data JPA
- **Entity**: Domain models with business logic methods (Domain Model Pattern, not Anemic Domain Model)
- **Converter**: Stateful beans (not static utilities) that transform entities to DTOs, injected with dependencies like `CurrentMemberAccessor`

### Key Design Principles (from docs/testable.md)

The codebase has been refactored for testability:

1. **Dependency Injection over Static Utilities**:
   - Use `Clock` bean for time-dependent logic (not `LocalDateTime.now()`)
   - Use `CurrentMemberAccessor` instead of static `MemberContext` calls
   - Converters are Spring beans, not static utility classes

2. **Domain Logic in Entities**:
   - Rich domain models (e.g., `Reading.startReading()`, `Card.assertReadableBy()`, `Post.publishBy()`)
   - Services orchestrate, entities encapsulate rules
   - Enables pure POJO testing without Spring context

3. **External System Abstraction**:
   - `BookSearchClient` interface for Naver API
   - `OcrService` interface for Mistral OCR
   - `TokenProvider` and `TokenBlacklistStore` interfaces for auth

4. **Controlled Non-Determinism**:
   - `StorageKeyGenerator` for UUID generation
   - `SleepSupport` for delays
   - All time operations via injected `Clock`

### Authentication & Authorization

- **OAuth2**: Kakao login via Spring Security OAuth2 client
- **JWT**: Dual-token system (access + refresh tokens)
  - Access tokens: 1 hour validity
  - Refresh tokens: 30 days, stored in `TokenStorage` (likely Redis)
  - Blacklist support for logout
- **Member Context**: Current user injected via `@AuthenticatedMember Member` parameter in controllers
- **Authorization**: Domain entities implement `authorizeMember(Member)` for ownership checks

### Data Access

- **JPA/Hibernate**: `ddl-auto: update` (caution in production)
- **OSIV disabled**: `spring.jpa.open-in-view: false` - always return DTOs from services
- **Virtual Threads**: Enabled via `spring.threads.virtual.enabled: true` (Java 21+)
- **Fluent Query DSL**: Custom library `me.miensoap:fluent:1.0-SNAPSHOT` for type-safe queries

### External Integrations

- **AWS S3**: Image storage via `io.github.miensoap:aws-s3:1.0.3` library
- **Naver Book Search API**: Book metadata retrieval
- **Mistral OCR**: Async OCR processing with retry logic, uses dedicated `ocrExecutor` thread pool
- **Monitoring**: Actuator + Prometheus metrics exposed

### API Documentation

- **Swagger UI**: Available at `/resources/docs/swagger`
- **OpenAPI Spec**: `/resources/docs/v3/api-docs`

## Environment Configuration

Required environment variables (see `application.yml`):
- `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `KAKAO_REDIRECT_URI` (OAuth2 callback)
- JWT secrets should be overridden in production (currently using test values)

Profiles:
- `local`: Local development (see `application-local.yml`)
- `prod`: Production deployment (see `application-prod.yml`)

## Deployment

Blue-Green deployment strategy:
1. Deploy to GREEN server (Oracle Cloud)
2. Health check via `/actuator/health`
3. Deploy to BLUE server (AWS)

Deployment scripts in `deploy/` directory.

## Important Notes

### Modulith Migration Path (from docs/ddd-modulith.md)

The project is structured for potential Spring Modulith adoption:
- `domain/` packages represent Bounded Contexts
- Consider moving `api/` controllers into their respective `domain/` packages
- Use package-private visibility to enforce module boundaries
- Leverage domain events (`ApplicationEventPublisher`) to reduce coupling

### Testing Guidelines (from docs/api-test-scenarios.md)

1. Use `IntegrationTestSupport` for service/integration tests (no mocking by default)
2. Create additional mock members when testing authorization between users
3. Separate authentication tests into dedicated test classes
4. Controller tests validate parameters, status codes, and error types only
5. Service tests cover business logic and edge cases

### Common Patterns

- **Error Handling**: Domain-specific error enums (e.g., `BookErrorInfo`, `CardErrorInfo`) with `BadRequestException`, `ForbiddenException`, etc.
- **Pagination**: Custom `PageResponse` wrapper for consistent API responses
- **Validation**: Bean Validation on DTOs, domain validation in entity methods
- **Soft Delete**: `Comment` entity uses `isDeleted` flag with `deletedAt` timestamp
