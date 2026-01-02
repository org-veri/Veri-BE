# Plan: Declarative Current Member Cache

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Replace manual Caffeine cache with declarative caching annotations for current member DTO while preserving TTL and cache limits.

## Steps
- [x] Review current **ThreadLocalCurrentMemberAccessor** and cache usage
- [x] Introduce cache config and cache service using **@Cacheable** with Caffeine
- [x] Update accessor and tests to use declarative cache
- [x] Verify usage sites and update docs if needed

## History
2026-01-02 21:30 | Declarative Current Member Cache | Added cache config and **@Cacheable** service, enabled caching, updated accessor and tests. | Modified files: `core/core-api/build.gradle.kts`, `core/core-app/src/main/java/org/veri/be/Application.java`, `core/core-api/src/main/java/org/veri/be/global/cache/CacheConfig.java`, `core/core-api/src/main/java/org/veri/be/global/auth/context/CurrentMemberInfoCacheService.java`, `core/core-api/src/main/java/org/veri/be/global/auth/context/ThreadLocalCurrentMemberAccessor.java`, `tests/src/test/kotlin/org/veri/be/unit/auth/ThreadLocalCurrentMemberAccessorTest.kt`


## Review
**Source File**:
```
.agents/review/completed/current-member-cache-annotation-review-2026-01-02.md
```

### Review Content

# Implementation Review - 2026-01-02

**Reviewer**: Codex
**Scope**: Declarative caching for current member DTO

## Summary
- Enabled Spring caching and added a Caffeine-backed cache manager with TTL and size limits.
- Moved current member info caching into a **@Cacheable** service and updated accessor/test usage.

## Findings
- No functional regressions identified in reviewed changes.
- **Testing**: No automated tests executed in this review.

## Action Items
- Run relevant unit tests if verification is required.
