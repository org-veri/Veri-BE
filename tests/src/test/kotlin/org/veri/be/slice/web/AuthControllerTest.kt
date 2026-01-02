package org.veri.be.slice.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.common.AuthController
import org.veri.be.domain.auth.service.AuthService
import org.veri.be.global.auth.dto.ReissueTokenRequest
import org.veri.be.global.auth.dto.ReissueTokenResponse
import org.veri.be.lib.response.ApiResponseAdvice
import org.veri.be.support.ControllerTestSupport

@ExtendWith(MockitoExtension::class)
class AuthControllerTest : ControllerTestSupport() {

    @org.mockito.Mock
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        val controller = AuthController(authService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .build()
    }

    @Nested
    @DisplayName("POST /api/v1/auth/reissue")
    inner class ReissueToken {

        @Test
        @DisplayName("리프레시 토큰을 보내면 → 새 액세스 토큰을 반환한다")
        fun returnsNewAccessToken() {
            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", "refresh")
            val response = ReissueTokenResponse.builder()
                .accessToken("new-access")
                .build()
            given(authService.reissueToken(any(ReissueTokenRequest::class.java))).willReturn(response)

            postJson("/api/v1/auth/reissue", request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.accessToken").value("new-access"))

            val requestCaptor = ArgumentCaptor.forClass(ReissueTokenRequest::class.java)
            then(authService).should().reissueToken(requestCaptor.capture())
            assertThat(requestCaptor.value.refreshToken).isEqualTo("refresh")
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    inner class Logout {

        @Test
        @DisplayName("로그아웃하면 → 204를 반환한다")
        fun logsOut() {
            mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/logout")
                    .requestAttr("token", "access")
            )
                .andExpect(status().isNoContent)

            then(authService).should().logout("access")
        }
    }
}
