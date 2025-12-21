package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.lib.auth.jwt.AuthorizationHeaderUtil;

@ExtendWith(MockitoExtension.class)
class AuthorizationHeaderUtilTest {

    @Mock
    HttpServletRequest request;

    @Nested
    @DisplayName("extractTokenFromAuthorizationHeader")
    class ExtractTokenFromAuthorizationHeader {

        @Test
        @DisplayName("Bearer 토큰을 추출한다")
        void extractsBearerToken() {
            given(request.getHeader("Authorization")).willReturn("Bearer token-value");

            String token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request);

            assertThat(token).isEqualTo("token-value");
        }

        @Test
        @DisplayName("Authorization 헤더가 없으면 null을 반환한다")
        void returnsNullWhenMissing() {
            given(request.getHeader("Authorization")).willReturn(null);

            String token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request);

            assertThat(token).isNull();
        }

        @Test
        @DisplayName("Bearer 스킴이 아니면 null을 반환한다")
        void returnsNullWhenNotBearer() {
            given(request.getHeader("Authorization")).willReturn("Basic abc");

            String token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request);

            assertThat(token).isNull();
        }
    }
}
