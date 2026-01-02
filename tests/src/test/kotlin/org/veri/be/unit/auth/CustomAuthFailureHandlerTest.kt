package org.veri.be.unit.auth

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.veri.be.global.auth.AuthErrorInfo
import org.veri.be.global.auth.oauth2.CustomAuthFailureHandler
import org.veri.be.support.assertion.ExceptionAssertions

class CustomAuthFailureHandlerTest {

    private val handler = CustomAuthFailureHandler()

    @Nested
    @DisplayName("onAuthenticationFailure")
    inner class OnAuthenticationFailure {

        @Test
        @DisplayName("인증에 실패하면 → 예외가 발생한다")
        fun throwsWhenFailure() {
            ExceptionAssertions.assertApplicationException(
                {
                    handler.onAuthenticationFailure(
                        MockHttpServletRequest(),
                        MockHttpServletResponse(),
                        object : AuthenticationException("fail") {}
                    )
                },
                AuthErrorInfo.UNAUTHORIZED
            )
        }
    }
}
