# Issue: Modulith Book/Member Cycle

**Severity**: Low
**Status**: Open
**Date**: 2025-12-30

## Description
Closing the **book** module created a cycle with **member**. The **book** module depends on **member** (Reading entity + profile DTO), while **member** depends on **book** (`ReadingRepository`). Modulith verification fails with a cyclic dependency.

## Affected Files
- **core/core-api/src/main/java/org/veri/be/book/package-info.java**
- **core/core-api/src/main/java/org/veri/be/member/package-info.java**
- **core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingDetailResponse.java**
- **storage/db-core/src/main/java/org/veri/be/book/entity/Reading.java**
- **core/core-api/src/main/java/org/veri/be/member/service/MemberQueryService.java**

## Findings
- **Cycle**: `book` -> `member` (MemberProfileResponse + Member entity) and `member` -> `book` (ReadingRepository).
- **Mitigation applied**: Book module kept **OPEN** to avoid breaking Modulith verification while interfaces are prepared.

## Recommendation
- Decouple **member** from **book** by:
  - Moving `ReadingRepository` usage behind a `BookQueryPort` interface in **book** module.
  - Or inverting dependency by introducing a **read model** in member and an adapter in book.
- Once dependency is removed, switch **book** to **CLOSED** with explicit allowedDependencies.

## Update
- **2025-12-30**: 적용 방향 변경. **Member-owned interface**(`ReadingCountProvider`)를 도입하고 **book** 모듈에서 구현하여 의존성을 단방향으로 조정.
