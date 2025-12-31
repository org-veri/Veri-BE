package org.veri.be.unit.global

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.veri.be.global.interceptors.InjectIPAddressInterceptor

class InjectIPAddressInterceptorTest {

    private val interceptor = InjectIPAddressInterceptor()

    @Nested
    @DisplayName("preHandle")
    inner class PreHandle {

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있으면 해당 값을 저장한다")
        fun usesForwardedHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("X-Forwarded-For", "10.0.0.1")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.1")
        }

        @Test
        @DisplayName("X-Forwarded-For 헤더에 여러 IP가 있으면 첫 번째 IP를 저장한다")
        fun extractsFirstIpFromForwardedHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2, 10.0.0.3")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.1")
        }

        @Test
        @DisplayName("X-Real-IP 헤더가 있으면 해당 값을 저장한다")
        fun usesRealIpHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("X-Real-IP", "10.0.0.3")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.3")
        }

        @Test
        @DisplayName("WL-Proxy-Client-IP 헤더가 있으면 해당 값을 저장한다")
        fun usesWlProxyHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("WL-Proxy-Client-IP", "10.0.0.4")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.4")
        }

        @Test
        @DisplayName("HTTP_CLIENT_IP 헤더가 있으면 해당 값을 저장한다")
        fun usesHttpClientIpHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("HTTP_CLIENT_IP", "10.0.0.5")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.5")
        }

        @Test
        @DisplayName("HTTP_X_FORWARDED_FOR 헤더가 있으면 해당 값을 저장한다")
        fun usesHttpXForwardedForHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("HTTP_X_FORWARDED_FOR", "10.0.0.6")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.6")
        }

        @Test
        @DisplayName("Proxy-Client-IP 헤더가 있으면 해당 값을 저장한다")
        fun usesProxyHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("Proxy-Client-IP", "10.0.0.2")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.2")
        }

        @Test
        @DisplayName("헤더가 unknown 값이면 다음 헤더를 확인한다")
        fun skipsUnknownValue() {
            val request = MockHttpServletRequest()
            request.addHeader("X-Forwarded-For", "unknown")
            request.addHeader("X-Real-IP", "10.0.0.7")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.7")
        }

        @Test
        @DisplayName("헤더가 없으면 RemoteAddr 값을 저장한다")
        fun usesRemoteAddr() {
            val request = MockHttpServletRequest()
            request.remoteAddr = "127.0.0.1"

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("127.0.0.1")
        }

        @Test
        @DisplayName("RemoteAddr이 IPv6 로컬호스트이면 127.0.0.1로 변환한다")
        fun convertsIpv6LocalhostToIpv4() {
            val request = MockHttpServletRequest()
            request.remoteAddr = "0:0:0:0:0:0:0:1"

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("127.0.0.1")
        }
    }
}
