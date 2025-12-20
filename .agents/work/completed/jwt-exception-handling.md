# Plan: Jwt Exception Handling Shift

**Status**: Completed
**Date**: 2025-12-21
**Source Plan**: **jwt-exception-handling.md**

## Requirements (From Plan)
- **Review current JWT usage and global exception handling.**
- **Add JWT exception translation at usage sites (auth filter/service).**
- **Update configuration to use JWT handling with runtime exceptions.**
- **Capture review notes and log completion.**

## Progress
- **Started**: Plan and task workspace created.

## Progress Update
- **Updated**: Added a token provider wrapper to translate JWT parsing errors into **UnAuthorizedException** without touching lib.
- **Pending**: Confirm whether the JWT handler removal from **GlobalExceptionHandler** should be skipped due to lib restriction.

## Progress Update
- **Updated**: Removed JWT-specific handler from **GlobalExceptionHandler** per requirement.
- **Status**: Ready for review and closure.

## Plan (Legacy)
# Plan: Jwt Exception Handling Shift

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Move JWT error handling to usage sites and avoid global JWT exception handling.

## Steps
- [x] Review current JWT usage and global exception handling.
- [x] Add JWT exception translation at usage sites (auth filter/service).
- [x] Update configuration to use JWT handling with runtime exceptions.
- [x] Capture review notes and log completion.

## History
- **2025-12-21 05:13:58**: Added TokenProvider wrapper to translate JWT parsing errors into **UnAuthorizedException** and removed JWT handler from **GlobalExceptionHandler**. Modified files: `src/main/java/org/veri/be/global/auth/token/JwtExceptionHandlingTokenProvider.java`, `src/main/java/org/veri/be/global/auth/token/TokenProviderConfig.java`, `src/main/java/org/veri/be/lib/exception/handler/GlobalExceptionHandler.java`.
