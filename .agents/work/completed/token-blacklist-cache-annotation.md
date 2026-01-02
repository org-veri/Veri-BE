# Plan: Token Blacklist Declarative Cache

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Replace manual negative caching in **TokenStorageService** with declarative caching annotations while preserving TTL and cache limits.

## Steps
- [x] Review existing blacklist cache behavior and usage
- [x] Add cache configuration for blacklist cache name
- [x] Update **TokenStorageService** to use **@Cacheable**/**@CachePut**
- [x] Adjust tests if necessary and verify usage sites

## History
2026-01-02 21:39 | Token Blacklist Declarative Cache | Added cache name for blacklist and migrated negative caching to annotations with eviction on write. | Modified files: `core/core-api/src/main/java/org/veri/be/global/cache/CacheConfig.java`, `core/core-api/src/main/java/org/veri/be/domain/auth/service/TokenStorageService.java`


## Review
**Source File**:
```
.agents/review/completed/token-blacklist-cache-annotation-review-2026-01-02.md
```

### Review Content

# Implementation Review - 2026-01-02

**Reviewer**: Codex
**Scope**: Token blacklist declarative caching

## Summary
- Added a dedicated blacklist cache name in **CacheConfig**.
- Replaced manual negative caching in **TokenStorageService** with **@Cacheable** and **@CacheEvict**.

## Findings
- No functional regressions identified in reviewed changes.
- **Testing**: No automated tests executed in this review.

## Action Items
- Run unit tests if verification is required.
