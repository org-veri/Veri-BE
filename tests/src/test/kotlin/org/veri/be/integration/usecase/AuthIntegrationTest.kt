package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.auth.service.TokenStorageService
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.dto.ReissueTokenRequest
import org.veri.be.global.auth.token.TokenProvider
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.steps.AuthSteps

class AuthIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    @Autowired
    private lateinit var tokenStorageService: TokenStorageService

    @Nested
    @DisplayName("POST /api/v1/auth/reissue")
    inner class Reissue {
        @Test
        @DisplayName("Stored refresh 토큰이면 → 재발급한다")
        fun reissueSuccess() {
            val refreshToken = tokenProvider.generateRefreshToken(getMockMember().id).token()
            tokenStorageService.addRefreshToken(getMockMember().id, refreshToken, 100000L)

            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", refreshToken)

            AuthSteps.reissueToken(mockMvc, objectMapper, request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.accessToken").exists())
        }

        @Test
        @DisplayName("만료 혹은 위조된 refresh 토큰이면 → 401을 반환한다")
        fun invalidToken() {
            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", "invalid-token")

            AuthSteps.reissueToken(mockMvc, objectMapper, request)
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("refreshToken 누락/NULL이면 → 400을 반환한다")
        fun missingToken() {
            val request = ReissueTokenRequest()

            AuthSteps.reissueToken(mockMvc, objectMapper, request)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("존재하지 않는 회원 토큰이면 → 401을 반환한다")
        fun nonExistentMember() {
            val refreshToken = tokenProvider.generateRefreshToken(9999L).token()
            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", refreshToken)

            AuthSteps.reissueToken(mockMvc, objectMapper, request)
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("블랙리스트에 등록된 refresh 토큰이면 → 401을 반환한다")
        fun blacklistedToken() {
            val refreshToken = tokenProvider.generateRefreshToken(getMockMember().id).token()
            tokenStorageService.addBlackList(refreshToken, 100000L)

            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", refreshToken)

            AuthSteps.reissueToken(mockMvc, objectMapper, request)
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    inner class Logout {
        @Test
        @DisplayName("정상 로그아웃이면 → 204를 반환한다")
        fun logoutSuccess() {
            val accessToken = tokenProvider.generateAccessToken(
                JwtClaimsPayload(getMockMember().id, getMockMember().email, getMockMember().nickname, false)
            ).token()

            AuthSteps.logout(mockMvc, accessToken)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("이미 블랙리스트 처리된 토큰으로 재요청하면 → 204를 반환한다")
        fun alreadyLoggedOut() {
            val accessToken = tokenProvider.generateAccessToken(
                JwtClaimsPayload(getMockMember().id, getMockMember().email, getMockMember().nickname, false)
            ).token()

            AuthSteps.logout(mockMvc, accessToken)
                .andExpect(status().isNoContent)

            AuthSteps.logout(mockMvc, accessToken)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("refresh 토큰이 저장소에 없으면 → 204를 반환한다")
        fun noRefreshTokenInStorage() {
            val accessToken = tokenProvider.generateAccessToken(
                JwtClaimsPayload(getMockMember().id, getMockMember().email, getMockMember().nickname, false)
            ).token()
            tokenStorageService.deleteRefreshToken(getMockMember().id)

            AuthSteps.logout(mockMvc, accessToken)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("attribute 에 토큰이 없으면 → 400을 반환한다")
        fun missingAttribute() {
            AuthSteps.logout(mockMvc, null)
                .andExpect(status().isBadRequest)
        }
    }
}
