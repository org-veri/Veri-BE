package org.veri.be.global.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import org.veri.be.domain.auth.service.TokenBlacklistStore;
import org.veri.be.global.auth.oauth2.CustomAuthFailureHandler;
import org.veri.be.global.auth.oauth2.CustomOAuth2SuccessHandler;
import org.veri.be.global.auth.oauth2.CustomOAuth2UserService;
import org.veri.be.global.auth.token.TokenProvider;
import org.veri.be.lib.auth.jwt.JwtFilter;

import java.util.Collections;

@Profile("!test")
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomAuthFailureHandler customAuthFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.httpBasic(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().permitAll()
        );
        http.oauth2Login(config -> config
                .redirectionEndpoint(redirection -> redirection
                        .baseUri("/api/v1/oauth2/*")
                )
                .successHandler(customOAuth2SuccessHandler)
                .failureHandler(customAuthFailureHandler)
                .userInfoEndpoint(endpointConfig -> endpointConfig
                        .userService(customOAuth2UserService)));

        return http.build();
    }

    private final TokenBlacklistStore tokenBlacklistStore;
    private final TokenProvider tokenProvider;

    @Bean
    @ConditionalOnBooleanProperty(prefix = "auth.jwt", name = "use")
    public FilterRegistrationBean<OncePerRequestFilter> firstFilterRegister() {
        log.info("Filter jwt registered");
        FilterRegistrationBean<OncePerRequestFilter> registrationBean =
                new FilterRegistrationBean<>(new JwtFilter(tokenBlacklistStore, tokenProvider));

        registrationBean.setUrlPatterns(Collections.singletonList("/api/*"));

        return registrationBean;
    }
}
