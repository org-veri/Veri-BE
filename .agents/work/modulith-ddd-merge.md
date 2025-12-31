# Plan: DDD Modulith Merge Fixes

**Status**: In Progress
**Date**: 2025-12-21
**Goal**: Rebase or merge DDD modulith changes onto develop and fix Kotlin test compilation for the new module structure.

**Parent Task**: `.agents/work/backlog/modulith-migration.md`

## Steps
- [x] Align DDD branch with `develop` and resolve merge conflicts.
- [x] Update test imports and fixtures for new module packages and service splits.
- [x] Fix repository access in tests after package boundary changes.
- [x] Compile Kotlin tests and resolve remaining failures.
- [ ] Run test suite for the phase and document results.
- [x] Commit each logical fix with required commit prefix.

## History
2025-12-21 18:22 - **DDD Modulith Merge Fixes**: Merged `develop` into DDD branch, updated repository visibility and adjusted Kotlin tests for new module structure. **Modified Files**: `core/core-api/src/main/java/org/veri/be/auth/storage/BlacklistedTokenRepository.java`, `core/core-api/src/main/java/org/veri/be/auth/storage/RefreshTokenRepository.java`, `core/core-api/src/main/java/org/veri/be/book/service/BookRepository.java`, `core/core-api/src/main/java/org/veri/be/book/service/ReadingRepository.java`, `core/core-api/src/main/java/org/veri/be/card/service/CardRepository.java`, `core/core-api/src/main/java/org/veri/be/comment/service/CommentRepository.java`, `core/core-api/src/main/java/org/veri/be/image/service/ImageRepository.java`, `core/core-api/src/main/java/org/veri/be/image/service/OcrResultRepository.java`, `core/core-api/src/main/java/org/veri/be/member/service/MemberRepository.java`, `core/core-api/src/main/java/org/veri/be/post/service/LikePostRepository.java`, `core/core-api/src/main/java/org/veri/be/post/service/PostRepository.java`, `tests/src/test/kotlin/org/veri/be/unit/comment/CommentTest.kt`.
