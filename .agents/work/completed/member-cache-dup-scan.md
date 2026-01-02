# Plan: Member 조회 중복 가능 지점 조사

**Status**: Completed
**Date**: 2026-01-02
**Goal**: 회원 조회가 같은 요청에서 중복 발생할 수 있는 소스 지점을 식별하고 목록화한다.

## Steps
- [x] **Controller** 레벨에서 **@AuthenticatedMember** 사용 위치와 연관 흐름 확인
- [x] **CurrentMemberAccessor** 호출 경로 및 캐시 적용 여부 확인
- [x] **MemberRepository** 직접 호출 지점 확인
- [x] 결과 정리 및 경로별 중복 가능성 목록화

## Findings
- **MemberGuard** + **AuthenticatedMemberResolver** 조합 시 동일 요청 내 **Member 조회 2회** 가능
- **ThreadLocalCurrentMemberAccessor.getCurrentMemberInfo()**는 내부 호출로 인해 **캐시 프록시 비적용**
- **BookshelfService.searchDetail(...)** 내부에서 **CurrentMemberAccessor**를 2회 호출해 중복 조회 가능
- **@AuthenticatedMember** 사용 컨트롤러는 **resolver 단일 조회**가 기본이며, 서비스에서 추가 조회 시 중복 발생

## Review
**Summary**: 회원 조회 중복 가능 지점을 인증 가드/리졸버 및 서비스 호출 경로 중심으로 식별했다.

**Findings**:
- **MemberGuard**와 **AuthenticatedMemberResolver**가 동시에 동작할 때 **MemberRepository** 조회가 중복될 수 있다.
- **ThreadLocalCurrentMemberAccessor.getCurrentMemberInfo()**는 **self-invocation**으로 **@Cacheable**가 적용되지 않는다.
- **BookshelfService.searchDetail(...)**는 동일 요청 내 **CurrentMemberAccessor**를 2회 호출한다.

**Action Items**:
- **MemberGuard** 또는 **AuthenticatedMemberResolver** 중 하나를 **CurrentMemberInfo** 캐시 경로로 통일한다.
- **ThreadLocalCurrentMemberAccessor**의 캐시 적용 방식을 **프록시 호출 경로**로 수정한다.
- **BookshelfService.searchDetail(...)**의 중복 호출을 1회로 축소한다.

## History
- **2026-01-02**: **Member 조회 중복 가능 지점 조사** 완료. 중복 발생 경로 및 캐시 미적용 지점 정리. **Modified Files**:
```
.agents/work/member-cache-dup-scan.md
```
