# Plan: Auth Stability Refactoring

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Resolve systemic risks in custom AOP authorization and optimize request-level database performance.

## 1. Safety Fix (AOP)
- **Problem**: `UseGuardsAspect` was catching `ApplicationException` and returning `ApiResponse`, causing `ClassCastException` on `void` or `String` controller methods.
- **Solution**: Removed `try-catch` from the Aspect. Exceptions now propagate to the `GlobalExceptionHandler` (@RestControllerAdvice), ensuring type safety and proper HTTP status code handling.

## 2. Performance Optimization (Lazy Loading)
- **Problem**: `JwtFilter` was fetching the full `Member` entity from the database on every single request, even for public APIs or APIs only needing the ID.
- **Solution**: 
    - Updated `JwtFilter` to only set the `memberId` in `MemberContext`.
    - Implemented a Lazy Loading mechanism in `ThreadLocalCurrentMemberAccessor`.
    - The `Member` entity is now only fetched from the database when explicitly accessed (e.g., via `@AuthenticatedMember` or a Guard).
    - Added a request-scoped cache (Memoization) within the Accessor to prevent multiple DB hits for the same user in a single request.

## 3. Architectural Decoupling (Accessor Pattern)
- **Problem**: Guards and Resolvers were tightly coupled to the static `MemberContext` (ThreadLocal), making unit testing difficult and prone to side effects.
- **Solution**: 
    - Introduced/Enforced usage of the `CurrentMemberAccessor` interface.
    - `MemberGuard` and `AuthenticatedMemberResolver` now inject this interface, allowing for clean mocking in tests without manual ThreadLocal manipulation.

## 4. Verification
- Updated all Unit Tests (`MemberGuardTest`, `MemberContextTest`, etc.) to match the new architecture.
- Updated all Slice Tests (~8 Controller tests) to correctly initialize the Accessor and Resolver.
- **Result**: All tests passed, performance improved by 1 DB query per request.

## History
- **2025-12-21**: Migrated document into **.agents/work** structure.
