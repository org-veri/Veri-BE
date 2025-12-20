package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import org.mockito.Mockito;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.veri.be.domain.auth.service.TokenBlacklistStore;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.AuthConfig;
import org.veri.be.global.auth.oauth2.CustomAuthFailureHandler;
import org.veri.be.global.auth.oauth2.CustomOAuth2SuccessHandler;
import org.veri.be.global.auth.oauth2.CustomOAuth2UserService;
import org.veri.be.global.auth.token.TokenProvider;

class AuthConfigTest {

    @Nested
    @DisplayName("firstFilterRegister")
    class JwtFilterRegistration {

        @Test
        @DisplayName("JWT 필터 등록 정보를 생성한다")
        void buildsFilterRegistration() {
            AuthConfig authConfig = new AuthConfig(
                    Mockito.mock(CustomOAuth2UserService.class),
                    Mockito.mock(CustomOAuth2SuccessHandler.class),
                    Mockito.mock(CustomAuthFailureHandler.class),
                    Mockito.mock(TokenBlacklistStore.class),
                    Mockito.mock(TokenProvider.class)
            );

            FilterRegistrationBean<?> bean = authConfig.firstFilterRegister();

            assertThat(bean).isNotNull();
            assertThat(bean.getUrlPatterns()).contains("/api/*");
        }
    }

    @Nested
    @DisplayName("securityFilterChain")
    class SecurityFilterChainConfig {

        @Test
        @DisplayName("보안 필터 체인을 구성한다")
        void buildsSecurityFilterChain() throws Exception {
            CustomOAuth2UserService customOAuth2UserService = Mockito.mock(CustomOAuth2UserService.class);
            CustomOAuth2SuccessHandler customOAuth2SuccessHandler = Mockito.mock(CustomOAuth2SuccessHandler.class);
            CustomAuthFailureHandler customAuthFailureHandler = Mockito.mock(CustomAuthFailureHandler.class);
            AuthConfig authConfig = new AuthConfig(
                    customOAuth2UserService,
                    customOAuth2SuccessHandler,
                    customAuthFailureHandler,
                    Mockito.mock(TokenBlacklistStore.class),
                    Mockito.mock(TokenProvider.class)
            );
            ObjectPostProcessor<Object> objectPostProcessor = new ObjectPostProcessor<>() {
                @Override
                public <O> O postProcess(O object) {
                    return object;
                }
            };
            AuthenticationManagerBuilder authBuilder = new AuthenticationManagerBuilder(objectPostProcessor);

            ClientRegistration registration = ClientRegistration.withRegistrationId("kakao")
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("https://example.com/callback")
                    .authorizationUri("https://example.com/oauth/authorize")
                    .tokenUri("https://example.com/oauth/token")
                    .userInfoUri("https://example.com/oauth/userinfo")
                    .userNameAttributeName("id")
                    .clientName("kakao")
                    .build();
            ClientRegistrationRepository repository = new InMemoryClientRegistrationRepository(registration);
            OAuth2AuthorizedClientService authorizedClientService =
                    new InMemoryOAuth2AuthorizedClientService(repository);

            Map<Class<?>, Object> sharedObjects = new HashMap<>();
            sharedObjects.put(ClientRegistrationRepository.class, repository);
            sharedObjects.put(OAuth2AuthorizedClientService.class, authorizedClientService);
            ApplicationContext applicationContext = new StaticApplicationContext();
            sharedObjects.put(ApplicationContext.class, applicationContext);

            HttpSecurity httpSecurity = new HttpSecurity(objectPostProcessor, authBuilder, sharedObjects);
            httpSecurity.setSharedObject(PathPatternRequestMatcher.Builder.class, PathPatternRequestMatcher.withDefaults());

            SecurityFilterChain result = authConfig.securityFilterChain(httpSecurity);

            assertThat(result).isNotNull();
        }
    }
}
