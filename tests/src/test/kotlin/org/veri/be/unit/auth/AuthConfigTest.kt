package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.support.StaticApplicationContext
import org.springframework.security.config.ObjectPostProcessor
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.veri.be.lib.auth.token.TokenBlacklistStore
import org.veri.be.member.service.MemberQueryService
import org.veri.be.global.auth.AuthConfig
import org.veri.be.global.auth.oauth2.CustomAuthFailureHandler
import org.veri.be.global.auth.oauth2.CustomOAuth2SuccessHandler
import org.veri.be.global.auth.oauth2.CustomOAuth2UserService
import org.veri.be.lib.auth.token.TokenProvider

class AuthConfigTest {

    @Nested
    @DisplayName("firstFilterRegister")
    inner class JwtFilterRegistration {

        @Test
        @DisplayName("JWT 필터 등록 정보를 생성한다")
        fun buildsFilterRegistration() {
            val authConfig = AuthConfig(
                Mockito.mock(CustomOAuth2UserService::class.java),
                Mockito.mock(CustomOAuth2SuccessHandler::class.java),
                Mockito.mock(CustomAuthFailureHandler::class.java),
                Mockito.mock(TokenBlacklistStore::class.java),
                Mockito.mock(TokenProvider::class.java)
            )

            val bean: FilterRegistrationBean<*> = authConfig.firstFilterRegister()

            assertThat(bean).isNotNull
            assertThat(bean.urlPatterns).contains("/api/*")
        }
    }

    @Nested
    @DisplayName("securityFilterChain")
    inner class SecurityFilterChainConfig {

        @Test
        @DisplayName("보안 필터 체인을 구성한다")
        fun buildsSecurityFilterChain() {
            val customOAuth2UserService = Mockito.mock(CustomOAuth2UserService::class.java)
            val customOAuth2SuccessHandler = Mockito.mock(CustomOAuth2SuccessHandler::class.java)
            val customAuthFailureHandler = Mockito.mock(CustomAuthFailureHandler::class.java)
            val authConfig = AuthConfig(
                customOAuth2UserService,
                customOAuth2SuccessHandler,
                customAuthFailureHandler,
                Mockito.mock(TokenBlacklistStore::class.java),
                Mockito.mock(TokenProvider::class.java)
            )
            val objectPostProcessor = object : ObjectPostProcessor<Any> {
                override fun <O : Any?> postProcess(obj: O): O = obj
            }
            val authBuilder = AuthenticationManagerBuilder(objectPostProcessor)

            val registration = ClientRegistration.withRegistrationId("kakao")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .authorizationUri("https://example.com/oauth/authorize")
                .tokenUri("https://example.com/oauth/token")
                .userInfoUri("https://example.com/oauth/userinfo")
                .userNameAttributeName("id")
                .clientName("kakao")
                .build()
            val repository: ClientRegistrationRepository = InMemoryClientRegistrationRepository(registration)
            val authorizedClientService: OAuth2AuthorizedClientService =
                InMemoryOAuth2AuthorizedClientService(repository)

            val sharedObjects = HashMap<Class<*>, Any>()
            sharedObjects[ClientRegistrationRepository::class.java] = repository
            sharedObjects[OAuth2AuthorizedClientService::class.java] = authorizedClientService
            val applicationContext: ApplicationContext = StaticApplicationContext()
            sharedObjects[ApplicationContext::class.java] = applicationContext

            val httpSecurity = HttpSecurity(objectPostProcessor, authBuilder, sharedObjects)
            httpSecurity.setSharedObject(
                PathPatternRequestMatcher.Builder::class.java,
                PathPatternRequestMatcher.withDefaults()
            )

            val result: SecurityFilterChain = authConfig.securityFilterChain(httpSecurity)

            assertThat(result).isNotNull
        }
    }
}
