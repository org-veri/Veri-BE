package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.auth.storage.TokenStorageService
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.dto.ReissueTokenRequest
import org.veri.be.lib.auth.token.TokenProvider
import org.veri.be.integration.IntegrationTestSupport

class AuthIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    @Autowired
    private lateinit var tokenStorageService: TokenStorageService

    @Nested
    @DisplayName("POST /api/v1/auth/reissue")
    inner class Reissue {
        @Test
        @DisplayName("Stored refresh 토큰으로 재발급")
        fun reissueSuccess() {
            val refreshToken = tokenProvider.generateRefreshToken(getMockMember().id).token()
            tokenStorageService.addRefreshToken(getMockMember().id, refreshToken, 100000L)

            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", refreshToken)

            mockMvc.perform(
                post("/api/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.accessToken").exists())
        }

        @Test
        @DisplayName("만료 혹은 위조된 refresh 토큰")
        fun invalidToken() {
            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", "invalid-token")

            mockMvc.perform(
                post("/api/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("refreshToken 누락/NULL")
        fun missingToken() {
            val request = ReissueTokenRequest()

            mockMvc.perform(
                post("/api/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("탈퇴 등으로 존재하지 않는 회원 토큰")
        fun nonExistentMember() {
            val refreshToken = tokenProvider.generateRefreshToken(9999L).token()
            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", refreshToken)

            mockMvc.perform(
                post("/api/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("블랙리스트에 등록된 refresh 토큰")
        fun blacklistedToken() {
            val refreshToken = tokenProvider.generateRefreshToken(getMockMember().id).token()
            tokenStorageService.addBlackList(refreshToken, 100000L)

            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", refreshToken)

            mockMvc.perform(
                post("/api/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    inner class Logout {
        @Test
        @DisplayName("정상 로그아웃")
        fun logoutSuccess() {
            val member = getMockMember()
            val accessToken = tokenProvider.generateAccessToken(
                JwtClaimsPayload.of(member.id, member.email, member.nickname, false)
            ).token()

            mockMvc.perform(
                post("/api/v1/auth/logout")
                    .requestAttr("token", accessToken)
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("이미 블랙리스트 처리된 토큰으로 재요청")
        fun alreadyLoggedOut() {
            val member = getMockMember()
            val accessToken = tokenProvider.generateAccessToken(
                JwtClaimsPayload.of(member.id, member.email, member.nickname, false)
            ).token()

            mockMvc.perform(
                post("/api/v1/auth/logout")
                    .requestAttr("token", accessToken)
            )
                .andExpect(status().isNoContent)

            mockMvc.perform(
                post("/api/v1/auth/logout")
                    .requestAttr("token", accessToken)
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("refresh 토큰이 저장소에 없음")
        fun noRefreshTokenInStorage() {
            val member = getMockMember()
            val accessToken = tokenProvider.generateAccessToken(
                JwtClaimsPayload.of(member.id, member.email, member.nickname, false)
            ).token()
            tokenStorageService.deleteRefreshToken(getMockMember().id)

            mockMvc.perform(
                post("/api/v1/auth/logout")
                    .requestAttr("token", accessToken)
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("attribute 에 토큰 없음")
        fun missingAttribute() {
            mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isBadRequest)
        }
    }
}
