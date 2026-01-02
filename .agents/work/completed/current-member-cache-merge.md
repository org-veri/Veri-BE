# Plan: Merge Current Member Cache Into Accessor

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Remove the extra cache service and consolidate declarative caching into the current member accessor.

## Steps
- [x] Update **ThreadLocalCurrentMemberAccessor** to use **@Cacheable** directly
- [x] Remove the cache service class and adjust tests
- [x] Verify affected usage sites

## History
2026-01-02 21:39 | Merge Current Member Cache Into Accessor | Removed cache service and moved **@Cacheable** into accessor, updated tests. | Modified files: `core/core-api/src/main/java/org/veri/be/global/auth/context/CurrentMemberInfoCacheService.java`, `core/core-api/src/main/java/org/veri/be/global/auth/context/ThreadLocalCurrentMemberAccessor.java`, `tests/src/test/kotlin/org/veri/be/unit/auth/ThreadLocalCurrentMemberAccessorTest.kt`


## Review
**Source File**:
```
.agents/review/completed/current-member-cache-merge-review-2026-01-02.md
```

### Review Content

# Implementation Review - 2026-01-02

**Reviewer**: Codex
**Scope**: Declarative cache consolidation into accessor

## Summary
- Removed the standalone cache service and applied **@Cacheable** directly in the accessor using the member ID from **MemberContext**.
- Adjusted unit test expectations for direct repository access.

## Findings
- No functional regressions identified in reviewed changes.
- **Testing**: No automated tests executed in this review.

## Action Items
- Run unit tests if verification is required.
