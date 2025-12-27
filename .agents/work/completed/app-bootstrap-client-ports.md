# Plan: App Bootstrap and Client Port Extraction

## Metadata
- **Status**: Completed
- **Date**: 2025-12-27
- **Goal**: Move boot class to app module, keep core-api business logic, and move client implementations out of core-api behind port interfaces.
- **Parent Task**: `.agents/work/backlog/modulith-migration.md`

## Steps
- [x] Move main application class to **core-app** and update build scripts
- [x] Define port interfaces in **core-api** and move client implementations to client modules
- [x] Update module dependencies, Dockerfile/makefile, and tests
- [x] Verify build/test

## History
2025-12-27 - App Bootstrap and Client Port Extraction - Moved boot class to **core-app**, moved client implementations to client modules, added **OcrPort** and storage DTOs/ports in **core-api**, updated dependencies and scripts, and verified full `clean test build` - Modified Files: `core/core-app/src/main/java/org/veri/be/CoreApiApplication.java`, `core/core-app/build.gradle.kts`, `core/core-api/build.gradle.kts`, `core/core-api/src/main/java/org/veri/be/domain/image/client/OcrPort.java`, `core/core-api/src/main/java/org/veri/be/domain/image/service/MistralOcrService.java`, `core/core-api/src/main/java/org/veri/be/domain/image/dto/OcrResult.java`, `core/core-api/src/main/java/org/veri/be/global/storage/service/StorageService.java`, `core/core-api/src/main/java/org/veri/be/global/storage/dto/PresignedPostFormResponse.java`, `core/core-api/src/main/java/org/veri/be/global/storage/service/StorageConstants.java`, `core/core-api/src/main/java/org/veri/be/global/storage/service/StorageUtil.java`, `core/core-api/src/main/java/org/veri/be/global/storage/dto/PresignedUrlRequest.java`, `core/core-api/src/main/java/org/veri/be/global/storage/dto/PresignedUrlResponse.java`, `core/core-api/src/main/java/org/veri/be/domain/book/dto/book/NaverBookItem.java`, `core/core-api/src/main/java/org/veri/be/domain/book/dto/book/NaverBookResponse.java`, `core/core-api/src/main/java/org/veri/be/domain/book/client/NaverClientException.java`, `clients/client-aws/build.gradle.kts`, `clients/client-aws/src/main/java/org/veri/be/global/storage/service/AwsStorageService.java`, `clients/client-ocr/build.gradle.kts`, `clients/client-ocr/src/main/java/org/veri/be/domain/image/client/MistralOcrAdapter.java`, `clients/client-search/build.gradle.kts`, `clients/client-search/src/main/java/org/veri/be/domain/book/client/NaverBookClient.java`, `Dockerfile`, `makefile`, `tests/build.gradle.kts`, `tests/src/test/java/org/veri/be/unit/image/MistralOcrServiceTest.java`, `tests/src/test/java/org/veri/be/integration/SharedTestConfig.java`, `tests/src/test/java/org/veri/be/integration/support/stub/StubStorageService.java`, `tests/src/test/java/org/veri/be/slice/web/CardControllerV2Test.java`, `tests/src/test/java/org/veri/be/unit/card/CardCommandServiceTest.java`, `tests/src/test/java/org/veri/be/unit/global/storage/AwsStorageServiceTest.java`
