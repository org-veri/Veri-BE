# Logic Review - 2025-12-21

**Reviewer**: Codex
**Scope**: IllegalArgumentException conversion to BadRequest

## Summary
- **IllegalArgumentException** is converted to **BadRequestException** in application code.
- **Global IllegalArgumentException handler** removed.

## Findings
- **Verified** Mock token endpoint catches IllegalArgumentException and throws **BadRequestException**.
- **Verified** global handler no longer handles IllegalArgumentException.

## Action Items
- **None**.

## Files Reviewed
```
src/main/java/org/veri/be/mock/MockTokenController.java
src/main/java/org/veri/be/lib/exception/handler/GlobalExceptionHandler.java
```
