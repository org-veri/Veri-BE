# Plan: Storage Split vs Modulith Encapsulation

**Status**: Completed
**Date**: 2025-12-30
**Goal**: Document the current conflict (physical module split vs modulith encapsulation) and evaluate two resolution plans with concepts and trade-offs.

## Context
- **Current Issue**: Repositories live in `storage/db-core` while services/controllers live in `core/core-api`. Java `package-private` visibility requires the same package, so visibility enforcement across these physical modules is blocked.
- **Conflict**: **Modular Monolith encapsulation** favors tight, package-level access control, while **physical module split** for storage favors reuse and separation.

## Plan A: Public Repositories + ArchUnit Guard
**Concept**: Keep repositories `public` to satisfy Java compilation across modules, then enforce access rules via **ArchUnit** tests (e.g., only services in the same domain may access repositories).

**Pros**
- Preserves **storage module separation** and potential reuse.
- Minimal file movement; lower refactor risk.
- Enforces architecture at **build time** without reworking packages.

**Cons**
- Encapsulation relies on **tests**, not the compiler.
- **ArchUnit rule complexity** increases with real usage patterns:
  - **Example**: A shared query adapter (e.g., `SearchIndexRepository`) used by both **post** and **book** requires explicit allow-lists or a separate “shared” module, otherwise the rule blocks it.
  - **Example**: Controller-level projections or DTO mappers inside `core` might access repositories for read-only optimizations; rules must distinguish **service-only access** vs **read-model access** or risk false positives.
  - **Example**: Test fixtures or integration tests that access repositories directly must be explicitly excluded, otherwise rules fail on test packages.
  - **Example**: Batch jobs or scheduled tasks may live outside the module’s `service` package but legitimately use repositories, forcing additional rule exceptions.
- Misconfigured tests can allow unintended access.

## Plan B: Merge Storage into Core
**Concept**: Move storage entities/repositories into core packages so Java `package-private` can enforce encapsulation directly. Remove `storage:db-core` module.

**Pros**
- **Compiler-enforced** encapsulation via package-private access.
- Modulith boundaries align cleanly with package structure.
- Reduced cross-module wiring complexity.

**Cons**
- Larger refactor (file moves, Gradle updates).
- **Storage module reuse loss** with concrete impacts:
  - **Example**: If another service wants to reuse `storage/db-core` entities/repositories (e.g., a batch worker or reporting service), it can no longer depend on a standalone storage module and must pull the entire **core** module.
  - **Example**: Shared persistence utilities (e.g., auditing base entities) become coupled to core runtime, reducing portability for future components.
  - **Example**: Isolating persistence for tests or lightweight tools becomes harder because repositories are now tied to core beans/config.
- Higher change risk and migration cost.

## Recommendation Notes
- If **reuse** and **physical separation** are priority → Plan A.
- If **strict encapsulation** and **compiler enforcement** are priority → Plan B.

## History
- **2025-12-30**: Documented conflict and two resolution plans.
 - **2025-12-30**: **Completed**. Plan B executed with storage integration and visibility enforcement.
   - **Reference**:
     ```
     .agents/work/completed/modulith-phase3-storage-merge.md
     ```

### SELECTED (Plan B)
Decision: Merge Storage into Core (Plan B)

Rationale
1. Encapsulation Priority: We prioritize strict, compiler-enforced encapsulation (package-private) over physical module separation.
2. Cost vs Benefit: The overhead of managing cross-module mapping/wiring outweighs the current need for independent storage module reuse.
3. JPA Abstraction (New): Since we are sticking with JPA, physical separation offers no technical advantage for DB switching (MySQL ↔ PostgreSQL), as JPA handles this abstraction. Separation is only useful for paradigm shifts (e.g., to NoSQL), which is not in our scope.

Action Items
1. [Tool] Install IntelliJ Spring Modulith Plugin.
2. [Gradle] Move dependencies (JPA, DB drivers) from `storage:db-core` to `core`.
3. [FileSystem] Move all Entity/Repository classes to `core` source tree.
4. [Cleanup] Delete `storage:db-core` module.
5. [Refactor] Demote visibility (public → package-private).

분석 / 결정 근거 문서는 storage-module-review.md 파일을 참고
