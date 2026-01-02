# Plan: Fix Test Failures After Cache Changes

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Resolve failing integration tests caused by cache SpEL evaluation errors.

## Steps
- [x] Identify failing test root causes from reports/logs
- [x] Patch cache annotations or logic causing runtime errors
- [x] Re-run tests to confirm fixes

## History
2026-01-02 22:39 | Fix Test Failures After Cache Changes | Updated cache annotation to avoid SpEL Optional mismatch and re-ran tests. | Modified files: `core/core-api/src/main/java/org/veri/be/global/auth/context/ThreadLocalCurrentMemberAccessor.java`

## Review
**Summary**
- **ThreadLocalCurrentMemberAccessor** cache annotation corrected to prevent SpEL `isEmpty()` errors during integration tests.
- Full test suite re-run succeeded after the fix.

**Findings**
- Root cause: Spring cache Optional handling unwraps to **CurrentMemberInfo**, so `#result.isEmpty()` caused `SpelEvaluationException`.

**Action Items**
- None.
