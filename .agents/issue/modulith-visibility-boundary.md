# Issue: Visibility Enforcement Blocked by Package Layout

**Severity**: Low
**Status**: Open
**Date**: 2025-12-30

## Description
Attempting to apply package-private visibility across repositories/services is blocked by package layout. Repositories live in `storage/db-core` packages while services/controllers live in `core/core-api` packages, so package-private visibility would break access even within the same module.

## Affected Files
- **storage/db-core/src/main/java/org/veri/be/**/repository/**
- **core/core-api/src/main/java/org/veri/be/**/service/**

## Findings
- Example: **CardRepository** is used only by card services, but it resides in a different package tree, so `package-private` is not viable without refactoring package structure.

## Recommendation
- Consider co-locating repositories with services under the same package, or introduce module-private access patterns that do not rely on Java package visibility.
- Defer visibility demotion until package layout decisions are made.
