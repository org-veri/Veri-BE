# Plan: Detailed Refactoring & Analysis

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Unify DTO conversions into static factory methods and harden utility classes to prevent instantiation.

## 1. DTO Conversion Unification
**Goal**: Eliminate standalone `Converter` classes to reduce class explosion and improve cohesion by moving mapping logic to DTOs.

### Analysis
*   **Identified Converters**:
    1.  `org.veri.be.domain.member.converter.MemberConverter`
    2.  `org.veri.be.domain.book.dto.reading.ReadingConverter`
    3.  `org.veri.be.domain.book.dto.book.BookConverter`
    4.  `org.veri.be.domain.card.controller.dto.CardConverter`
*   **Pattern**: These classes typically contain `public static Dto toDto(Entity e, ...)` methods.

### Execution Steps
For each Converter:
1.  **Move Logic**: Copy the static conversion method into the target DTO class.
    *   Rename to `from(Entity e)` or `of(Entity e, ...)` following standard naming conventions.
    *   Example: `MemberConverter.toMemberInfoResponse(...)` -> `MemberInfoResponse.from(...)`.
2.  **Update Callers**: Find all usages of the Converter class and replace them with the new DTO static method.
3.  **Delete**: Remove the empty `Converter` class file.

---

## 2. Utility Class Hardening
**Goal**: Enforce non-instantiability for static utility classes to prevent misuse.

### Analysis
*   **Good**: `AuthorizationHeaderUtil` uses Lombok `@NoArgsConstructor(access = AccessLevel.PRIVATE)`.
*   **To Check/Fix**:
    1.  `org.veri.be.global.storage.service.StorageUtil`
    2.  `org.veri.be.lib.auth.util.UrlUtil`
    3.  `org.veri.be.lib.time.SleepSupport` (Check if this is a utility or a base test class)
*   **Excluded**:
    *   `IntegrationTestSupport`, `PersistenceSliceTestSupport`: These are abstract base classes for inheritance, so they **must** have accessible constructors (protected/public).

### Execution Steps
1.  **Inspect**: specific target classes (`StorageUtil`, `UrlUtil`, `SleepSupport`).
2.  **Refactor**:
    *   If using Lombok: Add `@NoArgsConstructor(access = AccessLevel.PRIVATE)`.
    *   If plain Java: Add `private ClassName() { throw new UnsupportedOperationException("Utility class"); }`.
    *   Ensure class is `final` (optional but recommended).

---

## 3. Test Logic & Quality Review
**Goal**: Ensure tests are robust, logical, and free of common anti-patterns.

### Analysis & Checklist
*   **Logical Gaps**:
    *   Are we asserting the *result* of the operation?
    *   Example: `delete` test should verify repository count decreased or entity is not found.
*   **Potential Errors**:
    *   **N+1 in Tests**: `IntegrationTestSupport` sets up `MemberContext` with a saved Member. Does this trigger unnecessary lazy loading in every test? (We addressed this in Auth refactoring, but good to verify).
    *   **TearDown**: Is `MemberContext.clear()` consistently called? (Checked: Yes, in `@AfterEach`).
*   **Anti-Patterns**:
    *   Empty `@Test` methods.
    *   `assertTrue(true)` or `assertNotNull(result)` without inspecting fields.

### Execution Steps
1.  **Scan**: Review `src/test` for specific logical flaws.
2.  **Report**: Create a list of "Weak Tests" if any found.
3.  **Refactor**: Strengthen assertions (e.g., check specific field values instead of just presence).