## Todo

(각 단계를 마친 후에는 반드시 테스트를 최신화)

1. 불필요한 Converter 제거하고 dto 클래스쪽 static 팩토리 메서드로 일원화
2. 유틸리티 클래스들의 인스턴스화 방지
3. 인증 로직 안정성 개선
   (아래 세 가지 문제점을 비판적으로 검토하고, 실제로 지금 코드베이스에 알맞는 피드백인지 확인 후, 해당 사항이 맞다면 코드를 수정)

```md
   1. AOP 예외 처리의 위험성 (The Safety Issue)🛑

- 문제점: UseGuardsAspect에서 예외(ApplicationException)를 직접 catch하여 ApiData 객체를 리턴하고 있음.
  현재 당신의 코드 (Current)개선 제안 (Recommended)방식Aspect 내부에서 try-catch 후 return ApiData.error(...)예외를 throw하고,
  @RestControllerAdvice가 처리
- 이유"에러 응답이 잘 나가니까 된 것 아닌가?"라고 생각할 수 있음.타입 안정성 위반. 컨트롤러가 void나 String을 반환하면 런타임 에러 발생.

- 왜 바꿔야 하는가? (Why)
    - 타입 불일치 폭탄: 만약 컨트롤러 메서드가 void deleteUser()라면? Aspect는 ApiData 객체를 리턴하려 하는데, 메서드 시그니처는 void라 **ClassCastException**이
      터지며 서버 500 에러로 이어집니다.
    - 관심사 분리 위반: 에러 처리는 Global Exception Handler의 책임입니다. AOP는 "검사"만 하고, 실패 시 "신고(throw)"만 해야 유지보수가 쉽습니다.

2. Guard와 Context의 강결합 (The Testing Issue)🛑

- 문제점: Guard 클래스들이 AccessTokenUserContext.get...() 같은 Static 메서드를 직접 호출함.
  현재 당신의 코드 (Current)개선 제안 (Recommended)방식Optional<AccessTokenUser> member = AccessTokenUserContext.getAccessTokenUser()
  ;private final UserContextUtil userContextUtil; 주입 받아 사용
    - 이유 : 정적 메서드가 코드는 짧아서 편해 보임.
- 테스트 지옥. 단위 테스트 할 때마다 ThreadLocal을 set/clear 해줘야 함.

- 왜 바꿔야 하는가? (Why)테스트 고립성(Isolation) 파괴: ThreadLocal은 전역 변수와 같습니다. A 테스트가 세팅한 값이 B 테스트에 영향을 주어 **Flaky Test(간헐적 실패)**를
  만듭니다.

- Provider 패턴 미적용:
- 이미 훌륭한 UserContextUtil을 만들어두고선, 정작 Guard에서는 안 쓰고 있습니다. 이를 주입받아 쓰면 Mocking이 쉬워져 테스트 코드가 given(
  userContextUtil.isAuthenticated()).willReturn(true) 한 줄로 끝납니다.

3. UserContextUtil의 성능 및 로직 (The Performance Issue)🛑 문제점: 무조건적인 DB 조회(findById)와 예외를 이용한 제어 흐름(try-catch).

- 현재 당신의 코드 (Current)개선 제안 (Recommended)성능getCurrentUser() 호출 시 무조건 DB Select 발생getTokenUser()(DB X)와 getCurrentUser()(
  DB O) 분리로직isAuthenticated 확인을 위해 try-catch 사용Optional.isPresent()로 값 유무만 확인

[AOP]: UseGuardsAspect에서 try-catch를 제거하고 예외를 던지게 수정하세요.
[Guard]: MemberGuard 등에 UserContextUtil을 주입(@RequiredArgsConstructor)받아 정적 호출을 제거하세요.
[Util]: UserContextUtil에 getTokenUser()(DB 조회 없음) 메서드를 추가하고, isAuthenticated 등에서 불필요한 DB 조회를 제거하세요.
```

4. 테스트 코드 "상황 - 기대결과" 를 모두 점검하고, 논리적으로 문제가 있는 부분, 잠재적 오류 발생 가능성이 있는 연관관계 등 모두 점검
