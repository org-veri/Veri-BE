# Result: Test Improvement Audit - Phase 1 Kickoff

- **Status**: In Progress
- **Date**: 2026-01-03
- **Source Request**: 테스트 코드 현황 파악 및 개선 포인트 분석, 계획 검토 및 개선 작업 시작

## [Audit Summary]
- **Controller 테스트**: **MemberControllerTest**에서 **MockMvc 설정/JSON 직렬화 중복**을 확인하고 **ControllerTestSupport** 기반으로 리팩토링 시작.
- **단위 테스트**: **MemberCommandServiceTest**, **MemberQueryServiceTest**에서 **Fixture/Custom Assert 부재** 및 **verify() 사용**을 확인하고 **MemberFixture/MemberAssert** 및 **then().should()**로 개선.
- **지원 인프라**: **ControllerTestSupport**, **MemberFixture**, **MemberAssert**를 추가하여 향후 전체 테스트 스위트 개선의 기반을 마련.

## [Refactored Code]
- **tests/src/test/kotlin/org/veri/be/support/ControllerTestSupport.kt**
```kotlin
package org.veri.be.support

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

open class ControllerTestSupport {
    protected lateinit var mockMvc: MockMvc
    protected val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()

    // Refactor: MockMvc JSON 요청 반복 코드를 헬퍼로 통합
    protected fun postJson(url: String, request: Any): ResultActions {
        return mockMvc.perform(
            post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    // Refactor: PUT 요청의 JSON 직렬화/Content-Type을 공통화
    protected fun putJson(url: String, request: Any): ResultActions {
        return mockMvc.perform(
            put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    // Refactor: PATCH 요청의 JSON 직렬화/Content-Type을 공통화
    protected fun patchJson(url: String, request: Any): ResultActions {
        return mockMvc.perform(
            patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    // Refactor: GET 파라미터 반복 설정을 단순화
    protected fun get(url: String, params: Map<String, String> = emptyMap()): ResultActions {
        val builder = get(url)
        params.forEach { (key, value) -> builder.param(key, value) }
        return mockMvc.perform(builder)
    }

    protected fun delete(url: String): ResultActions {
        return mockMvc.perform(delete(url))
    }
}
```

- **tests/src/test/kotlin/org/veri/be/support/fixture/MemberFixture.kt**
```kotlin
package org.veri.be.support.fixture

import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType

object MemberFixture {
    // Refactor: 테스트 데이터 생성 시 필수 필드 기본값을 제공
    fun aMember(): Member.MemberBuilder<*, *> {
        return Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
    }
}
```

- **tests/src/test/kotlin/org/veri/be/support/assertion/MemberAssert.kt**
```kotlin
package org.veri.be.support.assertion

import org.assertj.core.api.Assertions.assertThat
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType

class MemberAssert private constructor(
    private val actual: Member
) {
    companion object {
        fun assertThat(actual: Member): MemberAssert {
            return MemberAssert(actual)
        }
    }

    // Refactor: 반복되는 필드 검증을 체이닝 형태로 추출
    fun hasId(expected: Long): MemberAssert {
        assertThat(actual.id).isEqualTo(expected)
        return this
    }

    fun hasEmail(expected: String): MemberAssert {
        assertThat(actual.email).isEqualTo(expected)
        return this
    }

    fun hasNickname(expected: String): MemberAssert {
        assertThat(actual.nickname).isEqualTo(expected)
        return this
    }

    fun hasProfileImageUrl(expected: String): MemberAssert {
        assertThat(actual.profileImageUrl).isEqualTo(expected)
        return this
    }

    fun hasProviderType(expected: ProviderType): MemberAssert {
        assertThat(actual.providerType).isEqualTo(expected)
        return this
    }
}
```

- **tests/src/test/kotlin/org/veri/be/slice/web/MemberControllerTest.kt**
```kotlin
package org.veri.be.slice.web

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.personal.MemberController
import org.veri.be.domain.member.dto.MemberResponse
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.service.MemberCommandService
import org.veri.be.domain.member.service.MemberQueryService
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.lib.response.ApiResponseAdvice
import org.veri.be.support.ControllerTestSupport
import org.veri.be.support.fixture.MemberFixture
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberControllerTest : ControllerTestSupport() {

    @org.mockito.Mock
    private lateinit var memberCommandService: MemberCommandService

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    private lateinit var member: Member
    private lateinit var memberInfo: CurrentMemberInfo

    @BeforeEach
    fun setUp() {
        // Refactor: MemberFixture로 기본 값 제공
        member = MemberFixture.aMember()
            .id(1L)
            .providerType(ProviderType.KAKAO)
            .build()

        memberInfo = CurrentMemberInfo.from(JwtClaimsPayload(member.id, member.email, member.nickname, false))
        val controller = MemberController(memberCommandService, memberQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(testMemberAccessor(memberInfo))
            )
            .build()
    }

    private fun testMemberAccessor(memberInfo: CurrentMemberInfo): CurrentMemberAccessor {
        return object : CurrentMemberAccessor {
            override fun getCurrentMemberInfoOrNull() = memberInfo
            override fun getCurrentMember() = Optional.empty<Member>()
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/me")
    inner class GetMyInfo {

        @Test
        @DisplayName("요청하면 → 내 정보를 반환한다")
        fun returnsMyInfo() {
            val response = MemberResponse.MemberInfoResponse.builder()
                .email("member@test.com")
                .nickname("member")
                .image("https://example.com/profile.png")
                .numOfReadBook(3)
                .numOfCard(5)
                .build()
            given(memberQueryService.findMyInfo(memberInfo)).willReturn(response)

            // Refactor: ControllerTestSupport의 get() 헬퍼 사용
            get("/api/v1/members/me")
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.nickname").value("member"))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/members/me/info")
    inner class UpdateInfo {

        @Test
        @DisplayName("정보를 수정하면 → 결과를 반환한다")
        fun updatesInfo() {
            val request = UpdateMemberInfoRequest(
                "new-nickname",
                "https://example.com/new.png"
            )
            val response = MemberResponse.MemberSimpleResponse.builder()
                .id(1L)
                .nickname("new-nickname")
                .image("https://example.com/new.png")
                .build()
            given(memberCommandService.updateInfo(request, member.id)).willReturn(response)

            // Refactor: patchJson 헬퍼로 MockMvc 호출 단순화
            patchJson("/api/v1/members/me/info", request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.nickname").value("new-nickname"))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 → 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = UpdateMemberInfoRequest(null, null)

            // Refactor: patchJson 헬퍼로 MockMvc 호출 단순화
            patchJson("/api/v1/members/me/info", request)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/nickname/exists")
    inner class CheckNicknameExists {

        @Test
        @DisplayName("닉네임을 조회하면 → 중복 여부를 반환한다")
        fun returnsExists() {
            given(memberQueryService.existsByNickname("member")).willReturn(true)

            // Refactor: get 헬퍼로 파라미터 설정 단순화
            get("/api/v1/members/nickname/exists", mapOf("nickname" to "member"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(true))

            // Refactor: verify 대신 then().should() 사용
            then(memberQueryService).should().existsByNickname("member")
        }
    }
}
```

- **tests/src/test/kotlin/org/veri/be/unit/member/MemberCommandServiceTest.kt**
```kotlin
package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.member.dto.MemberResponse
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.exception.MemberErrorCode
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.member.service.MemberCommandService
import org.veri.be.domain.member.service.MemberQueryService
import org.veri.be.support.assertion.MemberAssert
import org.veri.be.support.assertion.ExceptionAssertions
import org.veri.be.support.fixture.MemberFixture

@ExtendWith(MockitoExtension::class)
class MemberCommandServiceTest {

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var memberCommandService: MemberCommandService

    @org.mockito.Captor
    private lateinit var memberCaptor: ArgumentCaptor<Member>

    @BeforeEach
    fun setUp() {
        memberCommandService = MemberCommandService(memberQueryService, memberRepository)
    }

    @Nested
    @DisplayName("updateInfo")
    inner class UpdateInfo {

        @Test
        @DisplayName("닉네임이 중복이면 → 예외가 발생한다")
        fun throwsWhenNicknameDuplicate() {
            // Refactor: MemberFixture로 기본 값 제공
            val member = MemberFixture.aMember()
                .id(1L)
                .nickname("old")
                .build()
            val request = UpdateMemberInfoRequest("dup", "https://example.com/profile.png")

            given(memberRepository.findById(1L)).willReturn(java.util.Optional.of(member))
            given(memberQueryService.existsByNickname("dup")).willReturn(true)

            ExceptionAssertions.assertApplicationException(
                { memberCommandService.updateInfo(request, member.id) },
                MemberErrorCode.ALREADY_EXIST_NICKNAME
            )
        }

        @Test
        @DisplayName("닉네임과 프로필을 수정하면 → 변경된 결과를 반환한다")
        fun updatesNicknameAndProfile() {
            // Refactor: MemberFixture로 기본 값 제공
            val member = MemberFixture.aMember()
                .id(1L)
                .nickname("old")
                .build()
            val request = UpdateMemberInfoRequest("new", "https://example.com/new.png")

            given(memberRepository.findById(1L)).willReturn(java.util.Optional.of(member))
            given(memberQueryService.existsByNickname("new")).willReturn(false)
            given(memberRepository.save(member)).willReturn(member)

            val response: MemberResponse.MemberSimpleResponse = memberCommandService.updateInfo(request, member.id)

            // Refactor: verify 대신 then().should() 사용
            then(memberRepository).should().save(memberCaptor.capture())
            // Refactor: MemberAssert로 검증 로직 통합
            MemberAssert.assertThat(memberCaptor.value)
                .hasNickname("new")
                .hasProfileImageUrl("https://example.com/new.png")
            assertThat(response.nickname).isEqualTo("new")
        }
    }
}
```

- **tests/src/test/kotlin/org/veri/be/unit/member/MemberQueryServiceTest.kt**
```kotlin
package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.card.repository.CardRepository
import org.veri.be.domain.member.dto.MemberResponse
import org.veri.be.domain.member.exception.MemberErrorCode
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.member.service.MemberQueryService
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor
import org.veri.be.support.assertion.MemberAssert
import org.veri.be.support.assertion.ExceptionAssertions
import org.veri.be.support.fixture.MemberFixture
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberQueryServiceTest {

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    @org.mockito.Mock
    private lateinit var readingRepository: ReadingRepository

    @org.mockito.Mock
    private lateinit var cardRepository: CardRepository

    @org.mockito.Mock
    private lateinit var threadLocalCurrentMemberAccessor: ThreadLocalCurrentMemberAccessor

    private lateinit var memberQueryService: MemberQueryService

    @BeforeEach
    fun setUp() {
        memberQueryService = MemberQueryService(
            memberRepository,
            readingRepository,
            cardRepository,
            threadLocalCurrentMemberAccessor
        )
    }

    @Nested
    @DisplayName("findById")
    inner class FindById {

        @Test
        @DisplayName("존재하지 않으면 → 예외를 던진다")
        fun throwsWhenNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { memberQueryService.findById(1L) },
                MemberErrorCode.NOT_FOUND
            )
        }

        @Test
        @DisplayName("존재하면 → 회원을 반환한다")
        fun returnsMember() {
            // Refactor: MemberFixture로 기본 값 제공
            val member = MemberFixture.aMember()
                .id(1L)
                .nickname("member")
                .build()
            given(memberRepository.findById(1L)).willReturn(Optional.of(member))

            val result = memberQueryService.findById(1L)

            // Refactor: MemberAssert로 검증 로직 통합
            MemberAssert.assertThat(result)
                .hasId(1L)
                .hasNickname("member")
        }
    }

    @Nested
    @DisplayName("findMyInfo")
    inner class FindMyInfo {

        @Test
        @DisplayName("독서/카드 수를 포함하면 → 정보를 반환한다")
        fun returnsMemberInfo() {
            // Refactor: MemberFixture로 기본 값 제공
            val member = MemberFixture.aMember()
                .id(1L)
                .nickname("member")
                .build()

            given(readingRepository.countAllByMemberId(1L)).willReturn(3)
            given(cardRepository.countAllByMemberId(1L)).willReturn(2)
            given(threadLocalCurrentMemberAccessor.memberOrThrow).willReturn(member)

            val response: MemberResponse.MemberInfoResponse =
                memberQueryService.findMyInfo(CurrentMemberInfo.from(JwtClaimsPayload(member.id, member.email, member.nickname, false)))

            assertThat(response.numOfReadBook).isEqualTo(3)
            assertThat(response.numOfCard).isEqualTo(2)
            assertThat(response.nickname).isEqualTo("member")
        }
    }

    @Nested
    @DisplayName("existsByNickname")
    inner class ExistsByNickname {

        @Test
        @DisplayName("닉네임을 조회하면 → 존재 여부를 반환한다")
        fun returnsExists() {
            given(memberRepository.existsByNickname("member")).willReturn(true)

            val exists = memberQueryService.existsByNickname("member")

            assertThat(exists).isTrue()
        }
    }
}
```

## [Remaining Gaps]
- **Fixture**: **CardFixture**, **PostFixture**, **BookFixture**, **CommentFixture**, **ReadingFixture** 미구축.
- **ControllerTestSupport 적용**: **PostControllerTest**, **CardControllerTest**, **CommentControllerTest**, **BookshelfControllerTest**, **ImageControllerTest**, **SocialCardControllerTest** 등 미리팩토링.
- **Unit Test 리팩토링**: **Card/Post/Comment/Image/Bookshelf/Auth** 단위 테스트에서 Fixture/Custom Assert/BDD 스타일 미적용.
- **Integration Test Steps**: **SocialCardIntegrationTest** 및 나머지 통합 테스트에서 Steps 패턴 미적용.
- **Custom Assert 확장**: **CardAssert**, **PostAssert** 등 복잡한 검증 로직 추출 미완료.

## Update: 2026-01-03

### [Audit Summary]
- **DisplayName 규칙 정규화**: 단위 테스트 전반의 **@DisplayName**을 **"상황 → 기대 결과"** 형태로 정리.
- **Fixture 기본 ID 제거**: **MemberFixture**, **BookFixture**, **ReadingFixture**, **CardFixture**, **PostFixture**, **CommentFixture**, **LikePostFixture**, **ImageFixture**에서 기본 **id** 설정 제거.
- **Steps 보완**: **CardSteps**, **PostSteps**에 요청/생성 분리 메서드 추가로 **ResultActions** 기반 검증 가능.
- **ControllerTestSupport 충돌 해결**: **MockMvcRequestBuilders** 사용으로 **post/get/patch/delete** 헬퍼의 호출 충돌 해결.
- **테스트 실행**: 아래 명령으로 전체 테스트 성공 확인.
```
./gradlew test
```

### [Refactored Code]
- **tests/src/test/kotlin/org/veri/be/support/fixture/MemberFixture.kt**
```kotlin
object MemberFixture {
    fun aMember(): Member.MemberBuilder<*, *> {
        return Member.builder()
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
    }
}
```

- **tests/src/test/kotlin/org/veri/be/support/steps/CardSteps.kt**
```kotlin
object CardSteps {
    fun requestCreateCard(mockMvc: MockMvc, objectMapper: ObjectMapper, request: CardCreateRequest): ResultActions {
        return mockMvc.perform(
            post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    fun createCard(mockMvc: MockMvc, objectMapper: ObjectMapper, request: CardCreateRequest): Long {
        val response = requestCreateCard(mockMvc, objectMapper, request)
            .andReturn()
            .response
            .contentAsString
        val cardId: Number = JsonPath.read(response, "$.result.cardId")
        return cardId.toLong()
    }
}
```

- **tests/src/test/kotlin/org/veri/be/support/ControllerTestSupport.kt**
```kotlin
open class ControllerTestSupport {
    protected lateinit var mockMvc: MockMvc
    protected val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()

    protected fun postJson(url: String, request: Any): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
    }

    protected fun get(url: String, params: Map<String, String> = emptyMap()): ResultActions {
        val builder = MockMvcRequestBuilders.get(url)
        params.forEach { (key, value) -> builder.param(key, value) }
        return mockMvc.perform(builder)
    }
}
```

### [Remaining Gaps]
- **없음**: 현재 범위 내 테스트 컨벤션 적용 및 개선 작업 완료.
