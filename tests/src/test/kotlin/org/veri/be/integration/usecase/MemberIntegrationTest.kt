package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.member.dto.UpdateMemberInfoRequest
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.integration.IntegrationTestSupport

class MemberIntegrationTest : IntegrationTestSupport() {

    @Nested
    @DisplayName("GET /api/v1/members/me")
    inner class GetMe {
        @Test
        @DisplayName("정상 내 정보 조회")
        fun getMeSuccess() {
            mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.email").value(getMockMember().email))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/members/me/info")
    inner class UpdateInfo {
        @Test
        @DisplayName("닉네임/프로필 모두 수정")
        fun updateInfoSuccess() {
            val request = UpdateMemberInfoRequest("newNick", "https://example.com/new.png")

            mockMvc.perform(
                patch("/api/v1/members/me/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.nickname").value("newNick"))
        }

        @Test
        @DisplayName("닉네임 중복")
        fun updateDuplicateNickname() {
            val other = Member.builder()
                .email("other@test.com")
                .nickname("dupNick")
                .profileImageUrl("https://example.com/img.png")
                .providerId("p2")
                .providerType(ProviderType.KAKAO)
                .build()
            memberRepository.save(other)

            val request = UpdateMemberInfoRequest("dupNick", "https://example.com/new.png")

            mockMvc.perform(
                patch("/api/v1/members/me/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isConflict)
        }

        @Test
        @DisplayName("필수 필드 검증 실패")
        fun updateValidationFail() {
            val request = UpdateMemberInfoRequest("", "invalid-url")

            mockMvc.perform(
                patch("/api/v1/members/me/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("기존 닉네임과 동일한 값 요청")
        fun updateSameNickname() {
            val request = UpdateMemberInfoRequest(getMockMember().nickname, "https://example.com/new.png")

            mockMvc.perform(
                patch("/api/v1/members/me/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/nickname/exists")
    inner class CheckNickname {
        @Test
        @DisplayName("닉네임 존재 여부 true")
        fun existsTrue() {
            mockMvc.perform(
                get("/api/v1/members/nickname/exists")
                    .param("nickname", getMockMember().nickname)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(true))
        }

        @Test
        @DisplayName("파라미터 누락")
        fun missingParam() {
            mockMvc.perform(get("/api/v1/members/nickname/exists"))
                .andExpect(status().isBadRequest)
        }
    }
}
