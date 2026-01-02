# Plan: Cache Key Generator Refactor

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Replace SpEL cache key/condition usage with a reusable KeyGenerator strategy for context-based caches.

## Steps
- [x] Add **ContextKeyProvider** interface and **ContextAwareKeyGenerator**
- [x] Apply KeyGenerator to member and token blacklist caches
- [x] Run tests to validate changes

## History
2026-01-02 22:55 | Cache Key Generator Refactor | Introduced context-aware key generator and applied it to member info and token blacklist caching. | Modified files: `core/core-api/src/main/java/org/veri/be/global/cache/ContextKeyProvider.java`, `core/core-api/src/main/java/org/veri/be/global/cache/ContextAwareKeyGenerator.java`, `core/core-api/src/main/java/org/veri/be/global/auth/context/ThreadLocalCurrentMemberAccessor.java`, `core/core-api/src/main/java/org/veri/be/domain/auth/service/TokenStorageService.java`
2026-01-02 23:08 | Cache Key Generator Refactor | Fixed context key type handling and updated tests to align with Optional behavior. | Modified files: `core/core-api/src/main/java/org/veri/be/global/auth/context/ThreadLocalCurrentMemberAccessor.java`, `tests/src/test/kotlin/org/veri/be/unit/book/ReadingConverterTest.kt`, `core/core-api/src/main/java/org/veri/be/global/cache/ContextAwareKeyGenerator.java`, `core/core-api/src/main/java/org/veri/be/global/auth/context/CurrentMemberAccessor.java`, `tests/src/test/kotlin/org/veri/be/slice/web/BookshelfControllerTest.kt`, `tests/src/test/kotlin/org/veri/be/slice/web/CommentControllerTest.kt`, `tests/src/test/kotlin/org/veri/be/slice/web/PostControllerTest.kt`, `tests/src/test/kotlin/org/veri/be/slice/web/MemberControllerTest.kt`, `tests/src/test/kotlin/org/veri/be/slice/web/ImageControllerTest.kt`, `tests/src/test/kotlin/org/veri/be/slice/web/CardControllerTest.kt`, `tests/src/test/kotlin/org/veri/be/slice/web/SocialCardControllerTest.kt`, `tests/src/test/kotlin/org/veri/be/unit/auth/CurrentMemberAccessorTest.kt`

## Review
**Summary**
- Added a reusable **ContextAwareKeyGenerator** and applied it to context-based caches.
- Adjusted context key handling to avoid null key failures.
- Added nullable accessor hook to skip caching missing member info.

**Findings**
- None.

**Action Items**
- None.
