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
        @DisplayName("Proxy-Client-IP 헤더가 있으면 해당 값을 저장한다")
        fun usesProxyHeader() {
            val request = MockHttpServletRequest()
            request.addHeader("Proxy-Client-IP", "10.0.0.2")

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("10.0.0.2")
        }

        @Test
        @DisplayName("헤더가 없으면 RemoteAddr 값을 저장한다")
        fun usesRemoteAddr() {
            val request = MockHttpServletRequest()
            request.remoteAddr = "127.0.0.1"

            interceptor.preHandle(request, MockHttpServletResponse(), Any())

            assertThat(request.getAttribute(InjectIPAddressInterceptor.IP)).isEqualTo("127.0.0.1")
        }
    }
}
