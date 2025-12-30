# Issue: Visibility Enforcement Blocked by Package Layout

**Severity**: Low
**Status**: On Hold
**Date**: 2025-12-30

## Description
Attempting to apply package-private visibility across repositories/services is blocked by package layout. Repositories live in `storage/db-core` packages while services/controllers live in `core/core-api` packages, so package-private visibility would break access even within the same module.

## Affected Files
- **storage/db-core/src/main/java/org/veri/be/**/repository/**
- **core/core-api/src/main/java/org/veri/be/**/service/**

## Findings
- Example: **CardRepository** is used only by card services but lives in `storage/db-core` (`org.veri.be.card.repository`), while services live in `core/core-api` (`org.veri.be.card.service`). Java package-private visibility requires the *same package*, not just the same module.
- The same pattern repeats across modules (`comment`, `post`, `book`, `member`), where repositories/entities are in `storage` and services/controllers are in `core`, preventing package-private access without moving code.
- Module boundaries do not relax Java access rules; Spring Modulith only enforces dependencies, it does not grant access across packages.

## Recommendation
- Consider co-locating repositories with services under the same package (or move repositories into the core module package) so package-private can apply.
- Alternatively, introduce module-private access patterns (e.g., internal interfaces in the same package, or move orchestration into storage module) instead of relying on Java visibility.
- Defer visibility demotion until package layout decisions are made.
