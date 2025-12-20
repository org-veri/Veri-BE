package org.veri.be.unit.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.global.auth.oauth2.CustomAuthFailureHandler;
import org.veri.be.support.assertion.ExceptionAssertions;

class CustomAuthFailureHandlerTest {

    CustomAuthFailureHandler handler = new CustomAuthFailureHandler();

    @Nested
    @DisplayName("onAuthenticationFailure")
    class OnAuthenticationFailure {

        @Test
        @DisplayName("인증 실패 시 예외가 발생한다")
        void throwsWhenFailure() {
            ExceptionAssertions.assertApplicationException(
                    () -> handler.onAuthenticationFailure(
                            new MockHttpServletRequest(),
                            new MockHttpServletResponse(),
                            new AuthenticationException("fail") {
                            }
                    ),
                    AuthErrorInfo.UNAUTHORIZED
            );
        }
    }
}
