# Plan: Error Handling Update Alignment

## Metadata
- Status: Completed
- Date: 2025-12-23
- Goal: 최신 커밋의 에러 처리 변경을 확인하고 프로덕션 코드와 테스트를 빌드/테스트가 통과하도록 최신화.

## Steps
- [x] 최신 커밋에서 에러 처리 변경 사항 확인
- [x] 변경 사항에 맞춰 프로덕션 코드 수정 (컴파일 오류 제거)
- [x] 테스트 업데이트 및 전체 테스트 통과
- [x] 자체 리뷰 및 산출물 정리

## History
- **Timestamp**: 2025-12-23 01:36 KST
- **Task**: Error Handling Update Alignment
- **Summary**: 최신 에러 처리 방식에 맞게 예외/에러 코드 구조 정리 및 테스트 보정.
- **Modified Files**:
```
app/src/main/java/org/veri/be/api/common/AuthController.java
app/src/main/java/org/veri/be/api/personal/BookshelfController.java
app/src/main/java/org/veri/be/api/personal/CardController.java
app/src/main/java/org/veri/be/api/social/PostController.java
app/src/main/java/org/veri/be/domain/auth/service/AuthService.java
app/src/main/java/org/veri/be/domain/book/client/NaverBookSearchClient.java
app/src/main/java/org/veri/be/domain/book/controller/enums/ReadingSortType.java
app/src/main/java/org/veri/be/domain/book/entity/Reading.java
app/src/main/java/org/veri/be/domain/book/exception/BookErrorCode.java
app/src/main/java/org/veri/be/domain/book/exception/BookErrorInfo.java (deleted)
app/src/main/java/org/veri/be/domain/book/exception/ReadingErrorCode.java
app/src/main/java/org/veri/be/domain/book/exception/ReadingErrorInfo.java (deleted)
app/src/main/java/org/veri/be/domain/book/service/BookService.java
app/src/main/java/org/veri/be/domain/book/service/BookshelfService.java
app/src/main/java/org/veri/be/domain/card/controller/enums/CardSortType.java
app/src/main/java/org/veri/be/domain/card/entity/Card.java
app/src/main/java/org/veri/be/domain/card/exception/CardErrorCode.java
app/src/main/java/org/veri/be/domain/card/exception/CardErrorInfo.java (deleted)
app/src/main/java/org/veri/be/domain/card/service/CardCommandService.java
app/src/main/java/org/veri/be/domain/card/service/CardQueryService.java
app/src/main/java/org/veri/be/domain/comment/entity/Comment.java
app/src/main/java/org/veri/be/domain/comment/service/CommentQueryService.java
app/src/main/java/org/veri/be/domain/image/client/MistralOcrClient.java
app/src/main/java/org/veri/be/domain/image/exception/ImageErrorCode.java
app/src/main/java/org/veri/be/domain/image/exception/ImageErrorInfo.java (deleted)
app/src/main/java/org/veri/be/domain/image/service/ImageCommandService.java
app/src/main/java/org/veri/be/domain/image/service/MistralOcrService.java
app/src/main/java/org/veri/be/domain/member/entity/Member.java
app/src/main/java/org/veri/be/domain/member/exception/MemberErrorCode.java
app/src/main/java/org/veri/be/domain/member/exception/MemberErrorInfo.java (deleted)
app/src/main/java/org/veri/be/domain/member/service/MemberCommandService.java
app/src/main/java/org/veri/be/domain/member/service/MemberQueryService.java
app/src/main/java/org/veri/be/domain/post/controller/enums/PostSortType.java
app/src/main/java/org/veri/be/domain/post/entity/Post.java
app/src/main/java/org/veri/be/domain/post/service/PostCommandService.java
app/src/main/java/org/veri/be/domain/post/service/PostQueryService.java
app/src/main/java/org/veri/be/global/auth/AuthErrorCode.java
app/src/main/java/org/veri/be/global/auth/AuthErrorInfo.java (deleted)
app/src/main/java/org/veri/be/global/auth/context/AuthenticatedMemberResolver.java
app/src/main/java/org/veri/be/global/auth/context/CurrentMemberAccessor.java
app/src/main/java/org/veri/be/global/auth/context/MemberContext.java
app/src/main/java/org/veri/be/global/auth/guards/MemberGuard.java
app/src/main/java/org/veri/be/global/auth/oauth2/CustomAuthFailureHandler.java
app/src/main/java/org/veri/be/global/auth/oauth2/dto/OAuth2UserInfoMapper.java
app/src/main/java/org/veri/be/global/auth/token/JwtExceptionHandlingTokenProvider.java
app/src/main/java/org/veri/be/lib/auth/jwt/TokenErrorCode.java
app/src/main/java/org/veri/be/lib/auth/jwt/TokenErrorInfo.java (deleted)
app/src/main/java/org/veri/be/lib/exception/ApplicationException.java
app/src/main/java/org/veri/be/lib/exception/CommonErrorCode.java
app/src/main/java/org/veri/be/lib/exception/ErrorCodeException.java (deleted)
app/src/main/java/org/veri/be/lib/exception/handler/GlobalExceptionHandler.java
app/src/main/java/org/veri/be/mock/MockTokenController.java
tests/src/test/java/org/veri/be/integration/usecase/SocialReadingIntegrationTest.java
tests/src/test/java/org/veri/be/support/assertion/ExceptionAssertions.java
tests/src/test/java/org/veri/be/unit/auth/CurrentMemberAccessorTest.java
tests/src/test/java/org/veri/be/unit/auth/CustomAuthFailureHandlerTest.java
tests/src/test/java/org/veri/be/unit/auth/MemberContextTest.java
tests/src/test/java/org/veri/be/unit/auth/MemberGuardTest.java
tests/src/test/java/org/veri/be/unit/auth/TokenErrorInfoTest.java
tests/src/test/java/org/veri/be/unit/book/BookServiceTest.java
tests/src/test/java/org/veri/be/unit/book/BookshelfServiceTest.java
tests/src/test/java/org/veri/be/unit/book/BookServiceTest.java
tests/src/test/java/org/veri/be/unit/book/NaverBookSearchClientTest.java
tests/src/test/java/org/veri/be/unit/book/ReadingSortTypeTest.java
tests/src/test/java/org/veri/be/unit/book/ReadingTest.java
tests/src/test/java/org/veri/be/unit/card/CardCommandServiceTest.java
tests/src/test/java/org/veri/be/unit/card/CardQueryServiceTest.java
tests/src/test/java/org/veri/be/unit/card/CardSortTypeTest.java
tests/src/test/java/org/veri/be/unit/card/CardTest.java
tests/src/test/java/org/veri/be/unit/comment/CommentQueryServiceTest.java
tests/src/test/java/org/veri/be/unit/comment/CommentTest.java
tests/src/test/java/org/veri/be/unit/common/exception/ApplicationExceptionTest.java
tests/src/test/java/org/veri/be/unit/image/ImageCommandServiceTest.java
tests/src/test/java/org/veri/be/unit/image/MistralOcrClientTest.java
tests/src/test/java/org/veri/be/unit/image/MistralOcrServiceTest.java
tests/src/test/java/org/veri/be/unit/member/MemberCommandServiceTest.java
tests/src/test/java/org/veri/be/unit/member/MemberEntityTest.java
tests/src/test/java/org/veri/be/unit/member/MemberQueryServiceTest.java
tests/src/test/java/org/veri/be/unit/post/PostCommandServiceTest.java
tests/src/test/java/org/veri/be/unit/post/PostQueryServiceTest.java
tests/src/test/java/org/veri/be/unit/post/PostSortTypeTest.java
tests/src/test/java/org/veri/be/unit/post/PostTest.java
```
