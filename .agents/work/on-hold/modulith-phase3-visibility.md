# Plan: Modulith Phase 3 Visibility Enforcement

**Status**: On Hold
**Date**: 2025-12-30
**Goal**: Reduce public surface area by making internal types package-private where possible while preserving module boundaries.

**Parent Task**:
```
.agents/work/backlog/modulith-migration.md
```

## Phase 1: Inventory
- [x] **List repositories/services/DTOs** per module that are candidates for package-private visibility.
- [x] **Trace usage** across modules to avoid breaking dependencies.

## Phase 2: Implementation
- [ ] **Demote visibility** for internal types that are only used inside their module.
- [ ] **Adjust package layouts** if required to keep access within module boundaries.

## Phase 3: Verification
- [ ] **Run Modulith verification** after visibility changes.

## History
- **2025-12-30**: **Phase 1 inventory started**. Identified package layout blocker for repository visibility and logged issue.
  - **Issue**:
    ```
    .agents/issue/modulith-visibility-boundary.md
    ```
- **2025-12-30**: **Phase 1 inventory completed**. Package boundaries prevent package-private demotion without reorganizing `storage` vs `core` packages; task moved **On Hold** pending decision.
