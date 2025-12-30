# Issue: Comment-Post Modulith Cycle

**Severity**: High
**Status**: Open
**Date**: 2025-12-30

## Description
Modulith verification detects a cycle between **comment** and **post** modules due to mutual entity references and service usage.

## Affected Files
```
storage/db-core/src/main/java/org/veri/be/comment/entity/Comment.java
storage/db-core/src/main/java/org/veri/be/post/entity/Post.java
core/core-api/src/main/java/org/veri/be/comment/service/CommentCommandService.java
core/core-api/src/main/java/org/veri/be/comment/service/CommentQueryService.java
core/core-api/src/main/java/org/veri/be/post/service/PostQueryService.java
core/core-api/src/main/java/org/veri/be/post/dto/response/PostDetailResponse.java
```

## Findings
- **Comment** references **Post** as a parent association.
- **Post** aggregates **Comment** list and exposes comment data in DTOs.
- **CommentCommandService** depends on **PostQueryService**, while **PostQueryService** depends on **CommentQueryService**.

## Recommendation
- Decide a single ownership boundary for comment aggregation (e.g., move comment list projections into **comment** module and consume via DTO).
- Replace direct entity references with module-level DTOs or interfaces to remove mutual dependencies.
- Until refactoring, keep **comment** module **OPEN** to avoid cyclic Modulith violations.

## Update
**Date**: 2025-12-30
**Status**: Completed

### Resolution
- Removed direct **Comment -> Post** entity association by storing `postId` in **Comment**.
- Comment queries now return **Comment** entities, and post detail responses map comments internally.
- Comment module closed with dependencies limited to **global/lib/member** and cycles cleared.
