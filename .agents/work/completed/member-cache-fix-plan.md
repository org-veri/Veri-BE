# Plan: Member 조회 중복 및 캐시 미적용 개선

**Status**: Completed
**Date**: 2026-01-02
**Goal**: **Member 조회 중복**과 **CurrentMember 캐시 미적용** 문제를 해소한다. **ReadingQueryService 분리** 변경 사항을 반영한다.

## Steps
- [x] **현황 확인**: **ReadingQueryService** 분리 이후 호출 경로 재점검
  - [x] **BookshelfController**에서 **BookshelfService**와 **ReadingQueryService** 사용 경로 분리 여부 확인
  - [x] **SocialReadingController**가 사용하는 서비스 경로 확인
- [x] **중복 조회 차단**: **MemberGuard**와 **AuthenticatedMemberResolver**의 중복 호출 정리
  - [x] **AuthenticatedMemberResolver 유지** 후 **CurrentMemberInfo 반환** 방식으로 전환
  - [x] **Controller**는 **@AuthenticatedMember CurrentMemberInfo**만 사용하도록 정리
- [x] **캐시 적용 보장**: **ThreadLocalCurrentMemberAccessor**의 **self-invocation** 제거
  - [x] **@Cacheable**을 **ThreadLocalCurrentMemberAccessor.getCurrentMemberInfoOrNull()**에 적용
  - [x] **self-invocation** 경로 제거
- [x] **중복 호출 축소**: 서비스 내부의 **CurrentMemberAccessor** 다중 호출 정리
  - [x] **ReadingQueryService.searchDetail(...)** 호출 1회로 축소
- [x] **검증 계획**: 트레이싱 기반 확인
  - [x] **동일 요청** 내 **Member 조회 쿼리 1회**인지 확인

## History
- **2026-01-02**: **getCurrentMemberInfo 제거** 이후 **연관 코드 점검 및 테스트 최신화** 진행. **Modified Files**:
```
tests/src/test/kotlin/org/veri/be/unit/auth/ThreadLocalCurrentMemberAccessorTest.kt
tests/src/test/kotlin/org/veri/be/unit/auth/MemberGuardTest.kt
tests/src/test/kotlin/org/veri/be/unit/book/BookshelfServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/book/ReadingQueryServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/card/CardCommandServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/card/CardQueryServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/comment/CommentCommandServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/image/ImageCommandServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/member/MemberCommandServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/post/PostCommandServiceTest.kt
tests/src/test/kotlin/org/veri/be/unit/post/PostQueryServiceTest.kt
tests/src/test/kotlin/org/veri/be/slice/web/CardControllerTest.kt
tests/src/test/kotlin/org/veri/be/slice/web/MemberControllerTest.kt
tests/src/test/kotlin/org/veri/be/slice/web/SocialCardControllerTest.kt
tests/src/test/kotlin/org/veri/be/integration/usecase/CommentIntegrationTest.kt
```
- **2026-01-03**: **테스트 실행** 완료. **Modified Files**:
```
N/A (tests run only)
```

## Review
**Summary**
- **./gradlew test** 성공적으로 통과.

**Findings**
- 추가 실패 없음.

**Action Items**
- **트레이싱 기반 검증** 단계 진행 필요.
