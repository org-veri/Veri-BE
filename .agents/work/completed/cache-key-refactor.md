# Plan: Cache Key Condition Refactor

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Refactor cache key/condition logic to avoid SpEL static access and keep caching behavior intact.

## Steps
- [x] Refactor **ThreadLocalCurrentMemberAccessor** cache key/condition to use target helpers
- [x] Update review notes and close task

## History
2026-01-02 22:40 | Cache Key Condition Refactor | Replaced static SpEL key/condition with target helper methods for current member cache. | Modified files: `core/core-api/src/main/java/org/veri/be/global/auth/context/ThreadLocalCurrentMemberAccessor.java`

## Review
**Summary**
- Cache key/condition now uses accessor helper methods to avoid static SpEL calls.

**Findings**
- None.

**Action Items**
- None.
