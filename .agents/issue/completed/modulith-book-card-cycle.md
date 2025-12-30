# Issue: Book-Card Modulith Cycle

**Severity**: High
**Status**: Open
**Date**: 2025-12-30

## Description
Modulith verification reports a cycle between **book** and **card** modules. The cycle is caused by the bidirectional domain relationship between **Reading** and **Card**, plus cross-module DTO usage.

## Affected Files
```
storage/db-core/src/main/java/org/veri/be/book/entity/Reading.java
storage/db-core/src/main/java/org/veri/be/card/entity/Card.java
core/core-api/src/main/java/org/veri/be/book/dto/reading/response/ReadingDetailResponse.java
core/core-api/src/main/java/org/veri/be/card/controller/dto/response/CardDetailResponse.java
core/core-api/src/main/java/org/veri/be/card/service/CardCommandService.java
```

## Findings
- **Reading** owns a `List<Card>` and calls `Card.setPrivate()`, creating a book -> card dependency.
- **Card** holds a `Reading` reference and uses **ReadingRepository**, creating a card -> book dependency.
- DTOs in both modules access card and reading data across the boundary.

## Recommendation
- Decide a single module boundary owner for **Reading-Card** association, or introduce an abstraction (interface/DTO) to remove direct entity references.
- Until refactoring, keep **card** module **OPEN** to avoid cyclic Modulith violations.

## Update
**Date**: 2025-12-30
**Status**: Completed

### Resolution
- Removed **Reading -> Card** collection and book-side card access.
- Added **ReadingCardSummaryProvider** abstraction so card module supplies summaries and privacy updates.
- Book module no longer depends on card module, resolving the cycle.
