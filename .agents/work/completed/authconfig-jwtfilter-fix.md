# Plan: JwtFilter Constructor Fix

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Align JwtFilter constructor usage in AuthConfig with the current signature.
**Parent Task**: `.agents/work/completed/jwt-exception-handling.md`

## Steps
- [x] Remove obsolete MemberQueryService injection from AuthConfig.
- [x] Update JwtFilter construction to use TokenBlacklistStore and TokenProvider only.

## History
- **2025-12-21**: Updated **AuthConfig** to use the current **JwtFilter** constructor. Modified files: `src/main/java/org/veri/be/global/auth/AuthConfig.java`.
