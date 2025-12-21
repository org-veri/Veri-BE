package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.common.contenttype.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.Authenticator;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.oauth2.CustomOAuth2SuccessHandler;
import org.veri.be.global.auth.oauth2.dto.CustomOAuth2User;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2SuccessHandlerTest {

    @Mock
    Authenticator authService;

    @Mock
    Authentication authentication;

    @Captor
    ArgumentCaptor<OAuth2UserInfo> userInfoCaptor;

    CustomOAuth2SuccessHandler handler;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handler = new CustomOAuth2SuccessHandler(authService);
    }

    @Nested
    @DisplayName("onAuthenticationSuccess")
    class OnAuthenticationSuccess {

        @Test
        @DisplayName("OAuth2 사용자 정보로 로그인하고 응답을 작성한다")
        void writesLoginResponse() throws Exception {
            CustomOAuth2User user = new CustomOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    kakaoAttributes(),
                    "id",
                    "kakao"
            );
            given(authentication.getPrincipal()).willReturn(user);
            given(authService.loginWithOAuth2(any(OAuth2UserInfo.class)))
                    .willReturn(LoginResponse.builder()
                            .accessToken("access")
                            .refreshToken("refresh")
                            .build());
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(authService).loginWithOAuth2(userInfoCaptor.capture());
            OAuth2UserInfo captured = userInfoCaptor.getValue();
            assertThat(captured.getEmail()).isEqualTo("member@test.com");
            assertThat(captured.getNickname()).isEqualTo("member");
            assertThat(captured.getProviderId()).isEqualTo("10");
            assertThat(captured.getProviderType()).isEqualTo(ProviderType.KAKAO);

            assertThat(response.getContentType()).contains(ContentType.APPLICATION_JSON.getType());
            assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

            JsonNode json = objectMapper.readTree(response.getContentAsString());
            assertThat(json.get("result").get("accessToken").asText()).isEqualTo("access");
            assertThat(json.get("result").get("refreshToken").asText()).isEqualTo("refresh");
        }

        private Map<String, Object> kakaoAttributes() {
            Map<String, Object> profile = new HashMap<>();
            profile.put("nickname", "member");
            profile.put("profile_image_url", "https://example.com/profile.png");

            Map<String, Object> account = new HashMap<>();
            account.put("email", "member@test.com");
            account.put("profile", profile);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", 10L);
            attributes.put("kakao_account", account);
            return attributes;
        }
    }
}
