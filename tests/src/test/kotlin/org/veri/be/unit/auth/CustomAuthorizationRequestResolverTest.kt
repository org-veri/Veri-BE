package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.veri.be.global.auth.oauth2.CustomAuthorizationRequestResolver

class CustomAuthorizationRequestResolverTest {

    private fun repository(): ClientRegistrationRepository {
        val registration = ClientRegistration.withRegistrationId("kakao")
            .clientId("client-id")
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .authorizationUri("https://auth.example.com/oauth/authorize")
            .tokenUri("https://auth.example.com/oauth/token")
            .userInfoUri("https://auth.example.com/userinfo")
            .userNameAttributeName("id")
            .clientName("kakao")
            .build()
        return InMemoryClientRegistrationRepository(listOf(registration))
    }

    @Nested
    @DisplayName("resolve")
    inner class Resolve {

        @Test
        @DisplayName("Origin 헤더가 있으면 redirectUri를 변경한다")
        fun overridesRedirectUriWhenOriginProvided() {
            val resolver = CustomAuthorizationRequestResolver(repository())
            val request = MockHttpServletRequest("GET", "/oauth2/authorization/kakao")
            request.addHeader("Origin", "https://example.com")

            val result: OAuth2AuthorizationRequest? = resolver.resolve(request)

            assertThat(result).isNotNull
            assertThat(result!!.redirectUri).isEqualTo("https://example.com/oauth/callback/kakao")
        }

        @Test
        @DisplayName("요청 URL이 없으면 기본 redirectUri를 유지한다")
        fun keepsDefaultRedirectUriWhenNoOrigin() {
            val repo = repository()
            val resolver = CustomAuthorizationRequestResolver(repo)
            val request = MockHttpServletRequest("GET", "/oauth2/authorization/kakao")

            val result = resolver.resolve(request)
            val defaultRequest = DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization")
                .resolve(request)

            assertThat(result).isNotNull
            assertThat(result!!.redirectUri).isEqualTo(defaultRequest!!.redirectUri)
        }

        @Test
        @DisplayName("요청이 매칭되지 않으면 null을 반환한다")
        fun returnsNullWhenNoMatch() {
            val resolver = CustomAuthorizationRequestResolver(repository())
            val request = MockHttpServletRequest("GET", "/other/path")

            val result = resolver.resolve(request)

            assertThat(result).isNull()
        }

        @Test
        @DisplayName("clientRegistrationId가 주어지면 redirectUri를 변경한다")
        fun overridesRedirectUriWithClientRegistrationId() {
            val resolver = CustomAuthorizationRequestResolver(repository())
            val request = MockHttpServletRequest("GET", "/oauth2/authorization/kakao")
            request.addHeader("Origin", "https://example.com")

            val result = resolver.resolve(request, "kakao")

            assertThat(result).isNotNull
            assertThat(result!!.redirectUri).isEqualTo("https://example.com/oauth/callback/kakao")
        }
    }
}
