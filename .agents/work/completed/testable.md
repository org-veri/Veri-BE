# Log: Architectural Testability Improvements

**Status**: Completed
**Goal**: Address non-testable code patterns and decouple dependencies.

## Accomplishments

### 1. Control of Volatile Values
- [x] **BookshelfService**: Injected `Clock` instead of calling `LocalDateTime.now()` directly.
- [x] **AuthService**: Injected `Clock` for token expiration logic.
- [x] **TokenStorageService**: Injected `Clock` for blacklist expiration checks.
- [x] **Comment/OCR**: Abstracted `SleepSupport` and `Clock` for temporal control.

### 2. Dependency Injection Enhancements
- [x] **BookService**: Injected `ObjectMapper` bean.
- [x] **Controllers**: Updated `CardController`, `PostController`, and `CommentController` to receive `Member` via `@AuthenticatedMember` instead of relying on static context in services.

### 3. Domain Model Logic (POJO)
- [x] **Entities**: Moved complex state transitions and business rules from services into `Reading`, `Card`, `Post`, and `Comment` entities.
- [x] **Services**: Refactored services to act as orchestrators rather than rule-holders.

### 4. Static Removal & Abstraction
- [x] **MemberContext**: Introduced `CurrentMemberAccessor` interface to wrap static `ThreadLocal`.
- [x] **JwtUtil**: Refactored into `JwtService` bean.
- [x] **Converters**: Converted to Spring Beans or static factory methods.
- [x] **Storage**: Introduced `StorageKeyGenerator` interface.

### 5. External System Abstraction
- [x] **BookSearch**: Introduced `BookSearchClient` interface for Naver OpenAPI.
- [x] **OCR**: Introduced `OcrService` interface and `AbstractOcrService` base.
- [x] **Auth**: Abstracted `TokenProvider` and `TokenBlacklistStore`.

## History
- **2025-12-21**: Migrated log into **.agents/work** structure.
