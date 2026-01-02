# Plan: Remove Converters and Use DTO Factories

**Status**: Completed
**Date**: 2026-01-02
**Goal**: Eliminate converter classes and OAuth2UserInfoMapper, refactor call sites to use DTO static factory methods.

## Steps
- [x] Identify converter and mapper usage sites
- [x] Refactor services/controllers to use DTO static factories
- [x] Update or replace tests relying on converter classes
- [x] Remove converter and mapper classes
- [x] Run tests to confirm changes

## History
2026-01-02 23:16 | Remove Converters and Use DTO Factories | Replaced converter/mapper usage with static DTO factories, removed converter/mapper classes, updated tests. | Modified files: `core/core-api/src/main/java/org/veri/be/global/auth/oauth2/dto/OAuth2UserInfo.java`, `core/core-api/src/main/java/org/veri/be/global/auth/oauth2/CustomOAuth2SuccessHandler.java`, `core/core-api/src/main/java/org/veri/be/domain/book/service/BookService.java`, `core/core-api/src/main/java/org/veri/be/domain/book/service/BookshelfService.java`, `core/core-api/src/main/java/org/veri/be/domain/card/service/CardQueryService.java`, `core/core-api/src/main/java/org/veri/be/domain/card/service/CardCommandService.java`, `core/core-api/src/main/java/org/veri/be/domain/member/service/MemberQueryService.java`, `core/core-api/src/main/java/org/veri/be/domain/book/dto/book/BookConverter.java`, `core/core-api/src/main/java/org/veri/be/domain/book/dto/reading/ReadingConverter.java`, `core/core-api/src/main/java/org/veri/be/domain/member/converter/MemberConverter.java`, `core/core-api/src/main/java/org/veri/be/domain/card/controller/dto/CardConverter.java`, `core/core-api/src/main/java/org/veri/be/global/auth/oauth2/dto/OAuth2UserInfoMapper.java`, `tests/src/test/kotlin/org/veri/be/unit/book/BookConverterTest.kt`, `tests/src/test/kotlin/org/veri/be/unit/member/MemberConverterTest.kt`, `tests/src/test/kotlin/org/veri/be/unit/book/ReadingConverterTest.kt`, `tests/src/test/kotlin/org/veri/be/unit/book/BookshelfServiceTest.kt`, `tests/src/test/kotlin/org/veri/be/unit/card/CardResponseMappingTest.kt`, `tests/src/test/kotlin/org/veri/be/unit/card/CardQueryServiceTest.kt`, `tests/src/test/kotlin/org/veri/be/unit/auth/OAuth2UserInfoMapperTest.kt`

## Review
**Summary**
- Removed converter and mapper classes and switched to DTO static factories.
- Adjusted services and tests to align with new mapping locations.

**Findings**
- None.

**Action Items**
- None.
