package org.veri.be.unit.auth

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.common.contenttype.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.Authenticator
import org.veri.be.global.auth.dto.LoginResponse
import org.veri.be.global.auth.oauth2.CustomOAuth2SuccessHandler
import org.veri.be.global.auth.oauth2.dto.CustomOAuth2User
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo

@ExtendWith(MockitoExtension::class)
class CustomOAuth2SuccessHandlerTest {

    @org.mockito.Mock
    private lateinit var authService: Authenticator

    @org.mockito.Mock
    private lateinit var authentication: Authentication

    @org.mockito.Captor
    private lateinit var userInfoCaptor: ArgumentCaptor<OAuth2UserInfo>

    private lateinit var handler: CustomOAuth2SuccessHandler

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        handler = CustomOAuth2SuccessHandler(authService)
    }

    @Nested
    @DisplayName("onAuthenticationSuccess")
    inner class OnAuthenticationSuccess {

        @Test
        @DisplayName("OAuth2 사용자 정보로 로그인하고 응답을 작성한다")
        fun writesLoginResponse() {
            val user = CustomOAuth2User(
                listOf(SimpleGrantedAuthority("ROLE_USER")),
                kakaoAttributes(),
                "id",
                "kakao"
            )
            given(authentication.principal).willReturn(user)
            given(authService.loginWithOAuth2(any(OAuth2UserInfo::class.java)))
                .willReturn(
                    LoginResponse.builder()
                        .accessToken("access")
                        .refreshToken("refresh")
                        .build()
                )
            val request = MockHttpServletRequest()
            val response = MockHttpServletResponse()

            handler.onAuthenticationSuccess(request, response, authentication)

            verify(authService).loginWithOAuth2(userInfoCaptor.capture())
            val captured = userInfoCaptor.value
            assertThat(captured.email).isEqualTo("member@test.com")
            assertThat(captured.nickname).isEqualTo("member")
            assertThat(captured.providerId).isEqualTo("10")
            assertThat(captured.providerType).isEqualTo(ProviderType.KAKAO)

            assertThat(response.contentType).contains(ContentType.APPLICATION_JSON.type)
            assertThat(response.characterEncoding).isEqualTo("UTF-8")

            val json: JsonNode = objectMapper.readTree(response.contentAsString)
            assertThat(json.get("result").get("accessToken").asText()).isEqualTo("access")
            assertThat(json.get("result").get("refreshToken").asText()).isEqualTo("refresh")
        }

        private fun kakaoAttributes(): Map<String, Any> {
            val profile = HashMap<String, Any>()
            profile["nickname"] = "member"
            profile["profile_image_url"] = "https://example.com/profile.png"

            val account = HashMap<String, Any>()
            account["email"] = "member@test.com"
            account["profile"] = profile

            val attributes = HashMap<String, Any>()
            attributes["id"] = 10L
            attributes["kakao_account"] = account
            return attributes
        }
    }
}
