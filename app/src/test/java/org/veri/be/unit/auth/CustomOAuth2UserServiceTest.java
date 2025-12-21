package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;
import org.springframework.http.RequestEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.oauth2.CustomOAuth2UserService;
import org.veri.be.global.auth.oauth2.dto.CustomOAuth2User;

class CustomOAuth2UserServiceTest {

    @Nested
    @DisplayName("loadUser")
    class LoadUser {

        @Test
        @DisplayName("OAuth2 사용자 정보를 CustomOAuth2User로 변환한다")
        void returnsCustomOAuth2User() {
            RestOperations restOperations = Mockito.mock(RestOperations.class);
            Map<String, Object> attributes = Map.of("id", 10L, "email", "member@test.com");
            ResponseEntity<Map> response = new ResponseEntity<>(attributes, HttpStatus.OK);

            Mockito.lenient().when(restOperations.exchange(any(RequestEntity.class), eq(Map.class)))
                    .thenReturn(response);
            Mockito.lenient().when(restOperations.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
                    .thenReturn(response);

            CustomOAuth2UserService service = new CustomOAuth2UserService();
            service.setRestOperations(restOperations);

            OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration(), accessToken());

            OAuth2User user = service.loadUser(userRequest);

            assertThat(user).isInstanceOf(CustomOAuth2User.class);
            CustomOAuth2User customUser = (CustomOAuth2User) user;
            assertThat(customUser.getProviderType()).isEqualTo(ProviderType.KAKAO);
            assertThat(customUser.getAuthorities()).hasSize(1);
            GrantedAuthority authority = customUser.getAuthorities().iterator().next();
            assertThat(authority.getAuthority()).isEqualTo("ROLE_USER");
            assertThat(customUser.getName()).isEqualTo("10");
        }
    }

    private ClientRegistration clientRegistration() {
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
                .build();
    }

    private OAuth2AccessToken accessToken() {
        return new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300)
        );
    }
}
