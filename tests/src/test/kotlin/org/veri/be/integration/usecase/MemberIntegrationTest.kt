package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.steps.MemberSteps

class MemberIntegrationTest : IntegrationTestSupport() {

    @Nested
    @DisplayName("GET /api/v1/members/me")
    inner class GetMe {
        @Test
        @DisplayName("요청하면 → 내 정보를 조회한다")
        fun getMeSuccess() {
            MemberSteps.getMyInfo(mockMvc)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.email").value(getMockMember().email))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/members/me/info")
    inner class UpdateInfo {
        @Test
        @DisplayName("닉네임/프로필을 수정하면 → 결과를 반환한다")
        fun updateInfoSuccess() {
            val request = UpdateMemberInfoRequest("newNick", "https://example.com/new.png")

            MemberSteps.updateInfo(mockMvc, objectMapper, request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.nickname").value("newNick"))
        }

        @Test
        @DisplayName("닉네임이 중복이면 → 409를 반환한다")
        fun updateDuplicateNickname() {
            val other = MemberFixture.aMember()
                .email("other@test.com")
                .nickname("dupNick")
                .profileImageUrl("https://example.com/img.png")
                .providerId("p2")
                .providerType(ProviderType.KAKAO)
                .build()
            memberRepository.save(other)

            val request = UpdateMemberInfoRequest("dupNick", "https://example.com/new.png")

            MemberSteps.updateInfo(mockMvc, objectMapper, request)
                .andExpect(status().isConflict)
        }

        @Test
        @DisplayName("필수 필드 검증 실패면 → 400을 반환한다")
        fun updateValidationFail() {
            val request = UpdateMemberInfoRequest("", "invalid-url")

            MemberSteps.updateInfo(mockMvc, objectMapper, request)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("기존 닉네임과 동일하면 → 200을 반환한다")
        fun updateSameNickname() {
            val request = UpdateMemberInfoRequest(getMockMember().nickname, "https://example.com/new.png")

            MemberSteps.updateInfo(mockMvc, objectMapper, request)
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/nickname/exists")
    inner class CheckNickname {
        @Test
        @DisplayName("닉네임이 존재하면 → true를 반환한다")
        fun existsTrue() {
            MemberSteps.checkNickname(mockMvc, getMockMember().nickname)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(true))
        }

        @Test
        @DisplayName("파라미터가 누락되면 → 400을 반환한다")
        fun missingParam() {
            MemberSteps.checkNickname(mockMvc, null)
                .andExpect(status().isBadRequest)
        }
    }
}
