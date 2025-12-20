# Issue: Missing DTO Validation in Controllers

**Severity**: High
**Status**: Closed
**Date**: 2025-12-21

## Description
The `CardController` accepts request bodies (DTOs) that contain validation annotations (e.g., `@Min`, inferred from imports), but the controller methods accept these DTOs without the `@Valid` or `@Validated` annotation.

Without `@Valid`, the constraints defined in the DTO (like `@NotNull`, `@Size`, `@Min`) are **ignored** by Spring, allowing invalid data to enter the business logic.

## Affected Files
*   `src/main/java/org/veri/be/api/personal/CardController.java`
*   `src/main/java/org/veri/be/api/common/AuthController.java`
*   `src/main/java/org/veri/be/api/social/PostController.java`
*   `src/main/java/org/veri/be/api/social/CommentController.java`
*   `src/main/java/org/veri/be/api/personal/MemberController.java`
*   `src/main/java/org/veri/be/api/personal/BookshelfController.java` (Partially affected)

## Example
In `CardController.java` and others:
```java
public ApiResponse<CardCreateResponse> createCard(
    @RequestBody CardCreateRequest request, // <--- Missing @Valid here
    @AuthenticatedMember Member member)
```

## Recommendation
Add `@Valid` to all `@RequestBody` parameters in Controllers.

```java
public ApiResponse<CardCreateResponse> createCard(
    @Valid @RequestBody CardCreateRequest request, // <--- Added @Valid
    @AuthenticatedMember Member member)
```
