# Plan: Auth Test Migration Guide

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Assist in updating tests affected by the Authentication logic refactoring (Lazy Loading and Accessor pattern).

## Migration Steps

### 1. Fix Slice Tests (`src/test/java/org/veri/be/slice/web/*.java`)
*   **Problem**: Tests manually set `MemberContext.setCurrentMember(member)`.
*   **Solution**: This approach **still works** for setting the cache.
*   **Recommended**: Stick to `setCurrentMember(member)` for Slice Tests to keep them simple.

### 2. Fix Unit Tests (`src/test/java/org/veri/be/unit/auth/*.java`)
*   **MemberGuardTest**: Update to mock `CurrentMemberAccessor`.
*   **ThreadLocalCurrentMemberAccessorTest**: Update to require `MemberRepository`.

### 3. Fix Integration Tests (`IntegrationTestSupport.java`)
*   Ensure single point of setup for `MemberContext`.

### 4. UseGuardsAspect Exception Handling
*   Update tests to expect exceptions instead of `ApiResponse`.

## History
- **2025-12-21**: Migrated document into **.agents/work** structure.
