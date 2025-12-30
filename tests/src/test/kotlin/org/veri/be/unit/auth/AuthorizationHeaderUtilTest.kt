package org.veri.be.unit.auth

import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.lib.auth.jwt.AuthorizationHeaderUtil

@ExtendWith(MockitoExtension::class)
class AuthorizationHeaderUtilTest {

    @org.mockito.Mock
    private lateinit var request: HttpServletRequest

    @Nested
    @DisplayName("extractTokenFromAuthorizationHeader")
    inner class ExtractTokenFromAuthorizationHeader {

        @Test
        @DisplayName("Bearer 토큰을 추출한다")
        fun extractsBearerToken() {
            given(request.getHeader("Authorization")).willReturn("Bearer token-value")

            val token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request)

            assertThat(token).isEqualTo("token-value")
        }

        @Test
        @DisplayName("Authorization 헤더가 없으면 null을 반환한다")
        fun returnsNullWhenMissing() {
            given(request.getHeader("Authorization")).willReturn(null)

            val token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request)

            assertThat(token).isNull()
        }

        @Test
        @DisplayName("Bearer 스킴이 아니면 null을 반환한다")
        fun returnsNullWhenNotBearer() {
            given(request.getHeader("Authorization")).willReturn("Basic abc")

            val token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request)

            assertThat(token).isNull()
        }
    }
}
