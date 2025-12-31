package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.client.RestOperations
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.oauth2.CustomOAuth2UserService
import org.veri.be.global.auth.oauth2.dto.CustomOAuth2User
import java.time.Instant

class CustomOAuth2UserServiceTest {

    @Nested
    @DisplayName("loadUser")
    inner class LoadUser {

        @Test
        @DisplayName("OAuth2 사용자 정보를 CustomOAuth2User로 변환한다")
        fun returnsCustomOAuth2User() {
            // given
            val restOperations = Mockito.mock(RestOperations::class.java)

            val attributes = mapOf(
                "id" to 10L,
                "email" to "member@test.com"
            )
            val response = ResponseEntity(attributes, HttpStatus.OK)

            Mockito.lenient()
                .doReturn(response)
                .`when`(restOperations).exchange(
                    any(RequestEntity::class.java),
                    eq(Map::class.java)
                )

            Mockito.lenient()
                .doReturn(response)
                .`when`(restOperations).exchange(
                    any(RequestEntity::class.java),
                    any<ParameterizedTypeReference<Map<String, Any>>>()
                )

            // when
            val service = CustomOAuth2UserService()
            service.setRestOperations(restOperations)

            val userRequest = OAuth2UserRequest(clientRegistration(), accessToken())
            val user: OAuth2User = service.loadUser(userRequest)

            // then
            assertThat(user).isInstanceOf(CustomOAuth2User::class.java)

            val customUser = user as CustomOAuth2User
            assertThat(customUser.providerType).isEqualTo(ProviderType.KAKAO)
            assertThat(customUser.name).isEqualTo("10")

            assertThat(customUser.authorities).hasSize(1)
            val authority: GrantedAuthority = customUser.authorities.iterator().next()
            assertThat(authority.authority).isEqualTo("ROLE_USER")
        }

        private fun clientRegistration(): ClientRegistration {
            return ClientRegistration.withRegistrationId("kakao")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName("id")
                .scope("profile")
                .build()
        }

        private fun accessToken(): OAuth2AccessToken {
            return OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300)
            )
        }
    }
}
