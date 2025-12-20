# RequestParam vs ModelAttribute 밸리데이션 실무 패턴 정리

**네, 맞습니다. 실무적으로나 유지보수 관점에서 DTO(`@ModelAttribute`)를 사용하는 패턴이 훨씬 권장됩니다.**

단순히 파라미터가 하나(`nickname`)라서 `@RequestParam`이 편해 보일 수 있지만, **유효성 검증(Validation)**과 **확장성** 측면에서는 DTO 방식이 확실한 장점이 있습니다.

이유를 3가지로 정리해 드립니다.

---

### 1. 검증 로직의 응집성 (Cohesion)

* **`@RequestParam` 사용 시:** 컨트롤러 메소드 파라미터 옆에 `@Size`, `@Pattern` 등이 지저분하게 붙습니다. 또한 컨트롤러 클래스 상단에 `@Validated`를 붙여야 동작합니다.
* **DTO(`@ModelAttribute`) 사용 시:** 검증 규칙이 DTO 클래스 안에 정의되므로, **"어떤 데이터가 유효한지"에 대한 정의가 한곳(DTO)에 모입**니다. 컨트롤러는 깔끔해집니다.

### 2. 예외 처리의 일관성 (Exception Handling)

이게 가장 큰 기술적 차이입니다.

* **DTO (`@Valid` 사용)**: 유효성 검증 실패 시 `BindException` (GET 방식) 또는 `MethodArgumentNotValidException` (POST 방식)이 발생합니다. 전역
  예외 처리기(`@RestControllerAdvice`)에서 처리하기가 표준화되어 있어 매우 편합니다.
* **`@RequestParam` (`@Validated` 사용)**: 유효성 검증 실패 시 `ConstraintViolationException`이 발생합니다. 이는 JPA 등 다른 레이어의 검증 예외와 섞일 수
  있어, 예외 처리기를 따로 구분해서 짜야 하는 번거로움이 있습니다.

### 3. 확장성

나중에 "닉네임 중복 확인 시 **이메일도 같이 확인**해야 한다"거나 "특정 **약관 동의 여부**도 필요하다"는 식으로 요구사항이 늘어날 때, DTO 방식은 필드만 추가하면 되지만 `@RequestParam`은
메소드 시그니처 자체를 뜯어고쳐야 합니다.

---

### ✅ 추천 패턴: DTO 도입 코드

**1. Request DTO 생성**

```java
@Getter
@Setter
@NoArgsConstructor
public class CheckNicknameRequest {
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "특수문자는 사용할 수 없습니다.")
    private String nickname;
}

```

**2. Controller 수정**

```java
@Operation(summary = "닉네임 중복 확인")
@GetMapping("/nickname/exists")
public ApiResponse<Boolean> checkNicknameExists(
    @Valid @ModelAttribute CheckNicknameRequest request // @Valid로 검증 수행
) {
    boolean exists = memberQueryService.existsByNickname(request.getNickname());
    return ApiResponse.ok(exists);
}

```

### 요약

지금 당장은 파라미터가 1개라서 `@RequestParam`도 나쁘지 않지만, **프로젝트 전체의 예외 처리 전략 통일**과 **깔끔한 코드**를 원하신다면 *
*DTO(`@ModelAttribute`) + `@Valid` 조합**을 사용하는 것이 Best Practice입니다.

혹시 `BindException`을 잡아서 에러 메시지를 예쁘게 내려주는 **GlobalExceptionHandler** 코드도 필요하신가요?

# PathVariable 밸리데이션 실무 패턴 정리

실무에서 **`@PathVariable`**은 **'자원의 식별자(Identity)'** 역할을 하기 때문에, DTO(`@ModelAttribute`, `@RequestBody`)와는 밸리데이션을 대하는 관점이 조금
다릅니다.
결론부터 말씀드리면, **"형식(Format) 검증은 프레임워크에 맡기고, 실질적인 검증(존재 여부/권한)은 서비스 레이어에서 수행"**하는 것이 일반적인 패턴입니다. 아예 배제하는 것은 아니지만, DTO처럼 복잡한
어노테이션을 덕지덕지 붙이지는 않습니다.
이유와 실무 패턴을 정리해 드립니다.

---

### 1. 밸리데이션에서 '배제'해도 되는가?

**"형식 검증(Type Check)"은 이미 Spring이 강력하게 해주고 있습니다.**

`@PathVariable`은 주로 DB의 PK(Long id)나 UUID를 받습니다.

```java

@GetMapping("/members/{memberId}")
public ApiResponse<MemberRes> getMember(@PathVariable Long memberId) { ...}

```

* **상황:** 사용자가 `/members/abc`라고 요청을 보냄.
* **결과:** Spring이 `String` "abc"를 `Long`으로 변환하려다 실패하여 `TypeMismatchException`을 발생시킵니다.
* **실무:** 이 자체가 가장 강력한 1차 밸리데이션입니다. 따라서 별도로 `@Pattern(regexp="^[0-9]*$")` 같은 것을 붙일 필요가 없습니다.

### 2. 실무에서 `@PathVariable`을 다루는 3단계 패턴

#### 1단계: 최소한의 방어 (Controller 레벨)

정말 이상한 값(음수 ID 등)만 막고 싶다면, 클래스 상단에 `@Validated`를 붙이고 파라미터에 제약 조건을 겁니다. 하지만 **잘 안 씁니다.**

```java
@Validated // 이게 있어야 동작함
@RestController
public class MemberController {
    @GetMapping("/members/{id}")
    public ApiResponse getMember(@PathVariable @Positive Long id) { ... } // 양수만 허용
}

```

* **비추천 이유:** ID가 음수로 들어오는 경우는 해킹 시도 외에는 거의 없으며, 어차피 DB 조회 시 없으면 예외가 터지기 때문에 굳이 컨트롤러에서 잡지 않는 경우가 많습니다.

#### 2단계: 존재 여부 확인 (Service 레벨 - 가장 중요)

`@PathVariable`로 받은 ID는 **"DB에 실제로 존재하는가?"**가 밸리데이션의 핵심입니다. 이건 어노테이션으로 못 하고 로직으로 풀어야 합니다.

```java
// Service
public MemberResponse getMember(Long id) {
    return memberRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다.")); // 404 Not Found
}

```

* **실무 관점:** `@PathVariable` 값의 오류는 **400 Bad Request**보다는 **404 Not Found**로 귀결되는 것이 자연스럽습니다.

#### 3단계: 권한 검증 (Security 레벨 - IDOR 방지)

이게 실무에서 가장 위험하고 중요한 부분입니다. **"존재하는 ID인가?"**보다 **"이 사용자가 이 ID를 조회할 자격이 있는가?"**를 검증해야 합니다.

* **취약점 예시:** 내가 1번 유저인데 `/members/2`를 호출해서 남의 정보를 봄.
* 이건 `@Valid` 같은 걸로 막을 수 없고, Spring Security나 비즈니스 로직에서 막아야 합니다.

---

### 3. 질문하신 `nickname`의 경우 (`/members/{nickname}`)

만약 닉네임을 경로 변수(Path Variable)로 쓴다면 이야기가 조금 다릅니다.

* `Long`이 아니라 `String`이므로 타입 미스매치 예외가 발생하지 않습니다.
* 특수문자나 길이 제한이 필요할 수 있습니다.

**이 경우 추천 패턴:**

1. **컨트롤러 검증 최소화:** 그냥 String으로 받고, 서비스로 넘깁니다.
2. **서비스에서 검증:** 서비스에서 조회(`findByNickname`)를 시도하고, 없으면 예외를 던집니다.
3. **이유:** URL 경로에 이상한 특수문자나 공백을 넣으면, 애초에 톰캣이나 앞단의 Nginx/AWS ALB 등에서 막히거나 인코딩 문제가 발생합니다. 따라서 컨트롤러에서 정규식으로 빡빡하게
   검사하기보다는, **"조회해 봤는데 없더라(404)"**로 처리하는 게 깔끔합니다.

### 요약

1. **`@PathVariable`은 DTO처럼 `@Valid`를 빡빡하게 걸지 않아도 됩니다.**
2. **타입 변환(Long, UUID)** 과정에서 1차로 걸러지는 것으로 충분합니다.
3. 값의 유효성보다는 **"해당 리소스가 존재하는가(404)"**와 **"접근 권한이 있는가(403)"**를 체크하는 것이 실무의 핵심입니다.

**결론:**
`@PathVariable`에는 `@Min(1)` 같은 어노테이션을 붙이기 위해 애쓰지 마시고, **타입(Long/UUID)만 명확히 지정**한 뒤 **서비스 레이어의 `orElseThrow**` 패턴에 맡기셔도
충분합니다.
