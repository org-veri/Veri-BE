# Plan: Refactoring Suggestions

**Status**: In Progress
**Date**: 2025-12-21
**Goal**: Systematically address security risks, bug fixes, and configuration issues identified during initial review.

## 1. Security Hardening (Priority: Critical)
- [ ] **Externalize Secrets**: (ON HOLD) Move `access-key` and any other credentials from `application-*.yml` to Environment Variables.
- [ ] **Rotate Keys**: (ON HOLD) Assume current keys are compromised if they were pushed to any shared repository.

## 2. Bug Fixes (Priority: High)
- [x] **Global Validation Fix**: Apply `@Valid` annotation to all `@RequestBody` parameters in:
    - `CardController`
    - `AuthController`
    - `PostController`
    - `CommentController`
    - `MemberController`
    - `BookshelfController` (check `addBook` method)

## 3. Configuration Review
- [x] **Verify Dependencies**: Confirmed that Java 25 and Spring Boot 4.0.0 are intended as per the latest system overview.
- [x] **OSIV**: Explicitly disabled OSIV in both main and test configurations. Verified that no `LazyInitializationException` occurs in the comprehensive integration test suite.

## 4. Code Quality
- [x] **Test Coverage**: Added Controller Slice Tests and Integration Tests to cover DTO validation constraints (e.g., `@Min`, `@NotNull`, `@URL`). All 70+ test scenarios pass.

## History
- **2025-12-21**: Migrated document into **.agents/work** structure.
