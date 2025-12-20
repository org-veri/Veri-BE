# Plan: Test Stabilization After Validation Updates

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Align tests and validation behavior so the test suite compiles and passes.
**Parent Task**: `.agents/work/completed/jwt-exception-handling.md`

## Steps
- [x] Fix validation gaps and controller checks causing 400/401 mismatches.
- [x] Update tests that rely on outdated CardConverter mocking or response fields.
- [x] Re-run tests and capture results.

## Progress
- **Updated**: Added validation for request DTOs and controller parameter validation.
- **Updated**: Fixed outdated tests for CardConverter usage and response fields.
- **Updated**: Adjusted auth logic to return BadRequest for missing tokens.
