package org.veri.be.slice.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
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
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @org.mockito.Mock
    private lateinit var memberCommandService: MemberCommandService

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    private lateinit var member: Member
    private lateinit var memberInfo: CurrentMemberInfo

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        member = Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
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
        @DisplayName("내 정보를 반환한다")
        fun returnsMyInfo() {
            val response = MemberResponse.MemberInfoResponse.builder()
                .email("member@test.com")
                .nickname("member")
                .image("https://example.com/profile.png")
                .numOfReadBook(3)
                .numOfCard(5)
                .build()
            given(memberQueryService.findMyInfo(memberInfo)).willReturn(response)

            mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.nickname").value("member"))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/members/me/info")
    inner class UpdateInfo {

        @Test
        @DisplayName("내 정보를 수정하면 결과를 반환한다")
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

            mockMvc.perform(
                patch("/api/v1/members/me/info")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.nickname").value("new-nickname"))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = UpdateMemberInfoRequest(null, null)

            mockMvc.perform(
                patch("/api/v1/members/me/info")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/nickname/exists")
    inner class CheckNicknameExists {

        @Test
        @DisplayName("닉네임 중복 여부를 반환한다")
        fun returnsExists() {
            given(memberQueryService.existsByNickname("member")).willReturn(true)

            mockMvc.perform(
                get("/api/v1/members/nickname/exists")
                    .param("nickname", "member")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(true))

            verify(memberQueryService).existsByNickname("member")
        }
    }
}
