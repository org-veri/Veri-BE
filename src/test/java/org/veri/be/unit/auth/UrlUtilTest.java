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
import org.veri.be.lib.auth.util.UrlUtil;

@ExtendWith(MockitoExtension.class)
class UrlUtilTest {

    @Mock
    HttpServletRequest request;

    @Nested
    @DisplayName("getRequestingUrl")
    class GetRequestingUrl {

        @Test
        @DisplayName("request가 null이면 null을 반환한다")
        void returnsNullWhenRequestNull() {
            String result = UrlUtil.getRequestingUrl(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Origin 헤더가 있으면 Origin을 반환한다")
        void returnsOriginHeader() {
            given(request.getHeader("Origin")).willReturn("https://example.com");

            String result = UrlUtil.getRequestingUrl(request);

            assertThat(result).isEqualTo("https://example.com");
        }

        @Test
        @DisplayName("Referer 헤더의 base URL을 반환한다")
        void returnsBaseUrlFromReferer() {
            given(request.getHeader("Origin")).willReturn(null);
            given(request.getHeader("Referer")).willReturn("https://example.com/path/page?query=1");

            String result = UrlUtil.getRequestingUrl(request);

            assertThat(result).isEqualTo("https://example.com");
        }

        @Test
        @DisplayName("포트가 포함된 Referer는 포트까지 포함한다")
        void returnsBaseUrlWithPort() {
            given(request.getHeader("Origin")).willReturn("");
            given(request.getHeader("Referer")).willReturn("http://localhost:8080/path");

            String result = UrlUtil.getRequestingUrl(request);

            assertThat(result).isEqualTo("http://localhost:8080");
        }

        @Test
        @DisplayName("Referer가 잘못된 형식이면 null을 반환한다")
        void returnsNullWhenRefererInvalid() {
            given(request.getHeader("Origin")).willReturn(null);
            given(request.getHeader("Referer")).willReturn("not-a-url");

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> UrlUtil.getRequestingUrl(request)
            );
        }
    }
}
