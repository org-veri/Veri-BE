# Plan: IllegalArgumentException Handler Removal

**Status**: Completed
**Date**: 2025-12-21
**Goal**: Replace application-level IllegalArgumentException flows with BadRequest exceptions and remove the global handler.

## Steps
- [x] Locate application IllegalArgumentException usage.
- [x] Add try-catch to translate IllegalArgumentException to BadRequest in application code.
- [x] Remove IllegalArgumentException handler from the global exception handler.
- [x] Append history and move to completed.

## Progress
- **Updated**: Mock token controller now converts IllegalArgumentException to BadRequest.
- **Updated**: Removed IllegalArgumentException handler from global exception handler.

## History
- **2025-12-21 05:23:34**: Converted mock token IllegalArgumentException to **BadRequestException** and removed IllegalArgumentException handler from **GlobalExceptionHandler**. Modified files: `src/main/java/org/veri/be/mock/MockTokenController.java`, `src/main/java/org/veri/be/lib/exception/handler/GlobalExceptionHandler.java`.
