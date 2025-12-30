# Plan: Modulith Phase 3 Storage Merge

**Status**: In Progress
**Date**: 2025-12-30
**Goal**: Remove the **storage** module by integrating repository/entity packages into **core** so package-private visibility can be enforced.

**Parent Task**:
```
.agents/work/backlog/modulith-migration.md
```

## Phase 1: Preparation
- [ ] **Map storage packages** (`storage/db-core`) to their destination packages under **core**.
- [ ] **Enumerate cross-module usage** to avoid breaking dependencies during the move.
- [ ] **Update module boundaries plan** for the new package layout.

## Phase 2: Migration
- [ ] **Move repository/entity classes** into core module packages.
- [ ] **Update imports/build scripts** to remove `storage` module references.
- [ ] **Adjust module declarations** for new package locations.

## Phase 3: Visibility Enforcement
- [ ] **Demote internal visibility** (repositories/services/DTOs) to package-private where safe.
- [ ] **Re-run Modulith verification** after visibility changes.

## History
 - **2025-12-30**: **Plan created**. Storage module integration planned to unblock visibility enforcement.
